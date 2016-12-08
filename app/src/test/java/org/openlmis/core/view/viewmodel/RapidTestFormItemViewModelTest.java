package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RapidTestFormItemViewModelTest {

    private RapidTestFormItemViewModel itemViewModel;

    @Test
    public void shouldConvertFormItemViewModelToDataModel() throws Exception {
        itemViewModel = new RapidTestFormItemViewModel(RapidTestReportViewModel.PUB_PHARMACY);
        RapidTestFormGridViewModel formGridViewModel = mock(RapidTestFormGridViewModel.class);
        itemViewModel.setRapidTestFormGridViewModelList(Arrays.asList(formGridViewModel));
        itemViewModel.convertToDataModel();
        verify(formGridViewModel).convertFormGridViewModelToDataModel(RapidTestReportViewModel.PUB_PHARMACY);
    }

    @Test
    public void shouldValidateRowViewModel() throws Exception {
        itemViewModel = new RapidTestFormItemViewModel(RapidTestReportViewModel.ACC_EMERGENCY);
        RapidTestFormGridViewModel formGridViewModel1 = mock(RapidTestFormGridViewModel.class);
        RapidTestFormGridViewModel formGridViewModel2 = mock(RapidTestFormGridViewModel.class);
        when(formGridViewModel1.validate()).thenReturn(true);
        when(formGridViewModel2.validate()).thenReturn(false);

        itemViewModel.setRapidTestFormGridViewModelList(new ArrayList<RapidTestFormGridViewModel>());
        itemViewModel.getRapidTestFormGridViewModelList().add(formGridViewModel1);
        assertTrue(itemViewModel.validate());

        itemViewModel.getRapidTestFormGridViewModelList().add(formGridViewModel2);
        assertFalse(itemViewModel.validate());
    }
}