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

    Lot lot = getMockLot(stockCard, "lot-1", "2020-10-31");

    Lot lot2 = getMockLot(stockCard, "DEF", "2020-11-31");

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
    assertEquals("productCode", entry.getProductCode());
    assertEquals(-50, entry.getQuantity());
    assertEquals("2016-01-01", entry.getOccurredDate());
    assertEquals("123", entry.getDocumentationNo());
    assertEquals(2, entry.getLotEventList().size());
    assertEquals("lot-1", entry.getLotEventList().get(0).getLotNumber());
    assertEquals("2020-10-31", entry.getLotEventList().get(0).getExpirationDate());
    assertEquals(30L, entry.getLotEventList().get(0).getQuantity());
    assertEquals(50, entry.getLotEventList().get(0).getStockOnHand());
    assertEquals(entry, entry1);
    assertNotEquals(entry.getLotEventList().get(0), entry.getLotEventList().get(1));
  }

  @Test
  public void shouldCreateStockMovementEntryForInventory() {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate("2016-1-1")
        .withMovementReason("DAMAGED")
        .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
        .withStockOnHand(100)
        .withQuantity(50)
        .build();
    stockMovementItem.setStockCard(stockCard);
    Lot lot = getMockLot(stockCard, "ABC", "2020-10-11");

    LotMovementItem lotMovementItem = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot)
        .setStockOnHand(50L)
        .setReason("INVENTORY_POSITIVE")
        .setMovementQuantity(30L)
        .build();

    stockMovementItem
        .setLotMovementItemListWrapper(newArrayList(lotMovementItem));

    // when
    StockMovementEntry entry = new StockMovementEntry(stockMovementItem);

    // then
    assertEquals("productCode", entry.getProductCode());
    assertEquals(50, entry.getQuantity());
    assertEquals("2016-01-01", entry.getOccurredDate());
    assertEquals("ADJUSTMENT", entry.getType());
    assertEquals(1, entry.getLotEventList().size());
    assertEquals("ABC", entry.getLotEventList().get(0).getLotNumber());
    assertEquals("2020-10-11", entry.getLotEventList().get(0).getExpirationDate());
    assertEquals(30L, entry.getLotEventList().get(0).getQuantity());
    assertEquals(50, entry.getLotEventList().get(0).getStockOnHand());
  }

  private Lot getMockLot(StockCard stockCard, String lotNumber, String expireDate) {
    Lot lot = new Lot();
    lot.setProduct(stockCard.getProduct());
    lot.setLotNumber(lotNumber);
    lot.setExpirationDate(DateUtil.parseString(expireDate, DateUtil.DB_DATE_FORMAT));
    return lot;
  }
}
