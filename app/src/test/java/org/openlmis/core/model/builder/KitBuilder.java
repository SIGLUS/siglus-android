package org.openlmis.core.model.builder;

import org.openlmis.core.model.Kit;
import org.openlmis.core.model.Product;

import java.util.ArrayList;

public class KitBuilder {

    private Kit kit;

    public KitBuilder() {
        kit = new Kit();
        kit.setProducts(new ArrayList<Product>());
        kit.setKit(true);
    }

    public KitBuilder addProduct(Product product) {
        kit.getProducts().add(product);
        return this;
    }


    public KitBuilder setPrimaryName(String primaryName) {
        kit.setPrimaryName(primaryName);
        return this;
    }

    public KitBuilder setCode(String code) {
        kit.setCode(code);
        return this;
    }

    public Kit build() {
        return kit;
    }
}
