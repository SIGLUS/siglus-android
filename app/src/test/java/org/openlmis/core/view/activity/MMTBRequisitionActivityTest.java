package org.openlmis.core.view.activity;

import android.content.Intent;
import java.util.Date;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class VIARequisitionActivityTest {

  ALRequisitionActivity alRequisitionActivity;
  private ActivityController<ALRequisitionActivity> activityController;

  @Before
  public void setUp() throws Exception {
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    activityController = Robolectric.buildActivity(ALRequisitionActivity.class);
    alRequisitionActivity = activityController.create().start().resume().get();
  }

  @After
  public void tearDown(){
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetIntentToMe() {
    Date endDate = DateUtil.parseString("02/02/2021", DateUtil.SIMPLE_DATE_FORMAT);
    Intent intent = ALRequisitionActivity.getIntentToMe(RuntimeEnvironment.application, endDate);
    Assert.assertEquals(endDate, intent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
  }
}