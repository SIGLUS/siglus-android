package org.openlmis.core.network.model;

import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProgramProduct;

import java.util.List;

import lombok.Data;

@Data
public class ProductAndSupportedPrograms {

    @Deprecated
    private List<String> supportedPrograms;
    private Product product;
    private List<ProgramProduct> productPrograms;
}
