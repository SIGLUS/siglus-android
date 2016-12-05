package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class RapidTestReportViewModelTest {
    RapidTestReportViewModel viewModel;

    @Test
    public void shouldConvertToViewModel() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        RapidTestFormItemViewModel itemViewModel = mock(RapidTestFormItemViewModel.class);
        List<RapidTestFormItemViewModel> itemViewModelList = Arrays.asList(itemViewModel);
        this.viewModel.setItemViewModelList(itemViewModelList);

        Program program = new Program(Constants.RAPID_TEST_CODE, "name", "", false, null);
        this.viewModel.convertFormViewModelToDataModel(program);

        verify(itemViewModel).convertToDataModel();
        assertEquals(DateUtil.parseString("2016-08-21", DateUtil.DB_DATE_FORMAT), this.viewModel.getRapidTestForm().getPeriodBegin());
        assertEquals(DateUtil.parseString("2016-09-20", DateUtil.DB_DATE_FORMAT), this.viewModel.getRapidTestForm().getPeriodEnd());
    }
}