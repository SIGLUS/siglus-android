package org.openlmis.core.view.viewmodel;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.Signature;
import org.openlmis.core.model.builder.ProgramDataColumnBuilder;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

@RunWith(LMISTestRunner.class)
public class RapidTestReportViewModelTest {

  public static final String PROGRAM_NAME = "name";
  public static final String PERIOD = "2016-09-11";
  RapidTestReportViewModel viewModel;

  @Test
  public void shouldConvertToDataModel() throws Exception {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));
    RapidTestFormItemViewModel itemViewModel = new RapidTestFormItemViewModel(
        new MovementReasonManager.MovementReason(
            MovementReasonManager.MovementType.ISSUE,
            "TOTAL", "Total"));
    List<RapidTestFormItemViewModel> itemViewModelList = Arrays.asList(itemViewModel);
    viewModel.setItemViewModelList(itemViewModelList);

    Program program = new Program(Constants.RAPID_TEST_CODE, PROGRAM_NAME, "", false, null, null);
    viewModel.convertFormViewModelToDataModel(program);
    assertFalse(this.viewModel.isSynced());
    assertFalse(this.viewModel.isSubmitted());
    assertTrue(this.viewModel.isEditable());
    assertTrue(this.viewModel.isDraft());
    assertNull(this.viewModel.getSyncedTime());
    assertEquals(0, this.viewModel.getStatus().getViewType());
    assertTrue(this.viewModel.validateAPES());
    assertTrue(this.viewModel.validateUnjustified());
    assertFalse(this.viewModel.validateOnlyAPES());
    assertTrue(this.viewModel.isFormEmpty());
    assertEquals(DateUtil.parseString("2016-08-21", DateUtil.DB_DATE_FORMAT),
        this.viewModel.getRapidTestForm().getPeriodBegin());
    assertEquals(DateUtil.parseString("2016-09-20", DateUtil.DB_DATE_FORMAT),
        this.viewModel.getRapidTestForm().getPeriodEnd());
  }


  @Test
  public void shouldConvertToDataModule1() {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString("2019-09-11", DateUtil.DB_DATE_FORMAT)),
        RapidTestReportViewModel.Status.INCOMPLETE);
    RapidTestFormItemViewModel itemViewModel = mock(RapidTestFormItemViewModel.class);
    List<RapidTestFormItemViewModel> itemViewModelList = Arrays.asList(itemViewModel);
    viewModel.setItemViewModelList(itemViewModelList);
    Program program = new Program(Constants.RAPID_TEST_CODE, PROGRAM_NAME, "", false, null, null);
    viewModel.convertFormViewModelToDataModel(program);
    verify(itemViewModel).convertToDataModel();
    assertNull(this.viewModel.getSyncedTime());
    assertTrue(this.viewModel.isDraft());
    assertFalse(this.viewModel.isSubmitted());
    assertTrue(this.viewModel.isEditable());
    assertFalse(this.viewModel.isSynced());
    assertTrue(this.viewModel.isFormEmpty());
    assertEquals(DateUtil.parseString("2019-08-21", DateUtil.DB_DATE_FORMAT),
        this.viewModel.getRapidTestForm().getPeriodBegin());
    assertEquals(DateUtil.parseString("2019-09-20", DateUtil.DB_DATE_FORMAT),
        this.viewModel.getRapidTestForm().getPeriodEnd());
  }

  @Test
  public void shouldSetRapidTestFormAndUpdateStatus() throws Exception {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));
    ProgramDataForm rapidTestForm = new ProgramDataForm();
    rapidTestForm.setStatus(Status.DRAFT);
    viewModel.setRapidTestForm(rapidTestForm);
    assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel.getStatus());
    assertTrue(viewModel.isEditable());

    rapidTestForm.setStatus(Status.SUBMITTED);
    viewModel.setRapidTestForm(rapidTestForm);
    assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel.getStatus());
    assertTrue(viewModel.isEditable());

    rapidTestForm.setStatus(Status.AUTHORIZED);
    viewModel.setRapidTestForm(rapidTestForm);
    assertEquals(RapidTestReportViewModel.Status.COMPLETED, viewModel.getStatus());
    assertFalse(viewModel.isEditable());

    rapidTestForm.setStatus(Status.AUTHORIZED);
    rapidTestForm.setSynced(true);
    viewModel.setRapidTestForm(rapidTestForm);
    assertEquals(RapidTestReportViewModel.Status.SYNCED, viewModel.getStatus());
    assertFalse(viewModel.isEditable());
  }

  @Test
  public void shouldConvertViewModelAndUpdateDataModel() throws Exception {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));
    MovementReasonManager.MovementReason reason1 = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, "ACC_EMERGENCY", "Acc emergency");

    RapidTestFormItemViewModel itemViewModel = new RapidTestFormItemViewModel(reason1);
    RapidTestFormGridViewModel gridViewModel = new RapidTestFormGridViewModel(
        RapidTestFormGridViewModel.ColumnCode.HIVDETERMINE);
    gridViewModel.setPositiveValue("100");
    gridViewModel.setConsumptionValue("1200");
    gridViewModel.setUnjustifiedValue("110");
    itemViewModel.setRapidTestFormGridViewModelList(newArrayList(gridViewModel));
    List<RapidTestFormItemViewModel> itemViewModelList = newArrayList(itemViewModel);
    this.viewModel.setItemViewModelList(itemViewModelList);

    ProgramDataForm rapidTestForm = new ProgramDataForm();
    rapidTestForm.setStatus(Status.DRAFT);
    rapidTestForm.setPeriodBegin(this.viewModel.getPeriod().getBegin().toDate());
    rapidTestForm.setPeriodEnd(this.viewModel.getPeriod().getEnd().toDate());
    this.viewModel.setRapidTestForm(rapidTestForm);
    this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().add(
        new ProgramDataFormItem(PROGRAM_NAME, new ProgramDataColumnBuilder().setCode("code").build(),
            100));

    Program program = new Program(Constants.RAPID_TEST_CODE, PROGRAM_NAME, "", false, null, null);
    this.viewModel.convertFormViewModelToDataModel(program);

    assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().size(), is(3));
    assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(0)
        .getProgramDataColumn().getCode(), is("CONSUME_HIVDETERMINE"));
    assertThat(
        this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(0).getForm(),
        is(viewModel.getRapidTestForm()));
    assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(1)
        .getProgramDataColumn().getCode(), is("POSITIVE_HIVDETERMINE"));
    assertThat(
        this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(1).getForm(),
        is(viewModel.getRapidTestForm()));
    assertThat(this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(2)
        .getProgramDataColumn().getCode(), is("UNJUSTIFIED_HIVDETERMINE"));
    assertThat(
        this.viewModel.getRapidTestForm().getProgramDataFormItemListWrapper().get(2).getForm(),
        is(viewModel.getRapidTestForm()));
  }

  @Test
  public void shouldValidateItemList() throws Exception {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));
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
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));
    assertFalse(viewModel.isAuthorized());


  }

  @Test
  public void shouldSetSignature() throws Exception {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));

    assertNull(viewModel.getRapidTestForm().getStatus());

    viewModel.addSignature("submit");
    assertEquals(Signature.TYPE.SUBMITTER,
        viewModel.getRapidTestForm().getSignaturesWrapper().get(0).getType());
    assertEquals("submit",
        viewModel.getRapidTestForm().getSignaturesWrapper().get(0).getSignature());

    viewModel.addSignature("authorize");
    assertEquals(Signature.TYPE.APPROVER,
        viewModel.getRapidTestForm().getSignaturesWrapper().get(1).getType());
    assertEquals("authorize",
        viewModel.getRapidTestForm().getSignaturesWrapper().get(1).getSignature());
    assertNotNull(viewModel.getRapidTestForm().getSubmittedTime());
  }

  @Test
  public void shouldUpdateTotal() throws Exception {
    viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString(PERIOD, DateUtil.DB_DATE_FORMAT)));
    viewModel.getItemViewModelList().get(0).getGridHIVDetermine().setConsumptionValue("100");
    viewModel.updateTotal(RapidTestFormGridViewModel.ColumnCode.HIVDETERMINE, consumption);
    assertEquals("100", viewModel.getItemTotal().getGridHIVDetermine().getConsumptionValue());

    viewModel.getItemViewModelList().get(1).getGridHIVDetermine().setConsumptionValue("2333");
    viewModel.updateTotal(RapidTestFormGridViewModel.ColumnCode.HIVDETERMINE, consumption);
    assertEquals("2433", viewModel.getItemTotal().getGridHIVDetermine().getConsumptionValue());
  }
}