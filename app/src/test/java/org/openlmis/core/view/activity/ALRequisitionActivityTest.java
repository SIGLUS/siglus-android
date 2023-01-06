package org.openlmis.core.view.activity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.presenter.AddDrugsToVIAPresenter;
import org.openlmis.core.view.adapter.AddDrugsToVIAAdapter;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class AddDrugsToVIAActivityTest {

  AddDrugsToVIAActivity addDrugsToVIAActivity;
  private ActivityController<AddDrugsToVIAActivity> activityController;

  @Before
  public void setUp() throws Exception {
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    activityController = Robolectric.buildActivity(AddDrugsToVIAActivity.class);
    addDrugsToVIAActivity = activityController.create().start().resume().get();
  }

  @After
  public void tearDown(){
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldNotGoToNextPageIfValidationFailed() throws Exception {
    addDrugsToVIAActivity.mAdapter = mock(AddDrugsToVIAAdapter.class);
    when(addDrugsToVIAActivity.mAdapter.validateAll()).thenReturn(1);

    addDrugsToVIAActivity.presenter = mock(AddDrugsToVIAPresenter.class);
    addDrugsToVIAActivity.btnComplete.performClick();

    verify(addDrugsToVIAActivity.presenter, never()).convertViewModelsToRnrFormItems();
  }
}