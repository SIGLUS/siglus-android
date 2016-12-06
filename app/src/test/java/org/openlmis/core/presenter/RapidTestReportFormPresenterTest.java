package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.apache.commons.lang.StringUtils;
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
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RapidTestReportFormPresenterTest {

    @Mock
    ProgramDataFormRepository programDataFormRepositoryMock;

    @Mock
    ProgramRepository programRepositoryMock;

    @Mock
    RapidTestReportViewModel viewModelMock;

    RapidTestReportFormPresenter rapidTestReportFormPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        rapidTestReportFormPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RapidTestReportFormPresenter.class);
    }

    @Test
    public void shouldLoadViewModel() throws Exception {
        Period period = Period.of(DateUtil.parseString("2016-11-01", DateUtil.DB_DATE_FORMAT));

        //generate new view model
        TestSubscriber<RapidTestReportViewModel> subscriber1 = new TestSubscriber<>();
        Observable<RapidTestReportViewModel> observable1 = rapidTestReportFormPresenter.loadViewModel(0L, period.getBegin());
        observable1.subscribe(subscriber1);
        subscriber1.awaitTerminalEvent();

        verify(programDataFormRepositoryMock, never()).queryById(anyInt());
        subscriber1.assertNoErrors();
        RapidTestReportViewModel viewModel1 = subscriber1.getOnNextEvents().get(0);

        assertEquals(period.getBegin(), viewModel1.getPeriod().getBegin());
        assertEquals(RapidTestReportViewModel.Status.MISSING, viewModel1.getStatus());

        //convert db model to view model
        ProgramDataForm form = new ProgramDataForm();
        ProgramDataFormItem programDataFormItem1 = new ProgramDataFormItem();
        programDataFormItem1.setName(RapidTestReportViewModel.MOBILE_UNIT);
        programDataFormItem1.setProgramDataColumnCode("CONSUME_" + StringUtils.upperCase(RapidTestFormGridViewModel.ColumnCode.Syphillis.name()));
        programDataFormItem1.setValue(100);
        ProgramDataFormItem programDataFormItem2 = new ProgramDataFormItem();
        programDataFormItem2.setName(RapidTestReportViewModel.PNCTL);
        programDataFormItem2.setProgramDataColumnCode("POSITIVE_" + StringUtils.upperCase(RapidTestFormGridViewModel.ColumnCode.Syphillis.name()));
        programDataFormItem2.setValue(300);
        form.getProgramDataFormItemListWrapper().add(programDataFormItem1);
        form.getProgramDataFormItemListWrapper().add(programDataFormItem2);
        form.setStatus(ProgramDataForm.STATUS.DRAFT);
        form.setPeriodBegin(period.getBegin().toDate());
        form.setPeriodEnd(period.getEnd().toDate());
        when(programDataFormRepositoryMock.queryById(anyInt())).thenReturn(form);
        TestSubscriber<RapidTestReportViewModel> subscriber2 = new TestSubscriber<>();
        Observable<RapidTestReportViewModel> observable2 = rapidTestReportFormPresenter.loadViewModel(1L, period.getBegin());
        observable2.subscribe(subscriber2);
        subscriber2.awaitTerminalEvent();

        verify(programDataFormRepositoryMock).queryById(1L);
        subscriber2.assertNoErrors();
        RapidTestReportViewModel viewModel2 = subscriber2.getOnNextEvents().get(0);

        assertEquals(viewModel2.getItemViewModelMap().get(RapidTestReportViewModel.MOBILE_UNIT).getGridSyphillis().getConsumptionValue(), "100");
        assertEquals(viewModel2.getItemViewModelMap().get(RapidTestReportViewModel.PNCTL).getGridSyphillis().getPositiveValue(), "300");
        assertEquals(RapidTestReportViewModel.Status.INCOMPLETE, viewModel2.getStatus());
    }

    @Test
    public void shouldSaveDraftForm() throws Exception {
        rapidTestReportFormPresenter.viewModel = viewModelMock;
        ProgramDataForm dataForm = new ProgramDataForm();
        when(programRepositoryMock.queryByCode(Constants.RAPID_TEST_CODE)).thenReturn(new Program());
        when(viewModelMock.getRapidTestForm()).thenReturn(dataForm);

        TestSubscriber<RapidTestReportViewModel> subscriber = new TestSubscriber<>();
        Observable<RapidTestReportViewModel> observable = rapidTestReportFormPresenter.saveDraftForm();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        verify(viewModelMock).convertFormViewModelToDataModel(any(Program.class));
        verify(programDataFormRepositoryMock).batchCreateOrUpdate(dataForm);
        subscriber.assertNoErrors();
    }

    private class MyTestModule extends AbstractModule {
        @Override
        public void configure() {
            bind(ProgramDataFormRepository.class).toInstance(programDataFormRepositoryMock);
            bind(ProgramRepository.class).toInstance(programRepositoryMock);
        }
    }
}