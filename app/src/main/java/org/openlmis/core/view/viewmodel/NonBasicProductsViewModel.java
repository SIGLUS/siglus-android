package org.openlmis.core.view.viewmodel;

import android.text.SpannableStringBuilder;

import org.openlmis.core.model.Product;
import org.openlmis.core.utils.TextStyleUtil;

import lombok.Getter;
import lombok.Setter;

@Getter
public class NonBasicProductsViewModel {

    private Product product;
    private SpannableStringBuilder styledProductName;
    private String productCode;
    private String productType;

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
