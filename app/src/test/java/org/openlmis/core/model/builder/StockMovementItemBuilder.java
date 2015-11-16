package org.openlmis.core.model.builder;

import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.StockMovementItem.MovementType;
import org.openlmis.core.utils.DateUtil;

import java.text.ParseException;

public class StockMovementItemBuilder {

    String movementDate = "10/10/2010";
    int stockExistence = 200;
    String documentNo = "abc";
    String movementReason = "ISSUE1";
    MovementType movementType = MovementType.ISSUE;
    int quantity = 12;

    public StockMovementItem build() throws ParseException {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(DateUtil.parseString(movementDate, "mm/dd/YYYY"));
        stockMovementItem.setDocumentNumber(documentNo);
        stockMovementItem.setStockOnHand(stockExistence);
        stockMovementItem.setReason(movementReason);
        stockMovementItem.setMovementType(movementType);
        stockMovementItem.setMovementQuantity(quantity);
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

}
