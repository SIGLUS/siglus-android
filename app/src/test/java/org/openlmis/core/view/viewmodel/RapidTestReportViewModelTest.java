package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class RapidTestReportViewModelTest {
    RapidTestReportViewModel viewModel;

    @Test
    public void shouldConvertToDataModel() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        RapidTestFormItemViewModel itemViewModel = mock(RapidTestFormItemViewModel.class);
        List<RapidTestFormItemViewModel> itemViewModelList = Arrays.asList(itemViewModel);
        viewModel.setItemViewModelList(itemViewModelList);

        Program program = new Program(Constants.RAPID_TEST_CODE, "name", "", false, null);
        viewModel.convertFormViewModelToDataModel(program);

        verify(itemViewModel).convertToDataModel();
        assertEquals(DateUtil.parseString("2016-08-21", DateUtil.DB_DATE_FORMAT), this.viewModel.getRapidTestForm().getPeriodBegin());
        assertEquals(DateUtil.parseString("2016-09-20", DateUtil.DB_DATE_FORMAT), this.viewModel.getRapidTestForm().getPeriodEnd());
    }

    @Test
    public void shouldSetRapidTestFormAndUpdateStatus() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        ProgramDataForm rapidTestForm = new ProgramDataForm();
        rapidTestForm.setStatus(ProgramDataForm.STATUS.DRAFT);
        viewModel.setRapidTestForm(rapidTestForm);
        assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel.getStatus());

        rapidTestForm.setStatus(ProgramDataForm.STATUS.AUTHORIZED);
        viewModel.setRapidTestForm(rapidTestForm);
        assertEquals(RapidTestReportViewModel.Status.COMPLETED, viewModel.getStatus());

        rapidTestForm.setStatus(ProgramDataForm.STATUS.SUBMITTED);
        viewModel.setRapidTestForm(rapidTestForm);
        assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel.getStatus());

        rapidTestForm.setStatus(ProgramDataForm.STATUS.AUTHORIZED);
        rapidTestForm.setSynced(true);
        viewModel.setRapidTestForm(rapidTestForm);
        assertEquals(RapidTestReportViewModel.Status.SYNCED, viewModel.getStatus());
    }

    @Test
    public void shouldConvertViewModelAndUpdateDataModel() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        RapidTestFormItemViewModel itemViewModel = new RapidTestFormItemViewModel(RapidTestReportViewModel.ACC_EMERGENCY);
        RapidTestFormGridViewModel gridViewModel = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.HIVDetermine);
        gridViewModel.setPositiveValue("100");
        gridViewModel.setConsumptionValue("1200");
        itemViewModel.setRapidTestFormGridViewModelList(newArrayList(gridViewModel));
        List<RapidTestFormItemViewModel> itemViewModelList = newArrayList(itemViewModel);
        this.viewModel.setItemViewModelList(itemViewModelList);

        ProgramDataForm rapidTestForm = new ProgramDataForm();
        rapidTestForm.setStatus(ProgramDataForm.STATUS.DRAFT);
        rapidTestForm.setPeriodBegin(this.viewModel.getPeriod().getBegin().toDate());
        rapidTestForm.setPeriodEnd(this.viewModel.getPeriod().getEnd().toDate());
        this.viewModel.setRapidTestForm(rapidTestForm);
        this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().add(new ProgramDataFormItem("name", "code", 100));

        Program program = new Program(Constants.RAPID_TEST_CODE, "name", "", false, null);
        this.viewModel.convertFormViewModelToDataModel(program);

        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().size(), is(2));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(0).getProgramDataColumnCode(), is("CONSUME_HIVDETERMINE"));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(0).getForm(), is(viewModel.getRapidTestForm()));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(1).getProgramDataColumnCode(), is("POSITIVE_HIVDETERMINE"));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(1).getForm(), is(viewModel.getRapidTestForm()));
    }
}