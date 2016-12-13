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

import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

public class ProgramDataFormItemAdapter implements JsonDeserializer<ProgramDataFormItem>, JsonSerializer<ProgramDataFormItem> {
    private final Gson gson;
    private final JsonParser jsonParser;

    public ProgramDataFormItemAdapter() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
                .create();
        jsonParser = new JsonParser();
    }

    @Override
    public JsonElement serialize(ProgramDataFormItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = gson.toJsonTree(src).getAsJsonObject();
        result.add("columnCode", jsonParser.parse(src.getProgramDataColumn().getCode()));
        return result;
    }

    @Override
    public ProgramDataFormItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ProgramDataFormItem programDataFormItem = gson.fromJson(json.toString(), ProgramDataFormItem.class);
        programDataFormItem.setProgramDataColumn(new ProgramDataColumn(json.getAsJsonObject().get("columnCode").getAsString()));
        return programDataFormItem;
    }
}
