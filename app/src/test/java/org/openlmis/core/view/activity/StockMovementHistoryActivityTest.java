package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Intent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.utils.Constants;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class StockMovementHistoryActivityTest {

  private StockMovementHistoryActivity activity;
  private ActivityController<StockMovementHistoryActivity> activityController;

  @Before
  public void setUp() {
    Intent intent = new Intent();
    intent.putExtra(Constants.PARAM_STOCK_NAME, "Stock Name");

    activityController = Robolectric.buildActivity(StockMovementHistoryActivity.class, intent);
    activity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldSetTitleWhenActivityCreated() {
    assertThat(activity).isNotNull();
    assertThat(activity.getTitle()).isEqualTo("Stock Name");
  }

  @Test
  public void shouldGetIntentToMeWithStockIdAndStockName() {
    Intent intent = StockMovementHistoryActivity
        .getIntentToMe(RuntimeEnvironment.application, 100L, "StockName", false, true);

    assertThat(intent).isNotNull();
    assertThat(intent.getComponent().getClassName())
        .isEqualTo(StockMovementHistoryActivity.class.getName());
    assertThat(intent.getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0)).isEqualTo(100L);
    assertThat(intent.getStringExtra(Constants.PARAM_STOCK_NAME)).isEqualTo("StockName");
    assertThat(intent.getBooleanExtra(Constants.PARAM_IS_KIT, false)).isEqualTo(true);
  }
}