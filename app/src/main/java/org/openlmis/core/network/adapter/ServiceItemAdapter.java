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
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.ServiceFormRepository;

import java.lang.reflect.Type;

import roboguice.RoboGuice;

import static org.openlmis.core.model.Regimen.RegimeType;

public class ServiceItemAdapter implements JsonDeserializer<ServiceItem>, JsonSerializer<ServiceItem> {

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
    public ServiceItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ServiceItem serviceItem = gson.fromJson(json, ServiceItem.class);
        try {
            Service service = serviceFormRepository.queryByCode(json.getAsJsonObject().get("code").getAsString());
            serviceItem.setService(service);
        } catch (LMISException e) {
            e.reportToFabric();
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
