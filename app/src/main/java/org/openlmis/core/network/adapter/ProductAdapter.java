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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class ProductAdapter implements JsonDeserializer<Product> {

    @Override
    public Product deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Gson().fromJson(json, ProductResponse.class).toProduct();
    }

    class ProductResponse extends Product {
        Type form;

        public Product toProduct() {
            setCreatedAt(DateUtil.getCurrentDate());
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
