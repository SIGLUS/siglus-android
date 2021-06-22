package org.openlmis.core.model.builder;

import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockMovementItem;

public class LotMovementItemBuilder {

  private final LotMovementItem lotMovementItem;

  public LotMovementItemBuilder() {
    lotMovementItem = new LotMovementItem();
  }

  public static LotMovementItemBuilder create() {
    return new LotMovementItemBuilder();
  }

  public LotMovementItemBuilder setLot(Lot lot) {
    lotMovementItem.setLot(lot);
    return this;
  }

  public LotMovementItemBuilder setStockOnHand(Long stockOnHand) {
    lotMovementItem.setStockOnHand(stockOnHand);
    return this;
  }

  public LotMovementItemBuilder setMovementQuantity(Long movementQuantity) {
    lotMovementItem.setMovementQuantity(movementQuantity);
    return this;
  }

  public LotMovementItemBuilder setReason(String reason) {
    lotMovementItem.setReason(reason);
    return this;
  }

  public LotMovementItemBuilder setStockMovementItem(StockMovementItem stockMovementItem) {
    lotMovementItem.setStockMovementItem(stockMovementItem);
    return this;
  }

  public LotMovementItem build() {
    return lotMovementItem;
  }
}
