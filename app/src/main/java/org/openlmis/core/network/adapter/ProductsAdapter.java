package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.openlmis.core.model.Product;

import java.util.Date;

public class ProductsAdapter implements JsonDeserializer<Product> {

    @Override
    public Product deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Gson().fromJson(json, ProductResponse.class).toProduct();
    }

    class ProductResponse extends Product {
        Type form;

        public Product toProduct() {
            setCreatedAt(new Date());
            setUpdatedAt(this.getCreatedAt());
            Product product = this;
            if (form != null){
                product.setType(form.code);
            }
            return product;
        }
    }

    class Type {
        String code;
    }

}
