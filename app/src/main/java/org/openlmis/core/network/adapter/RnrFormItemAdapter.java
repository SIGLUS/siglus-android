package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
        jsonObject.addProperty("productCode", src.getProduct().getCode());
        return jsonObject;
    }

    @Override
    public RnrFormItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RnrFormItem rnrFormItem = gson.fromJson(json.toString(), RnrFormItem.class);
        rnrFormItem.setApprovedAmount(rnrFormItem.getRequestAmount());
        return rnrFormItem;
    }

    class ProductAdapter implements JsonDeserializer<Product> {

        @Override
        public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return productRepository.getByCode(json.getAsString());
            } catch (LMISException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
