package org.openlmis.core.network.adapter;

import android.util.Log;

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
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;

import java.lang.reflect.Type;

import roboguice.RoboGuice;

public class RnrFormItemAdapter implements JsonSerializer<RnrFormItem>, JsonDeserializer<RnrFormItem> {

    private final Gson gson;

    @Inject
    public ProductRepository productRepository;


    public RnrFormItemAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder()
                .registerTypeAdapter(Product.class, new ProductAdapter())
                .excludeFieldsWithoutExposeAnnotation().create();
    }

    @Override
    public JsonElement serialize(RnrFormItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = gson.toJsonTree(src).getAsJsonObject();
        jsonObject.addProperty("reasonForRequestedQuantity", "reason");
        return jsonObject;
    }

    @Override
    public RnrFormItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return gson.fromJson(json.toString(), RnrFormItem.class);
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
            String parseCode = src.getCode().contains(" ") ? "\""+src.getCode()+"\"" : src.getCode();
            Log.e("123", parseCode);
            return new JsonParser().parse(parseCode);
        }
    }
}
