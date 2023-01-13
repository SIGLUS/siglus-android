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
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class MMIARequisitionActivityTest {
  MMIARequisitionActivity viaRequisitionActivity;
  private ActivityController<MMIARequisitionActivity> activityController;

  @Before
  public void setUp() throws Exception {
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    activityController = Robolectric.buildActivity(MMIARequisitionActivity.class);
    viaRequisitionActivity = activityController.create().start().resume().get();
  }

  @After
  public void tearDown(){
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetIntentToMeForEndData() {
    //given
    Date endDate = DateUtil.parseString("02/02/2021", DateUtil.SIMPLE_DATE_FORMAT);

    Program program = new Program();
    program.setProgramCode(Program.TARV_CODE);
    program.setProgramName(Program.TARV_CODE);
    RnRForm form = RnRForm.init(program, DateUtil.today());
    form.setStatus(Status.DRAFT);
    RnRFormViewModel viewModel = RnRFormViewModel.buildNormalRnrViewModel(form);
    long viewModelId = 100L;
    viewModel.setId(viewModelId);

    //when
    Intent emptyViewModelIntent = MMIARequisitionActivity.getIntentToMe(RuntimeEnvironment.application, endDate, null);
    Intent withViewModelIntent = MMIARequisitionActivity.getIntentToMe(RuntimeEnvironment.application, endDate, viewModel);

    //then
    Assert.assertEquals(MMIARequisitionActivity.class.getName(), emptyViewModelIntent.getComponent().getClassName());
    Assert.assertEquals(endDate, emptyViewModelIntent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    Assert.assertEquals(0L, emptyViewModelIntent.getLongExtra(Constants.PARAM_PREVIOUS_FORM, 0L));

    Assert.assertEquals(MMIARequisitionActivity.class.getName(), withViewModelIntent.getComponent().getClassName());
    Assert.assertEquals(endDate, withViewModelIntent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    Assert.assertEquals(viewModelId, withViewModelIntent.getLongExtra(Constants.PARAM_PREVIOUS_FORM, 0L));
  }
}