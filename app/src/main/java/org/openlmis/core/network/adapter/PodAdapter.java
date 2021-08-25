/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.network.adapter;

import static org.openlmis.core.enumeration.OrderStatus.RECEIVED;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.network.model.PodRemoteResponse;
import org.openlmis.core.network.model.PodsLocalResponse;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class PodAdapter implements JsonDeserializer<PodsLocalResponse> {

  @Override
  public PodsLocalResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Gson gson = new Gson();
    List<PodRemoteResponse> podRemoteResponses = gson.fromJson(json, new TypeToken<List<PodRemoteResponse>>() {
    }.getType());
    PodsLocalResponse podsLocalResponse = new PodsLocalResponse();
    podsLocalResponse.setPods(FluentIterable.from(podRemoteResponses).transform(podRemoteResponse -> {
      try {
        Pod pod = Objects.requireNonNull(podRemoteResponse).from();
        pod.setLocal(false);
        pod.setDraft(false);
        pod.setSynced(pod.getOrderStatus() == RECEIVED);
        return pod;
      } catch (LMISException e) {
        new LMISException(e, "PodAdapter.deserialize").reportToFabric();
        throw new JsonParseException("Pod deserialize fail", e);
      }
    }).toList());
    return podsLocalResponse;
  }

}
