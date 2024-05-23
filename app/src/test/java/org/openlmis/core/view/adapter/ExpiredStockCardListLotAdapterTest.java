package org.openlmis.core.view.adapter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import org.junit.Test;
import org.openlmis.core.R;

public class ExpiredStockCardListLotAdapterTest {

  final ExpiredStockCardListLotAdapter adapter = new ExpiredStockCardListLotAdapter(new ArrayList<>(), null);

  @Test
  public void shouldReturnMatchedLayoutIdWhenGetItemStockCardListLotLayoutIdIsCalled() {
    assertEquals(R.layout.item_expired_stock_card_list_lot_info, adapter.getItemStockCardListLotLayoutId());
  }
}