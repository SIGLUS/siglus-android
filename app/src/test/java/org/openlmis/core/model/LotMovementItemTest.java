package org.openlmis.core.model;

import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.builder.StockMovementItemBuilder;

import java.text.ParseException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LotMovementItemTest {
    LotMovementItem lotMovementItem = new LotMovementItem();

    @Test
    public void shouldSetStockMovementItemAndUpdateMovementQuantity() throws ParseException {
        StockMovementItem stockMovementItem= new StockMovementItemBuilder().build();

        lotMovementItem.setMovementQuantity(10L);
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.ISSUE);
        lotMovementItem.setStockMovementItemAndUpdateMovementQuantity(stockMovementItem);
        assertThat(lotMovementItem.getMovementQuantity(), is(-10L));

        lotMovementItem.setMovementQuantity(20L);
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);
        lotMovementItem.setStockMovementItemAndUpdateMovementQuantity(stockMovementItem);
        assertThat(lotMovementItem.getMovementQuantity(), is(20L));
    }
}