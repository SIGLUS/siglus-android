package org.openlmis.core.network.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.utils.DateUtil;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class StockMovementEntryTest {

  @Test
  public void shouldCreateStockMovementEntryForSyncUp() {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();

    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate("2016-1-1")
        .withMovementReason("reason")
        .withMovementType(MovementReasonManager.MovementType.ISSUE)
        .withStockOnHand(100)
        .withDocumentNo("123")
        .withQuantity(50)
        .build();
    stockMovementItem.setStockCard(stockCard);

    Lot lot = new Lot();
    lot.setProduct(stockCard.getProduct());
    lot.setLotNumber("ABC");
    lot.setExpirationDate(DateUtil.parseString("2020-10-31", DateUtil.DB_DATE_FORMAT));

    Lot lot2 = new Lot();
    lot2.setProduct(stockCard.getProduct());
    lot2.setLotNumber("DEF");
    lot2.setExpirationDate(DateUtil.parseString("2020-11-31", DateUtil.DB_DATE_FORMAT));

    LotMovementItem lotMovementItem1 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot)
        .setStockOnHand(50L)
        .setMovementQuantity(30L)
        .build();

    LotMovementItem lotMovementItem2 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot2)
        .setStockOnHand(100L)
        .setMovementQuantity(20L)
        .build();

    stockMovementItem
        .setLotMovementItemListWrapper(newArrayList(lotMovementItem1, lotMovementItem2));

    // when
    StockMovementEntry entry = new StockMovementEntry(stockMovementItem);
    StockMovementEntry entry1 = new StockMovementEntry(stockMovementItem);

    // then
    assertEquals(entry.getProductCode(), "productCode");
    assertEquals(entry.getQuantity(), 50);
    assertEquals(entry.getOccurred(), "2016-01-01");
    assertEquals(entry.getDocumentationNo(), "123");
    assertEquals(entry.getLotEventList().size(), 2);
    assertEquals(entry.getLotEventList().get(0).getLotNumber(), "ABC");
    assertEquals(entry.getLotEventList().get(0).getExpirationDate(), "2020-10-31");
    assertEquals(entry.getLotEventList().get(0).getQuantity(), 30L);
    assertEquals(entry.getLotEventList().get(0).getSoh(), 50);
    assertEquals(entry, entry1);
    assertNotEquals(entry.getLotEventList().get(0), entry.getLotEventList().get(1));
  }
}
