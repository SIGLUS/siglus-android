package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.Signature;
import org.openlmis.core.model.builder.ProgramDataColumnBuilder;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.consumption;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.positive;

@RunWith(LMISTestRunner.class)
public class RapidTestReportViewModelTest {
    RapidTestReportViewModel viewModel;

    @Test
    public void shouldConvertToDataModel() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        RapidTestFormItemViewModel itemViewModel = mock(RapidTestFormItemViewModel.class);
        List<RapidTestFormItemViewModel> itemViewModelList = Arrays.asList(itemViewModel);
        viewModel.setItemViewModelList(itemViewModelList);

        Program program = new Program(Constants.RAPID_TEST_CODE, "name", "", false, null, null);
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
        MovementReasonManager.MovementReason reason1 = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "ACC_EMERGENCY", "Acc emergency");

        RapidTestFormItemViewModel itemViewModel = new RapidTestFormItemViewModel(reason1);
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
        this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().add(new ProgramDataFormItem("name", new ProgramDataColumnBuilder().setCode("code").build(), 100));

        Program program = new Program(Constants.RAPID_TEST_CODE, "name", "", false, null, null);
        this.viewModel.convertFormViewModelToDataModel(program);

        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().size(), is(2));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(0).getProgramDataColumn().getCode(), is("CONSUME_HIVDETERMINE"));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(0).getForm(), is(viewModel.getRapidTestForm()));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(1).getProgramDataColumn().getCode(), is("POSITIVE_HIVDETERMINE"));
        assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(1).getForm(), is(viewModel.getRapidTestForm()));
    }

    @Test
    public void shouldValidateItemList() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        RapidTestFormItemViewModel itemViewModel1 = mock(RapidTestFormItemViewModel.class);
        RapidTestFormItemViewModel itemViewModel2 = mock(RapidTestFormItemViewModel.class);

        when(itemViewModel1.validatePositive()).thenReturn(true);
        when(itemViewModel2.validatePositive()).thenReturn(false);

        viewModel.setItemViewModelList(new ArrayList<RapidTestFormItemViewModel>());
        viewModel.getItemViewModelList().add(itemViewModel1);
        assertTrue(viewModel.validatePositive());

        viewModel.getItemViewModelList().add(itemViewModel2);
        assertFalse(viewModel.validatePositive());
    }

    @Test
    public void shouldReturnIsAuthorized() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        assertFalse(viewModel.isAuthorized());


    }

    @Test
    public void shouldSetSignature() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));

        assertNull(viewModel.getRapidTestForm().getStatus());

        viewModel.addSignature("submit");
        assertEquals(Signature.TYPE.SUBMITTER, viewModel.getRapidTestForm().getSignaturesWrapper().get(0).getType());
        assertEquals("submit", viewModel.getRapidTestForm().getSignaturesWrapper().get(0).getSignature());

        viewModel.addSignature("authorize");
        assertEquals(Signature.TYPE.APPROVER, viewModel.getRapidTestForm().getSignaturesWrapper().get(1).getType());
        assertEquals("authorize", viewModel.getRapidTestForm().getSignaturesWrapper().get(1).getSignature());
        assertNotNull(viewModel.getRapidTestForm().getSubmittedTime());
    }

    @Test
    public void shouldUpdateTotal() throws Exception {
        viewModel = new RapidTestReportViewModel(Period.of(DateUtil.parseString("2016-09-11", DateUtil.DB_DATE_FORMAT)));
        viewModel.getItemViewModelList().get(0).getGridHIVDetermine().setConsumptionValue("100");
        viewModel.updateTotal(RapidTestFormGridViewModel.ColumnCode.HIVDetermine,consumption);
        assertEquals("100", viewModel.getItemTotal().getGridHIVDetermine().getConsumptionValue());

        viewModel.getItemViewModelList().get(1).getGridHIVDetermine().setConsumptionValue("2333");
        viewModel.updateTotal(RapidTestFormGridViewModel.ColumnCode.HIVDetermine, consumption);
        assertEquals("2433", viewModel.getItemTotal().getGridHIVDetermine().getConsumptionValue());
    }
}