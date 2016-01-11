package org.openlmis.core.view.activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.robolectric.Robolectric;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class KitStockCardListActivityTest {

    @Test
    public void shouldLoadKitStockOverviewFragment() throws Exception {
        //given
        StockCardListActivity stockCardListActivity = buildActivity();

        //then
        assertThat(stockCardListActivity.stockCardFragment).isInstanceOf(KitStockCardListFragment.class);
    }

    private StockCardListActivity buildActivity() {
        return Robolectric.buildActivity(KitStockCardListActivity.class).create().get();
    }
}