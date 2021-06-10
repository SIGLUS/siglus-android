package org.openlmis.core.view.viewmodel;

import android.text.SpannableStringBuilder;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.TextStyleUtil;

@Getter
public class NonBasicProductsViewModel {

  private final Product product;
  private final SpannableStringBuilder styledProductName;
  private final String productCode;
  private final String productType;

  @Setter
  private boolean isChecked;

  public NonBasicProductsViewModel(Product product) {
    this.product = product;
    this.styledProductName = TextStyleUtil.formatStyledProductName(product);
    this.productCode = product.getCode();
    this.productType = product.getType();
    this.isChecked = false;
  }
}
