package org.openlmis.core.model;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.builder.StockMovementItemBuilder;

public class LotMovementItemTest {

  LotMovementItem lotMovementItem = new LotMovementItem();

  @Test
  public void shouldSetStockMovementItemAndUpdateMovementQuantity() {
    StockMovementItem stockMovementItem = new StockMovementItemBuilder().build();

    lotMovementItem.setMovementQuantity(10L);
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.ISSUE);
    lotMovementItem.setStockMovementItemAndUpdateMovementQuantity(stockMovementItem);
    assertThat(lotMovementItem.getMovementQuantity(), is(-10L));

    lotMovementItem.setMovementQuantity(20L);
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);
    lotMovementItem.setStockMovementItemAndUpdateMovementQuantity(stockMovementItem);
    assertThat(lotMovementItem.getMovementQuantity(), is(20L));
  }

  @Test
  public void testIsUselessMovement() {
    // given
    StockMovementItem stockMovementItem = Mockito.mock(StockMovementItem.class);
    Mockito.when(stockMovementItem.getMovementType()).thenReturn(MovementType.NEGATIVE_ADJUST);
    LotMovementItem lotMovementItem = new LotMovementItem();
    lotMovementItem.setStockMovementItem(stockMovementItem);
    lotMovementItem.setMovementQuantity(0L);

    // when & then
    assertTrue(lotMovementItem.isUselessMovement());

  }
}