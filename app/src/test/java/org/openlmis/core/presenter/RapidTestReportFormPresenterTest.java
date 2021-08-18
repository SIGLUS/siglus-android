package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class RapidTestReportFormPresenterTest {

  @Mock
  ProgramRepository programRepositoryMock;

  @Mock
  RnrFormRepository rnrFormRepositoryMock;

  @Mock
  RapidTestReportFormPresenter.RapidTestReportView view;

  @Mock
  RapidTestReportViewModel viewModelMock;

  @InjectMocks
  RapidTestReportFormPresenter rapidTestReportFormPresenter;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Ignore
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
    TestConsumptionItem programDataFormItem1 = new TestConsumptionItem();
    programDataFormItem1.setService("MOBILE_UNIT");
    programDataFormItem1.setUsageColumnsMap(new UsageColumnsMap().builder()
        .code("CONSUME_" + RapidTestFormGridViewModel.ColumnCode.SYPHILLIS.name()).build());
    programDataFormItem1.setValue(100);
    TestConsumptionItem programDataFormItem2 = new TestConsumptionItem();
    programDataFormItem2.setService("PNCTL");
    programDataFormItem2.setUsageColumnsMap(new UsageColumnsMap().builder()
        .code("POSITIVE_" + RapidTestFormGridViewModel.ColumnCode.SYPHILLIS.name()).build());
    programDataFormItem2.setValue(300);
    form.getTestConsumptionItemListWrapper().add(programDataFormItem1);
    form.getTestConsumptionItemListWrapper().add(programDataFormItem2);
    form.setStatus(Status.DRAFT);
    form.setPeriodBegin(period.getBegin().toDate());
    form.setPeriodEnd(period.getEnd().toDate());
    when(rnrFormRepositoryMock.queryRnRForm(anyInt())).thenReturn(form);
    TestSubscriber<RapidTestReportViewModel> subscriber2 = new TestSubscriber<>();
    rapidTestReportFormPresenter.viewModel = null;
   rapidTestReportFormPresenter.loadData(1L, period.getEnd().toDate());

    verify(rnrFormRepositoryMock).queryRnRForm(1L);
    subscriber2.assertNoErrors();
    RapidTestReportViewModel viewModel2 = subscriber2.getOnNextEvents().get(0);

    assertEquals("100", viewModel2.getItemViewModelMap().get("MOBILE_UNIT").getGridSyphillis().getConsumptionValue());
    assertEquals("300", viewModel2.getItemViewModelMap().get("PNCTL").getGridSyphillis().getPositiveValue());
    assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel2.getStatus());
  }

  @Test
  public void shouldSaveDraftForm() throws Exception {
    // given
    RnRForm dataForm = new RnRForm();
    when(programRepositoryMock.queryByCode(Constants.RAPID_TEST_PROGRAM_CODE)).thenReturn(new Program());
    when(viewModelMock.getRapidTestForm()).thenReturn(dataForm);

    // when
    TestSubscriber<RapidTestReportViewModel> subscriber = new TestSubscriber<>();
    rapidTestReportFormPresenter.createOrUpdateRapidTest().subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    // then
    verify(viewModelMock).convertFormViewModelToDataModel(any());
    verify(rnrFormRepositoryMock).createOrUpdateWithItems(dataForm);
  }

  @Test
  public void shouldDeleteDraftForm() throws Exception {
    // given
    RnRForm rapidTestForm = new RnRForm();
    rapidTestForm.setStatus(Status.DRAFT);
    rapidTestForm.setId(1L);
    rapidTestReportFormPresenter.viewModel.setRapidTestForm(rapidTestForm);

    // when
    rapidTestReportFormPresenter.deleteDraft();

    // then
    verify(rnrFormRepositoryMock).removeRnrForm(any(RnRForm.class));
  }

}