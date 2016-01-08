package org.openlmis.core.network.model;

import org.openlmis.core.model.Product;

import java.util.List;

import lombok.Data;

@Data
public class ProductAndSupportedPrograms {

    private List<String> supportedPrograms;
    private Product product;
}
