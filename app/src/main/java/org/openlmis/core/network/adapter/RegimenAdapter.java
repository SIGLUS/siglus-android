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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.network.ProgramCacheManager;
import org.openlmis.core.network.model.SyncDownRegimensResponse;

public class RegimenAdapter implements JsonDeserializer<SyncDownRegimensResponse> {

  @Override
  public SyncDownRegimensResponse deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    final SyncDownRegimensResponse syncDownRegimensResponse = new SyncDownRegimensResponse();
    final JsonArray jsonArray = json.getAsJsonArray();
    final List<Regimen> regimenList = new ArrayList<>();
    for (JsonElement jsonElement : jsonArray) {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      Regimen regimen = Regimen.builder()
          .name(jsonObject.get("name").getAsString())
          .code(jsonObject.get("code").getAsString())
          .program(ProgramCacheManager.getPrograms(jsonObject.get("programCode").getAsString()))
          .active(jsonObject.get("active").getAsBoolean())
          .type(getType(jsonObject, "category", Regimen.RegimeType.Default))
          .displayOrder(Long.valueOf(jsonObject.get("displayOrder").getAsString()))
          .isCustom(jsonObject.get("isCustom").getAsBoolean())
          .build();
      regimenList.add(regimen);
    }
    syncDownRegimensResponse.setRegimenList(regimenList);
    return syncDownRegimensResponse;
  }

  private Regimen.RegimeType getType(JsonObject jsonObject, String memberName,
      Regimen.RegimeType defaultType) {
    if (!jsonObject.has(memberName)) {
      return defaultType;
    }
    JsonObject category = jsonObject.get(memberName).getAsJsonObject();
    if (category.get("name").equals(Regimen.RegimeType.Adults)) {
      return Regimen.RegimeType.Adults;
    } else if (category.get("name").equals(Regimen.RegimeType.Paediatrics)) {
      return Regimen.RegimeType.Paediatrics;
    } else {
      return Regimen.RegimeType.Default;
    }
  }
}
