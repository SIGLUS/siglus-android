package org.openlmis.core.view.activity;

import android.view.Menu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.robolectric.Robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class KitStockCardListActivityTest {

    private KitStockCardListActivity kitStockCardListActivity;

    @Before
    public void setUp() throws Exception {
        kitStockCardListActivity = Robolectric.buildActivity(KitStockCardListActivity.class).create().get();
    }

    @Test
    public void shouldLoadKitStockOverviewFragment() throws Exception {
        assertThat(kitStockCardListActivity.stockCardFragment).isInstanceOf(KitStockCardListFragment.class);
    }

    @Test
    public void shouldNotHaveAnyMenuItems() {
        RobolectricUtils.waitLooperIdle();
        Menu optionsMenu = shadowOf(kitStockCardListActivity).getOptionsMenu();

        assertThat(optionsMenu.size()).isEqualTo(0);
    }
}