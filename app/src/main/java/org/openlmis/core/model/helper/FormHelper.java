package org.openlmis.core.model.helper;

import java.util.List;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;

public class FormHelper {

  public class StockMovementModifiedItem {

    long totalReceived = 0;
    long totalIssued = 0;
    long totalAdjustment = 0;

    public long getTotalReceived() {
      return totalReceived;
    }

    public long getTotalIssued() {
      return totalIssued;
    }

    public long getTotalAdjustment() {
      return totalAdjustment;
    }
  }

  public StockMovementModifiedItem assignTotalValues(List<StockMovementItem> stockMovementItems) {
    StockMovementModifiedItem movementModifiedItem = new StockMovementModifiedItem();

    for (StockMovementItem item : stockMovementItems) {
      if (MovementReasonManager.MovementType.RECEIVE == item.getMovementType()) {
        movementModifiedItem.totalReceived += item.getMovementQuantity();
      } else if (MovementReasonManager.MovementType.ISSUE == item.getMovementType()) {
        movementModifiedItem.totalIssued += item.getMovementQuantity();
      } else if (MovementReasonManager.MovementType.NEGATIVE_ADJUST == item.getMovementType()) {
        movementModifiedItem.totalAdjustment -= item.getMovementQuantity();
      } else if (MovementReasonManager.MovementType.POSITIVE_ADJUST == item.getMovementType()) {
        movementModifiedItem.totalAdjustment += item.getMovementQuantity();
      }
    }
    return movementModifiedItem;
  }
}
