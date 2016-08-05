package org.openlmis.core.model;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StockMovementItemTest {

    private StockMovementItem stockMovementItem;

    @Before
    public void setUp() throws Exception {
        stockMovementItem = new StockMovementItem();
        stockMovementItem.setStockOnHand(100);
        stockMovementItem.setMovementQuantity(50);
    }

    @Test
    public void shouldSetInitialAmountCorrectWhenTypeIsIssue() throws Exception {
        //given
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.ISSUE);

        //when
        long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

        //then
        assertThat(calculatePreviousSOH, is(150L));
    }

    @Test
    public void shouldSetInitialAmountCorrectWhenTypeIsNegative() throws Exception {
        //given
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST);

        //when
        long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

        //then
        assertThat(calculatePreviousSOH, is(150L));
    }

    @Test
    public void shouldSetInitialAmountCorrectWhenTypeIsReceive() throws Exception {
        //given
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);

        //when
        long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

        //then
        assertThat(calculatePreviousSOH, is(50L));
    }

    @Test
    public void shouldSetInitialAmountCorrectWhenTypeIsPositive() throws Exception {
        //given
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);

        //when
        long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

        //then
        assertThat(calculatePreviousSOH, is(50L));
    }

    @Test
    public void shouldSetInitialAmountCorrectWhenTypeIsPhysical() throws Exception {
        //given
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        stockMovementItem.setMovementQuantity(0L);

        //when
        long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

        //then
        assertThat(calculatePreviousSOH, is(100L));
    }

}