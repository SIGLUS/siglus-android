package org.openlmis.core.view.viewmodel;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.model.Inventory;

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
