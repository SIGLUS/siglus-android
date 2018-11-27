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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;

import java.lang.reflect.Type;

import roboguice.RoboGuice;

public class programDataFormBasicItemAdapter implements JsonSerializer<ProgramDataFormBasicItem>, JsonDeserializer<ProgramDataFormBasicItem> {

    private final Gson gson;

    @Inject
    public ProductRepository productRepository;


    public programDataFormBasicItemAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder()
                .registerTypeAdapter(Product.class, new ProductAdapter())
                .excludeFieldsWithoutExposeAnnotation().create();
    }


    @Override
    public ProgramDataFormBasicItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return gson.fromJson(json.toString(), ProgramDataFormBasicItem.class);
    }

    @Override
    public JsonElement serialize(ProgramDataFormBasicItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = gson.toJsonTree(src).getAsJsonObject();
        return jsonObject;
    }

    class ProductAdapter implements JsonDeserializer<Product>, JsonSerializer<Product> {

        @Override
        public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return productRepository.getByCode(json.getAsString());
            } catch (LMISException e) {
                e.reportToFabric();
                throw new JsonParseException("can not find Product by code");
            }
        }

        @Override
        public JsonElement serialize(Product src, Type typeOfSrc, JsonSerializationContext context) {
            JsonParser jsonParser = new JsonParser();
            String parseCode = src.getCode().contains(" ") ? "\""+src.getCode()+"\"" : src.getCode();
            JsonObject result = (JsonObject) jsonParser.parse(parseCode);
            result.addProperty("name", src.getPrimaryName());
            return result;
        }
    }
}
