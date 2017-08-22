package org.openlmis.core.model.builder;

import org.openlmis.core.model.Product;

public class ProductBuilder {
    private Product product;

    public ProductBuilder() {
        product = new Product();
        product.setActive(true);
    }

    public static ProductBuilder create() {
        return new ProductBuilder();
    }

    public ProductBuilder setPrimaryName(String primaryName) {
        product.setPrimaryName(primaryName);
        return this;
    }

    public ProductBuilder setStrength(String strength) {
        product.setStrength(strength);
        return this;
    }

    public ProductBuilder setCode(String code) {
        product.setCode(code);
        return this;
    }

    public ProductBuilder setType(String type) {
        product.setType(type);
        return this;
    }

    public ProductBuilder setIsArchived(boolean isArchived) {
        product.setArchived(isArchived);
        return this;
    }

    public ProductBuilder setIsBasic(boolean isBasic) {
        product.setBasic(isBasic);
        return this;
    }

    public Product build() {
        return product;
    }

    public static Product buildAdultProduct() {
        return new ProductBuilder().setType(Product.MEDICINE_TYPE_ADULT)
                .setCode("productCode")
                .setStrength("serious")
                .setPrimaryName("Primary product name")
                .build();
    }

    public ProductBuilder setProductId(long productId) {
        product.setId(productId);
        return this;
    }

    public ProductBuilder setIsActive(boolean active) {
        product.setActive(active);
        return this;
    }

    public ProductBuilder setIsKit(boolean isKit) {
        product.setKit(isKit);
        return this;
    }
}
