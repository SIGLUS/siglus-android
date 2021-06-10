package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;

@RunWith(LMISTestRunner.class)
public class RapidTestFormItemViewModelTest {

  private RapidTestFormItemViewModel itemViewModel;

  @Test
  public void shouldConvertFormItemViewModelToDataModel() throws Exception {
    MovementReasonManager.MovementReason reason1 = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, "PUB_PHARMACY", "Pub pharmacy");
    itemViewModel = new RapidTestFormItemViewModel(reason1);
    RapidTestFormGridViewModel formGridViewModel = mock(RapidTestFormGridViewModel.class);
    itemViewModel.setRapidTestFormGridViewModelList(Arrays.asList(formGridViewModel));
    itemViewModel.convertToDataModel();
    verify(formGridViewModel).convertFormGridViewModelToDataModel(reason1);
  }

  @Test
  public void shouldValidateRowViewModel() throws Exception {
    MovementReasonManager.MovementReason reason1 = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, "ACC_EMERGENCY", "Acc emergency");
    itemViewModel = new RapidTestFormItemViewModel(reason1);
    RapidTestFormGridViewModel formGridViewModel1 = mock(RapidTestFormGridViewModel.class);
    RapidTestFormGridViewModel formGridViewModel2 = mock(RapidTestFormGridViewModel.class);
    when(formGridViewModel1.validatePositive()).thenReturn(true);
    when(formGridViewModel2.validatePositive()).thenReturn(false);

    itemViewModel.setRapidTestFormGridViewModelList(new ArrayList<RapidTestFormGridViewModel>());
    itemViewModel.getRapidTestFormGridViewModelList().add(formGridViewModel1);
    assertTrue(itemViewModel.validatePositive());

    itemViewModel.getRapidTestFormGridViewModelList().add(formGridViewModel2);
    assertFalse(itemViewModel.validatePositive());
  }
}