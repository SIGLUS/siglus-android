package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.KitStockCardViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class KitStockCardListAdapterTest {

    @Test
    public void shouldUseKitStockCardViewHolder() throws Exception {
        KitStockCardListAdapter kitStockCardListAdapter = new KitStockCardListAdapter(new ArrayList<StockCardViewModel>(), null);
        View view = LayoutInflater.from(RuntimeEnvironment.application)
                .inflate(R.layout.item_stockcard, null, false);
        assertThat(kitStockCardListAdapter.createViewHolder(view)).isInstanceOf(KitStockCardViewHolder.class);
    }
}