package org.openlmis.core.model;

public class ProductBuilder {
    private Product product;

    public ProductBuilder() {
        product = new Product();
    }

    public ProductBuilder setPrimaryName(String primaryName) {
        product.setPrimaryName(primaryName);
        return this;
    }

    public ProductBuilder setProgram(Program program) {
        product.setProgram(program);
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

    public Product build() {
        return product;
    }

    public static Product buildAdultProduct() {
        return new ProductBuilder().setType(Product.MEDICINE_TYPE_ADULT)
                .setCode("productCode")
                .setStrength("serious")
                .setPrimaryName("Primary product name")
                .setProgram(new ProgramBuilder().setProgramCode("programCode").setProgramName("programName").build())
                .build();
    }

}
