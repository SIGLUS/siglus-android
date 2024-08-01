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
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class MMTBRequisitionActivityTest {

  MMTBRequisitionActivity mmtbRequisitionActivity;
  private ActivityController<MMTBRequisitionActivity> activityController;

  @Before
  public void setUp() throws Exception {
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    activityController = Robolectric.buildActivity(MMTBRequisitionActivity.class);
    mmtbRequisitionActivity = activityController.create().start().resume().get();
  }

  @After
  public void tearDown(){
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetIntentToMeForEndDate() {
    //given
    Date endDate = DateUtil.parseString("02/02/2021", DateUtil.SIMPLE_DATE_FORMAT);

    //when
    Intent intent = MMTBRequisitionActivity.getIntentToMe(ApplicationProvider.getApplicationContext(), endDate);

    //then
    Assert.assertEquals(MMTBRequisitionActivity.class.getName(), intent.getComponent().getClassName());
    Assert.assertEquals(endDate, intent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
  }

  @Test
  public void shouldGetIntentToMeForFormId() {
    //given
    long formId = 100L;

    //when
    Intent intent = MMTBRequisitionActivity.getIntentToMe(ApplicationProvider.getApplicationContext(), formId);

    //then
    Assert.assertEquals(MMTBRequisitionActivity.class.getName(), intent.getComponent().getClassName());
    Assert.assertEquals(formId, intent.getLongExtra(Constants.PARAM_FORM_ID, 0L));
  }
}