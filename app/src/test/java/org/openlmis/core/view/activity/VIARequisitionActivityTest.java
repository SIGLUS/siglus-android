package org.openlmis.core.view.activity;

import android.content.Intent;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class VIARequisitionActivityTest {
  VIARequisitionActivity viaRequisitionActivity;
  private ActivityController<VIARequisitionActivity> activityController;

  @Before
  public void setUp() throws Exception {
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    activityController = Robolectric.buildActivity(VIARequisitionActivity.class);
    viaRequisitionActivity = activityController.create().start().resume().get();
  }

  @After
  public void tearDown(){
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetIntentToMeForNewRequisition() {
    //given
    Date inventoryDate = DateUtil.parseString("02/02/2021", DateUtil.SIMPLE_DATE_FORMAT);
    boolean isMissedPeriod = false;

    //when
    Intent newRequisitionIntent = VIARequisitionActivity.getIntentToMe(RuntimeEnvironment.application, inventoryDate, isMissedPeriod);

    //then
    Assert.assertEquals(VIARequisitionActivity.class.getName(), newRequisitionIntent.getComponent().getClassName());
    Assert.assertEquals(inventoryDate, newRequisitionIntent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    Assert.assertEquals(isMissedPeriod, newRequisitionIntent.getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, true));
  }

  @Ignore
  @Test
  public void shouldGetIntentToMeForEmergencyRequisition() {
    //given
    StockCard stockCard1 = new StockCard();
    StockCard stockCard2 = new StockCard();
    List<StockCard> stockCards  = Arrays.asList(stockCard1, stockCard2);

    //when
    Intent emergencyRequisitionIntent = VIARequisitionActivity.getIntentToMe(RuntimeEnvironment.application, stockCards);

    //then
    Assert.assertEquals(stockCards, emergencyRequisitionIntent.getSerializableExtra(Constants.PARAM_SELECTED_EMERGENCY));
  }
}