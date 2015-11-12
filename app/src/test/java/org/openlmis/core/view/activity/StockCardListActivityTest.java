package org.openlmis.core.view.activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.robolectric.Robolectric;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class StockCardListActivityTest {

    private StockCardListActivity stockCardListActivity;

    @Before
    public void setUp() {
        stockCardListActivity = Robolectric.buildActivity(StockCardListActivity.class).create().get();
    }

    @Test
    public void shouldGetValidStockCardFragment() {
        assertThat(stockCardListActivity.stockCardFragment).isNotNull();
    }
}