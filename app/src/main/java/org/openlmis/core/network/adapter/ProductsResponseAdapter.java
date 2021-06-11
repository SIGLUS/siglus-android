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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.network.ProgramCacheManager;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;

/**
 * adapt v2 products api response to @see <a href="https://showdoc.siglus.us/web/#/9?page_id=310">v3 products api</a>
 */
public class ProductsResponseAdapter implements JsonDeserializer<SyncDownLatestProductsResponse> {

  @Override
  public SyncDownLatestProductsResponse deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    final SyncDownLatestProductsResponse syncDownLatestProductsResponse =
        new SyncDownLatestProductsResponse();
    final JsonObject responseObject = json.getAsJsonObject();
    syncDownLatestProductsResponse.setLastSyncTime(getString(responseObject, "lastSyncTime", ""));

    final ArrayList<ProductAndSupportedPrograms> latestProducts = new ArrayList<>();
    syncDownLatestProductsResponse.setLatestProducts(latestProducts);
    if (!responseObject.has("products")) {
      return syncDownLatestProductsResponse;
    }
    for (JsonElement jsonProduct : responseObject.get("products").getAsJsonArray()) {
      latestProducts.add(generateProductAndSupportedPrograms(jsonProduct));
    }
    return syncDownLatestProductsResponse;
  }

  private ProductAndSupportedPrograms generateProductAndSupportedPrograms(JsonElement jsonElement) {
    final JsonObject jsonProduct = jsonElement.getAsJsonObject();
    final ProductAndSupportedPrograms productAndSupportedPrograms =
        new ProductAndSupportedPrograms();

    final Product product = generateProduct(jsonProduct);
    final String programCode = jsonProduct.get("programCode").getAsString();
    final String category = getString(jsonProduct, "category", "Default");
    final List<ProductProgram> productPrograms = generateProductProgram(product, programCode,
        category);

    productAndSupportedPrograms.setProduct(product);
    productAndSupportedPrograms.setProductPrograms(productPrograms);
    return productAndSupportedPrograms;
  }

  private Product generateProduct(JsonObject jsonProduct) {
    final Product product = new Product();
    product.setCode(jsonProduct.get("productCode").getAsString());
    product.setPrimaryName(jsonProduct.get("fullProductName").getAsString());
    product.setProgram(ProgramCacheManager.getPrograms(product.getCode()));
    product.setStrength("");
    product.setType("");
    product.setArchived(getBoolean(jsonProduct, "archived", false));
    product.setActive(getBoolean(jsonProduct, "active", true));
    product.setKit(getBoolean(jsonProduct, "isKit", false));
    product.setBasic(getBoolean(jsonProduct, "isBasic", false));
    product.setKitProductList(generateKitProduct(product.getCode(), jsonProduct));
    return product;
  }

  private List<KitProduct> generateKitProduct(String productCode, JsonObject jsonProduct) {
    final ArrayList<KitProduct> kitProducts = new ArrayList<>();
    if (jsonProduct.has("children")) {
      for (JsonElement children : jsonProduct.getAsJsonArray("children")) {
        final KitProduct kitProduct = new KitProduct();
        final JsonObject jsonKitProduct = children.getAsJsonObject();
        kitProduct.setKitCode(productCode);
        kitProduct.setProductCode(jsonKitProduct.get("productCode").getAsString());
        kitProduct.setQuantity(jsonKitProduct.get("quantity").getAsInt());
        kitProducts.add(kitProduct);
      }
    }
    return kitProducts;
  }

  private List<ProductProgram> generateProductProgram(Product product, String programCode,
      String category) {
    final ArrayList<ProductProgram> productPrograms = new ArrayList<>();
    final ProductProgram productProgram = new ProductProgram();
    productProgram.setProgramCode(programCode);
    productProgram.setProductCode(product.getCode());
    productProgram.setActive(product.isActive());
    productProgram.setCategory(category);
    return productPrograms;
  }

  private boolean getBoolean(JsonObject jsonObject, String memberName, boolean defaultValue) {
    return jsonObject.has(memberName) ? jsonObject.get(memberName).getAsBoolean() : defaultValue;
  }

  private String getString(JsonObject jsonObject, String memberName, String defaultValue) {
    return jsonObject.has(memberName) ? jsonObject.get(memberName).getAsString() : defaultValue;
  }
}
