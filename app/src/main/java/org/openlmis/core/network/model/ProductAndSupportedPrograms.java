package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;

@Data
public class ProductAndSupportedPrograms {

  private Product product;
  private List<ProductProgram> productPrograms;
}
