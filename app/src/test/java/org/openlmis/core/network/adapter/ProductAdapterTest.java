package org.openlmis.core.network.adapter;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.google.gson.JsonParser;
import org.junit.Test;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.JsonFileReader;

public class ProductAdapterTest {

  @Test
  public void shouldDeserializeProductFromServerResponse() throws Exception {
    String json = JsonFileReader.readJson(getClass(), "ProductResponse.json");

    ProductAdapter productAdapter = new ProductAdapter();

    Product deserializedProduct = productAdapter
        .deserialize(new JsonParser().parse(json), null, null);

    assertTrue(deserializedProduct.isActive());
    assertEquals("Comprimidos", deserializedProduct.getType());
    assertEquals("Digoxina 0,25mg Comp", deserializedProduct.getPrimaryName());
    assertEquals("0,25mg", deserializedProduct.getStrength());
  }
}