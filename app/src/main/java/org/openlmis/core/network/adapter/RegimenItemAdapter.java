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
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.repository.RegimenRepository;
import roboguice.RoboGuice;

@SuppressWarnings("PMD")
public class RegimenItemAdapter implements JsonSerializer<RegimenItem>,
    JsonDeserializer<RegimenItem> {

  private final Gson gson;

  @Inject
  public RegimenRepository regimenRepository;

  public RegimenItemAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  }

  @Override
  public RegimenItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    RegimenItem regimenItem = gson.fromJson(json, RegimenItem.class);

    try {
      Regimen regimen = regimenRepository
          .getByCode(json.getAsJsonObject().get("code").getAsString());
      if (regimen == null) {
        regimen = createRegimen(json);
      }
      regimenItem.setRegimen(regimen);
    } catch (LMISException e) {
      new LMISException(e, "RegimenItemAdapter.deserialize").reportToFabric();
      throw new JsonParseException("can not find RegimenItem by name and category");
    }
    return regimenItem;
  }

  private Regimen createRegimen(JsonElement json) throws LMISException {
    Regimen regimen = gson.fromJson(json, Regimen.class);
    regimen.setCustom(true);
    regimenRepository.create(regimen);
    return regimen;
  }

  @Override
  public JsonElement serialize(RegimenItem src, Type typeOfSrc, JsonSerializationContext context) {
    JsonParser jsonParser = new JsonParser();
    JsonObject result = jsonParser.parse(gson.toJson(src.getRegimen())).getAsJsonObject();
    result.addProperty("patientsOnTreatment", src.getAmount());
    result.addProperty("comunitaryPharmacy", src.getPharmacy());
    result.addProperty("information", src.getInformation());
    result.addProperty("productCode", src.getProductCode());
    result.addProperty("hf", src.getHf());
    result.addProperty("chw", src.getChw());
    return result;
  }
}
