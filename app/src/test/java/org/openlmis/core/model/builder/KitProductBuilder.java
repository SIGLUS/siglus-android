package org.openlmis.core.model.builder;

import org.openlmis.core.model.KitProduct;

public class KitProductBuilder {

  private final KitProduct kitProduct;

  public KitProductBuilder() {
    kitProduct = new KitProduct();
  }

  public static KitProductBuilder create() {
    return new KitProductBuilder();
  }

  public KitProductBuilder setKitCode(String kitCode) {
    kitProduct.setKitCode(kitCode);
    return this;
  }

  public KitProductBuilder setProductCode(String productCode) {
    kitProduct.setProductCode(productCode);
    return this;
  }

  public KitProductBuilder setQuantity(Integer quantity) {
    kitProduct.setQuantity(quantity);
    return this;
  }

  public KitProduct build() {
    return kitProduct;
  }
}
