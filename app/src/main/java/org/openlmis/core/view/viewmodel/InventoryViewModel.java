package org.openlmis.core.view.viewmodel;


import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.Product;

import lombok.Data;

@Data
public class InventoryViewModel {

    Product product;
    boolean checked;
    String quantity;
    String expireDate;

    public InventoryViewModel (Product product){
        this.product = product;
    }

    public void reset(){
        quantity = StringUtils.EMPTY;
        expireDate = StringUtils.EMPTY;
    }
}
