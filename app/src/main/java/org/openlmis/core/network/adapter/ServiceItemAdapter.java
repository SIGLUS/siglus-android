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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.model.repository.ServiceFormRepository;
import roboguice.RoboGuice;

public class ServiceItemAdapter implements JsonDeserializer<ServiceItem>,
    JsonSerializer<ServiceItem> {

  private final Gson gson;

  @Inject
  public ServiceFormRepository serviceFormRepository;

  public ServiceItemAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder()
        .registerTypeAdapter(Service.class, new ServiceAdapter())
        .excludeFieldsWithoutExposeAnnotation().create();
  }

  @Override
  public ServiceItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    ServiceItem serviceItem = gson.fromJson(json, ServiceItem.class);
    try {
      Service service = serviceFormRepository
          .queryByCode(json.getAsJsonObject().get("code").getAsString());
      serviceItem.setService(service);
    } catch (LMISException e) {
      new LMISException(e, "ServiceItemAdapter.deserialize").reportToFabric();
      throw new JsonParseException("can not find service by code");
    }
    return serviceItem;
  }

  @Override
  public JsonElement serialize(ServiceItem src, Type typeOfSrc, JsonSerializationContext context) {
    JsonParser jsonParser = new JsonParser();
    JsonObject result = jsonParser.parse(gson.toJson(src)).getAsJsonObject();
    Service service = src.getService();
    result.addProperty("name", service.getName());
    result.addProperty("code", service.getCode());
    return result;
  }
}
