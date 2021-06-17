package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.Menu;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class KitStockCardListActivityTest {

  private KitStockCardListActivity kitStockCardListActivity;
  private ActivityController<KitStockCardListActivity> activityController;

  @Before
  public void setUp() throws Exception {
    activityController = Robolectric.buildActivity(KitStockCardListActivity.class);
    kitStockCardListActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldLoadKitStockOverviewFragment() throws Exception {
    assertThat(kitStockCardListActivity.stockCardFragment)
        .isInstanceOf(KitStockCardListFragment.class);
  }

  @Test
  public void shouldNotHaveAnyMenuItems() {
    RobolectricUtils.waitLooperIdle();
    Menu optionsMenu = shadowOf(kitStockCardListActivity).getOptionsMenu();

    assertThat(optionsMenu.size()).isEqualTo(0);
  }
}