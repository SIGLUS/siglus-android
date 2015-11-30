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

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.repository.RegimenRepository;

import java.lang.reflect.Type;

import roboguice.RoboGuice;

public class RegimenItemAdapter implements JsonSerializer<RegimenItem>, JsonDeserializer<RegimenItem> {

    private final Gson gson;

    @Inject
    public RegimenRepository regimenRepository;

    public RegimenItemAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    @Override
    public RegimenItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RegimenItem regimenItem = gson.fromJson(json, RegimenItem.class);
        try {
            regimenItem.setRegimen(regimenRepository.getByCode(json.getAsJsonObject().get("code").getAsString()));
        } catch (LMISException e) {
            e.reportToFabric();
            throw new JsonParseException("can not find RegimenItem by code");
        }
        return regimenItem;
    }

    @Override
    public JsonElement serialize(RegimenItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(gson.toJson(src.getRegimen())).getAsJsonObject();
        result.addProperty("patientsOnTreatment", src.getAmount());
        return result;
    }
}
