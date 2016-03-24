package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RegimeProduct;

import lombok.Data;

@Data
public class RegimeProductViewModel {
    private String shortCode;
    private String entireName;
    private boolean checked;

    public RegimeProductViewModel(RegimeProduct regimeProduct) {
        this.shortCode = regimeProduct.getShortCode();
        this.entireName = regimeProduct.getEntireName();
    }
}
