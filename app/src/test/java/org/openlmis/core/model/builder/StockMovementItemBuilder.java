package org.openlmis.core.model.builder;

import java.util.Date;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;

public class StockMovementItemBuilder {

  String movementDate = "2010-10-10";
  int stockExistence = 200;
  String documentNo = "abc";
  String movementReason = "ISSUE1";
  String signature = "signature";
  MovementType movementType = MovementReasonManager.MovementType.ISSUE;
  int quantity = 12;
  Date createdTime = DateUtil.getCurrentDate();
  long requested = 10L;

  public StockMovementItem build() {
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementDate(DateUtil.parseString(movementDate, DateUtil.DB_DATE_FORMAT));
    stockMovementItem.setDocumentNumber(documentNo);
    stockMovementItem.setStockOnHand(stockExistence);
    stockMovementItem.setReason(movementReason);
    stockMovementItem.setMovementType(movementType);
    stockMovementItem.setMovementQuantity(quantity);
    stockMovementItem.setCreatedTime(createdTime);
    stockMovementItem.setRequested(requested);
    stockMovementItem.setSignature(signature);
    return stockMovementItem;
  }

  public StockMovementItemBuilder withMovementDate(String movementDate) {
    this.movementDate = movementDate;
    return this;
  }

  public StockMovementItemBuilder withDocumentNo(String documentNo) {
    this.documentNo = documentNo;
    return this;
  }

  public StockMovementItemBuilder withMovementReason(String movementReason) {
    this.movementReason = movementReason;
    return this;
  }

  public StockMovementItemBuilder withQuantity(int quantity) {
    this.quantity = quantity;
    return this;
  }

  public StockMovementItemBuilder withMovementType(MovementType movementType) {
    this.movementType = movementType;
    return this;
  }

  public StockMovementItemBuilder withStockOnHand(int stockExistence) {
    this.stockExistence = stockExistence;
    return this;
  }

}
