package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class RapidTestFormItemViewModelTest {
    @Test
    public void shouldConvertFormItemViewModelToDataModel() throws Exception {
        RapidTestFormItemViewModel item_PUB_PHARMACY = new RapidTestFormItemViewModel(RapidTestReportViewModel.PUB_PHARMACY);
        RapidTestFormGridViewModel formGridViewModel = mock(RapidTestFormGridViewModel.class);
        item_PUB_PHARMACY.setRapidTestFormGridViewModelList(Arrays.asList(formGridViewModel));
        item_PUB_PHARMACY.convertToDataModel();
        verify(formGridViewModel).convertFormGridViewModelToDataModel(RapidTestReportViewModel.PUB_PHARMACY);
    }
}