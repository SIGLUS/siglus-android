package org.openlmis.core.network.model;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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

    StockMovementEntry entry = new StockMovementEntry(stockMovementItem, "123");
    assertThat(entry.getFacilityId(), is("123"));
    assertThat(entry.getProductCode(), is("productCode"));
    assertEquals(entry.getQuantity(), 50);
    assertThat(entry.getReasonName(), is("reason"));
    assertThat(entry.getOccurred(), is("2016-01-01"));
    assertThat(entry.getReferenceNumber(), is("123"));
    assertNull(entry.getRequestedQuantity());
    assertThat(entry.getLotEventList().size(), is(2));
    assertThat(entry.getLotEventList().get(0).getLotNumber(), is("ABC"));
    assertThat(entry.getLotEventList().get(0).getExpirationDate(), is("2020-10-31"));
    assertThat(entry.getLotEventList().get(0).getQuantity(), is(30L));
    assertThat(entry.getLotEventList().get(0).getCustomProps().get("SOH"), is("50"));

    StockMovementEntry entry1 = new StockMovementEntry(stockMovementItem, "123");

    assertEquals(entry, entry1);
    assertNotEquals(entry.getLotEventList().get(0), entry.getLotEventList().get(1));
  }
}
