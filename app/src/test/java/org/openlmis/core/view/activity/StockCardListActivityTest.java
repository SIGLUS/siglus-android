package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

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
import org.robolectric.shadows.ShadowApplication;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class StockCardListActivityTest {

  private StockCardListActivity stockCardListActivity;
  private ActivityController<StockCardListActivity> activityController;

  @Before
  public void setUp() {
    activityController = Robolectric.buildActivity(StockCardListActivity.class);
    stockCardListActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetValidStockCardFragment() {
    assertThat(stockCardListActivity.stockCardFragment).isNotNull();
  }

  @Test
  public void shouldNavigateToAddNewDrugPageWhenMenuClicked() {
    shadowOf(stockCardListActivity).clickMenuItem(StockCardListActivity.MENU_ID_ADD_NEW_DRUG);

    Intent nextIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertThat(nextIntent.getComponent().getClassName())
        .isEqualTo(InitialInventoryActivity.class.getName());
    assertThat(nextIntent.getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false)).isTrue();
  }

  @Test
  public void shouldNavigateToStockCardArchiveListWhenMenuClicked() {
    shadowOf(stockCardListActivity).clickMenuItem(StockCardListActivity.MENU_ID_ARCHIVE_LIST);

    Intent nextIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertThat(nextIntent.getComponent().getClassName())
        .isEqualTo(ArchivedDrugsListActivity.class.getName());
  }

  @Test
  public void shouldGetIntentToMe() {
    Intent intent = StockCardListActivity.getIntentToMe(RuntimeEnvironment.application);

    assertThat(intent.getComponent().getClassName())
        .isEqualTo(StockCardListActivity.class.getName());
    assertThat(intent.getFlags()).isEqualTo(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  }
}