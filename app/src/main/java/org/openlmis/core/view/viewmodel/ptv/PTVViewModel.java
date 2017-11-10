package org.openlmis.core.view.viewmodel.ptv;

import lombok.Getter;

@Getter
public class PTVViewModel {
    private String placeholderItemName;
    private long quantity1, quantity2, quantity3, quantity4, quantity5;

    public PTVViewModel(String placeholderItemName) {
        this.placeholderItemName = placeholderItemName;
    }

    public void setQuantity(int productPosition, long quantity) {
        switch (productPosition) {
            case 1:
                this.quantity1 = quantity;
                break;
            case 2:
                this.quantity2 = quantity;
                break;
            case 3:
                this.quantity3 = quantity;
                break;
            case 4:
                this.quantity4 = quantity;
                break;
            case 5:
                this.quantity5 = quantity;
                break;
        }
    }

}
