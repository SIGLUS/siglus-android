package org.openlmis.core.model.builder;

import org.openlmis.core.model.ProductProgram;

public class ProductProgramBuilder {

    ProductProgram productProgram;

    public ProductProgramBuilder() {
        productProgram = new ProductProgram();
    }

    public ProductProgramBuilder setProductCode(String productCode) {
        productProgram.setProductCode(productCode);
        return this;
    }

    public ProductProgramBuilder setProgramCode(String programCode) {
        productProgram.setProgramCode(programCode);
        return this;
    }

    public ProductProgramBuilder setActive(boolean isActive) {
        productProgram.setActive(isActive);
        return this;
    }

    public ProductProgramBuilder setCategory(String category) {
        productProgram.setCategory(category);
        return this;
    }

    public ProductProgram build() {
        return productProgram;
    }


}
