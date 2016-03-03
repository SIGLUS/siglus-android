package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.Inventory;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SelectInventoryViewModel {

    private Date inventoryDate;
    private boolean showTime;
    private boolean isChecked;

    public SelectInventoryViewModel(Inventory inventory) {
        inventoryDate = inventory.getCreatedAt();
    }

}
