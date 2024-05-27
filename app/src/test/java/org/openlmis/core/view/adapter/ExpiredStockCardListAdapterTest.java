package org.openlmis.core.view.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.ExpiredStockCardListViewHolder;
import org.openlmis.core.view.holder.StockCardViewHolder;

import java.util.ArrayList;

@RunWith(LMISTestRunner.class)
public class ExpiredStockCardListAdapterTest {

    final StockCardListAdapter adapter = new ExpiredStockCardListAdapter(new ArrayList<>());

    @Test
    public void shouldReturnMatchedLayoutIdWhenGetItemStockCardLayoutIdIsCalled() {
        assertEquals(R.layout.item_expired_stock_card, adapter.getItemStockCardLayoutId());
    }

    @Test
    public void shouldReturnExpiredStockCardListViewHolderWhenCreateViewHolderIsCalled() {
        StockCardViewHolder actualViewHolder =
                adapter.createViewHolder(new View(ApplicationProvider.getApplicationContext()));

        assertTrue(actualViewHolder instanceof ExpiredStockCardListViewHolder);
    }
}