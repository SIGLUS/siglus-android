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
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.network.model.RnrFormStatusEntry;

public class RnrFormStatusEntryAdapter implements JsonDeserializer<RnrFormStatusEntry> {

  private final Gson gson;

  public RnrFormStatusEntryAdapter() {
    gson = new GsonBuilder()
        .registerTypeAdapter(Status.class, new RnrFormStatusAdapter())
        .create();
  }

  @Override
  public RnrFormStatusEntry deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context)
      throws JsonParseException {
    return gson.fromJson(json, RnrFormStatusEntry.class);
  }
}
