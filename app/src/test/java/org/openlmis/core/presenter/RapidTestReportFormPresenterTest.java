package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.TestConsumptionLineItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.model.builder.ProgramDataColumnBuilder;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class RapidTestReportFormPresenterTest {

  @Mock
  ProgramRepository programRepositoryMock;

  @Mock
  RnrFormRepository rnrFormRepositoryMock;

  @Mock
  RapidTestReportViewModel viewModelMock;

  RapidTestReportFormPresenter rapidTestReportFormPresenter;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    rapidTestReportFormPresenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RapidTestReportFormPresenter.class);
  }

  @Test
  public void shouldLoadViewModel() throws Exception {
    Period period = Period.of(DateUtil.parseString("2016-11-01", DateUtil.DB_DATE_FORMAT));

    //generate new view model
    TestSubscriber<RapidTestReportViewModel> subscriber1 = new TestSubscriber<>();
    rapidTestReportFormPresenter.loadData(0L, period.getEnd().toDate());

    verify(rnrFormRepositoryMock, never()).queryRnRForm(anyInt());
    subscriber1.assertNoErrors();
    RapidTestReportViewModel viewModel1 = subscriber1.getOnNextEvents().get(0);

    assertEquals(period.getBegin(), viewModel1.getPeriod().getBegin());
    assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel1.getStatus());

    //convert db model to view model
    RnRForm form = new RnRForm();
    TestConsumptionLineItem programDataFormItem1 = new TestConsumptionLineItem();
    programDataFormItem1.setService("MOBILE_UNIT");
    programDataFormItem1.setUsageColumnsMap(new UsageColumnsMap().builder()
        .code("CONSUME_" + RapidTestFormGridViewModel.ColumnCode.SYPHILLIS.name()).build());
    programDataFormItem1.setValue(100);
    TestConsumptionLineItem programDataFormItem2 = new TestConsumptionLineItem();
    programDataFormItem2.setService("PNCTL");
    programDataFormItem2.setUsageColumnsMap(new UsageColumnsMap().builder()
        .code("POSITIVE_" + RapidTestFormGridViewModel.ColumnCode.SYPHILLIS.name()).build());
    programDataFormItem2.setValue(300);
    form.getTestConsumptionLinesWrapper().add(programDataFormItem1);
    form.getTestConsumptionLinesWrapper().add(programDataFormItem2);
    form.setStatus(Status.DRAFT);
    form.setPeriodBegin(period.getBegin().toDate());
    form.setPeriodEnd(period.getEnd().toDate());
    when(rnrFormRepositoryMock.queryRnRForm(anyInt())).thenReturn(form);
    TestSubscriber<RapidTestReportViewModel> subscriber2 = new TestSubscriber<>();
    rapidTestReportFormPresenter.viewModel = null;
   rapidTestReportFormPresenter.loadData(1L, period.getEnd().toDate());

    verify(rnrFormRepositoryMock).queryById(1L);
    subscriber2.assertNoErrors();
    RapidTestReportViewModel viewModel2 = subscriber2.getOnNextEvents().get(0);

    assertEquals("100", viewModel2.getItemViewModelMap().get("MOBILE_UNIT").getGridSyphillis().getConsumptionValue());
    assertEquals("300", viewModel2.getItemViewModelMap().get("PNCTL").getGridSyphillis().getPositiveValue());
    assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel2.getStatus());
  }

  @Test
  public void shouldSaveDraftForm() throws Exception {
    rapidTestReportFormPresenter.viewModel = viewModelMock;
    RnRForm dataForm = new RnRForm();
    when(programRepositoryMock.queryByCode(Constants.RAPID_TEST_PROGRAM_CODE)).thenReturn(new Program());
    when(viewModelMock.getRapidTestForm()).thenReturn(dataForm);

    rapidTestReportFormPresenter.createOrUpdateRapidTest();

    verify(viewModelMock).convertFormViewModelToDataModel(any(Program.class));
    verify(r).batchCreateOrUpdate(dataForm);
  }

  @Test
  public void shouldDeleteDraftForm() throws Exception {
    rapidTestReportFormPresenter.viewModel = new RapidTestReportViewModel(
        Period.of(DateUtil.parseString("2015-09-12", DateUtil.DB_DATE_FORMAT)));
    rapidTestReportFormPresenter.deleteDraft();
    verify(programDataFormRepositoryMock, never()).delete(any(RnRForm.class));

    RnRForm rapidTestForm = new RnRForm();
    rapidTestForm.setStatus(Status.DRAFT);
    rapidTestForm.setId(1L);
    rapidTestReportFormPresenter.viewModel.setRapidTestForm(rapidTestForm);
    rapidTestReportFormPresenter.deleteDraft();
    verify(programDataFormRepositoryMock).delete(any(RnRForm.class));
  }

  private class MyTestModule extends AbstractModule {

    @Override
    public void configure() {
      bind(ProgramDataFormRepository.class).toInstance(programDataFormRepositoryMock);
      bind(ProgramRepository.class).toInstance(programRepositoryMock);
    }
  }
}