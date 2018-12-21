package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.openlmis.core.model.ReportTypeForm;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;


public class ReportTypeAdapter implements JsonDeserializer<ReportTypeForm> {

    private final Gson gson;

    public ReportTypeAdapter() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
                .create();
    }

    @Override
    public ReportTypeForm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ReportTypeForm reportTypeForm = gson.fromJson(json.toString(), ReportTypeForm.class);
        return reportTypeForm;
    }

}
