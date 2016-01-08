package org.openlmis.core.model.builder;

import org.openlmis.core.model.KitProduct;

public class KitProductBuilder {

    private KitProduct kitProduct;

    public KitProductBuilder() {
        kitProduct = new KitProduct();
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
