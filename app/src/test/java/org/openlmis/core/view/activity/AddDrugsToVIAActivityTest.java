package org.openlmis.core.view.activity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.presenter.AddDrugsToVIAPresenter;
import org.openlmis.core.view.adapter.AddDrugsToVIAAdapter;
import org.robolectric.Robolectric;

@RunWith(LMISTestRunner.class)
public class AddDrugsToVIAActivityTest {

  AddDrugsToVIAActivity addDrugsToVIAActivity;

  @Before
  public void setUp() throws Exception {
    addDrugsToVIAActivity = Robolectric.buildActivity(AddDrugsToVIAActivity.class).create().get();
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