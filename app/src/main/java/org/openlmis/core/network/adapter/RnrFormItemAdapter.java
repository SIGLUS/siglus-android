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
import java.lang.reflect.Type;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.model.repository.ProductRepository;
import roboguice.RoboGuice;

public class RnrFormItemAdapter implements JsonSerializer<RnrFormItem>,
    JsonDeserializer<RnrFormItem> {

  private final Gson gson;

  @Inject
  public ProductRepository productRepository;

  private final JsonParser jsonParser;


  public RnrFormItemAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder()
        .registerTypeAdapter(Product.class, new ProductAdapter())
        .registerTypeAdapter(ServiceItem.class, new ServiceItemAdapter())
        .excludeFieldsWithoutExposeAnnotation().create();
    jsonParser = new JsonParser();
  }

  @Override
  public JsonElement serialize(RnrFormItem rnrFormItem, Type typeOfSrc,
      JsonSerializationContext context) {
    JsonObject jsonObject = gson.toJsonTree(rnrFormItem).getAsJsonObject();
    jsonObject.addProperty("reasonForRequestedQuantity", "reason");
    jsonObject.add("serviceItems",
        jsonParser.parse(gson.toJson(rnrFormItem.getServiceItemListWrapper())));
    return jsonObject;
  }

  @Override
  public RnrFormItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    RnrFormItem rnrFormItem = gson.fromJson(json.toString(), RnrFormItem.class);
    for (ServiceItem serviceItem : rnrFormItem.getServiceItemListWrapper()) {
      serviceItem.setFormItem(rnrFormItem);
    }
    return rnrFormItem;
  }

  class ProductAdapter implements JsonDeserializer<Product>, JsonSerializer<Product> {

    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return productRepository.getByCode(json.getAsString());
      } catch (LMISException e) {
        new LMISException(e, "RnrFormItemAdapter.deserialize").reportToFabric();
        throw new JsonParseException("can not find Product by code");
      }
    }

    @Override
    public JsonElement serialize(Product src, Type typeOfSrc, JsonSerializationContext context) {
      String parseCode = src.getCode().contains(" ") ? "\"" + src.getCode() + "\"" : src.getCode();
      return new JsonParser().parse(parseCode);
    }
  }
}
