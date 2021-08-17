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
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

@SuppressWarnings("squid:S1874")
public class ProgramDataFormItemAdapter implements JsonDeserializer<ProgramDataFormItem>,
    JsonSerializer<ProgramDataFormItem> {

  private final Gson gson;
  private final JsonParser jsonParser;

  public ProgramDataFormItemAdapter() {
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
        .create();
    jsonParser = new JsonParser();
  }

  @Override
  public JsonElement serialize(ProgramDataFormItem src, Type typeOfSrc,
      JsonSerializationContext context) {
    JsonObject result = gson.toJsonTree(src).getAsJsonObject();
    result.add("columnCode", jsonParser.parse(src.getProgramDataColumn().getCode()));
    return result;
  }

  @Override
  public ProgramDataFormItem deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    ProgramDataFormItem programDataFormItem = gson
        .fromJson(json.toString(), ProgramDataFormItem.class);
    programDataFormItem.setProgramDataColumn(
        new ProgramDataColumn(json.getAsJsonObject().get("columnCode").getAsString()));
    return programDataFormItem;
  }
}
