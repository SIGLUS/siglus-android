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
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.repository.RegimenRepository;

import java.lang.reflect.Type;

import roboguice.RoboGuice;

import static org.openlmis.core.model.Regimen.RegimeType;

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

        RegimeType regimeType = null;
        String categoryName = json.getAsJsonObject().get("categoryName").getAsString();
        if(categoryName.equals(RegimeType.Adults.toString())) {
            regimeType = RegimeType.Adults;
        } else if (categoryName.equals(RegimeType.Paediatrics.toString())) {
            regimeType = RegimeType.Paediatrics;
        }

        try {
            Regimen regimen = regimenRepository.getByNameAndCategory(json.getAsJsonObject().get("name").getAsString(), regimeType);
            if (regimen == null) {
                regimen = createRegimen(json);
            }
            regimenItem.setRegimen(regimen);
        } catch (LMISException e) {
            e.reportToFabric();
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
        return result;
    }
}
