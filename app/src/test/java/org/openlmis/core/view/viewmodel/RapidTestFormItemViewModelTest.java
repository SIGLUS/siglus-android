package org.openlmis.core.view.viewmodel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

}