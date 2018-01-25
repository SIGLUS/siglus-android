package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.utils.mapper.MalariaDataReportViewModelToMalariaProgramMapper;
import org.openlmis.core.utils.mapper.MalariaProgramToMalariaDataReportViewModelMapper;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class MalariaDataReportFormPresenterTest {

    private PatientDataService patientDataService;
    private MalariaDataReportFormPresenter malariaDataReportFormPresenter;
    private MalariaProgramToMalariaDataReportViewModelMapper malariaProgramToMalariaDataReportViewModelMapper;
    private MalariaDataReportViewModelToMalariaProgramMapper malariaProgramMapper;
    private TestSubscriber<List<ImplementationReportViewModel>> listImplementationViewModelSubscriber;
    private TestSubscriber<MalariaProgram> malariaProgramTestSubscriber;
    private Period period;
    private MalariaProgram malariaProgram;
    private MalariaDataReportViewModel malariaDataReportViewModel;
    private ImplementationReportViewModel usImplementationReportViewModel;
    private ImplementationReportViewModel apeImplementationReportViewModel;
    private String sign;
    private List<Long> stocks;
    private int numberOfMalariaReportRows;

    @Before
    public void init() {
        usImplementationReportViewModel = getImplementationViewModel(ImplementationReportType.US);
        apeImplementationReportViewModel = getImplementationViewModel(ImplementationReportType.APE);
        stocks = generateFourRandomLongsList();
        patientDataService = mock(PatientDataService.class);
        malariaProgramToMalariaDataReportViewModelMapper = mock(MalariaProgramToMalariaDataReportViewModelMapper.class);
        malariaProgramMapper = mock(MalariaDataReportViewModelToMalariaProgramMapper.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MalariaDataReportFormPresenterTest.MyTestModule());
        malariaDataReportFormPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MalariaDataReportFormPresenter.class);
        listImplementationViewModelSubscriber = new TestSubscriber<>();
        malariaProgramTestSubscriber = new TestSubscriber<>();
        period = new Period(DateTime.now());
        sign = "TWUIO";
        numberOfMalariaReportRows = 3;
    }

    @Test
    public void shouldReturnViewModelsWhenMalariaProgramIsNull() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        List<ImplementationReportViewModel> implementationReportViewModels = initializeImplementationReportViewModelsWhenMalariaProgramIsNull();
        ImplementationReportViewModel implementationUSReportViewModel = implementationReportViewModels.get(0);
        assertThat(implementationReportViewModels.size(), is(numberOfMalariaReportRows));
        assertUSImplementationStocks(implementationUSReportViewModel);
    }


    @Test
    public void shouldReturnViewModelsFromMalariaProgram() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        List<ImplementationReportViewModel> implementationReportViewModels = initializeImplementationReportViewModels();
        assertThat(implementationReportViewModels.size(), is(numberOfMalariaReportRows));
        assertThat(implementationReportViewModels.get(2).getExistingStock6x1(), is(usImplementationReportViewModel.getExistingStock6x1() + apeImplementationReportViewModel.getExistingStock6x1()));
        assertThat(implementationReportViewModels.get(2).getCurrentTreatment6x3(), is(usImplementationReportViewModel.getCurrentTreatment6x3() + apeImplementationReportViewModel.getCurrentTreatment6x3()));
    }

    @Test
    public void shouldThrowAnExceptionWhenFindForPeriodFails() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        doThrow(LMISException.class).when(patientDataService).findForPeriod(period.getBegin(), period.getEnd());

        Observable<List<ImplementationReportViewModel>> observable = malariaDataReportFormPresenter.getImplementationViewModelsForCurrentMalariaProgram(period);
        observable.subscribe(listImplementationViewModelSubscriber);
        listImplementationViewModelSubscriber.awaitTerminalEvent();

        listImplementationViewModelSubscriber.assertError(LMISException.class);
    }

    @Test
    public void shouldSaveMalariaProgramWithCorrectStatusAndSignWhenStatusIsSubmitted() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        initializeImplementationReportViewModels();
        when(patientDataService.save(malariaProgram)).thenReturn(Optional.of(malariaProgram));
        MalariaDataReportViewModel malariaDataReportViewModel = anyObject();
        when(malariaProgramMapper.map(malariaDataReportViewModel, eq(malariaProgram))).thenReturn(malariaProgram);

        Observable<MalariaProgram> observable = malariaDataReportFormPresenter.onSaveForm(ViaReportStatus.SUBMITTED, sign);
        observable.subscribe(malariaProgramTestSubscriber);
        malariaProgramTestSubscriber.awaitTerminalEvent();
        malariaProgramTestSubscriber.assertNoErrors();
        MalariaProgram actualMalariaProgram = malariaProgramTestSubscriber.getOnNextEvents().get(0);

        assertThat(actualMalariaProgram.getStatus(), is(ViaReportStatus.SUBMITTED));
        assertThat(actualMalariaProgram.getVerifiedBy(), is(sign));
    }

    @Test
    public void shouldSaveMalariaProgramWithCorrectStatusAndSignWhenStatusIsNotSubmitted() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        initializeImplementationReportViewModels();
        when(patientDataService.save(malariaProgram)).thenReturn(Optional.of(malariaProgram));
        MalariaDataReportViewModel malariaDataReportViewModel = anyObject();
        when(malariaProgramMapper.map(malariaDataReportViewModel, eq(malariaProgram))).thenReturn(malariaProgram);

        Observable<MalariaProgram> observable = malariaDataReportFormPresenter.onSaveForm(ViaReportStatus.MISSING, sign);
        observable.subscribe(malariaProgramTestSubscriber);
        malariaProgramTestSubscriber.awaitTerminalEvent();
        malariaProgramTestSubscriber.assertNoErrors();
        MalariaProgram actualMalariaProgram = malariaProgramTestSubscriber.getOnNextEvents().get(0);

        assertThat(actualMalariaProgram.getStatus(), is(ViaReportStatus.MISSING));
        assertThat(actualMalariaProgram.getCreatedBy(), is(sign));
    }

    @Test
    public void shouldCallMapMethodFromMalariaDataReportViewModelToMalariaProgramMapper() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        initializeImplementationReportViewModels();
        malariaDataReportFormPresenter.getMalariaProgram();

        verify(malariaProgramMapper, times(1)).map((MalariaDataReportViewModel) anyObject(), (MalariaProgram) anyObject());
    }

    @Test
    public void shouldThrowAnExceptionWhenOnSaveForm() throws LMISException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        initializeImplementationReportViewModels();
        MalariaDataReportViewModel malariaDataReportViewModel = anyObject();
        when(malariaProgramMapper.map(malariaDataReportViewModel, eq(malariaProgram))).thenReturn(malariaProgram);
        doThrow(LMISException.class).when(patientDataService).save(malariaProgram);

        Observable<MalariaProgram> observable = malariaDataReportFormPresenter.onSaveForm(ViaReportStatus.MISSING, sign);
        observable.subscribe(malariaProgramTestSubscriber);
        malariaProgramTestSubscriber.awaitTerminalEvent();

        malariaProgramTestSubscriber.assertError(LMISException.class);
    }

    @Test
    public void shouldReplaceViewModelSuppliedInMalariaProgramViewModelList() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        initializeImplementationReportViewModels();
        ImplementationReportViewModel implementationReportViewModel = getImplementationViewModel(ImplementationReportType.US);

        List<ImplementationReportViewModel> implementationReportViewModels = malariaDataReportFormPresenter.regenerateImplementationModels(implementationReportViewModel);
        assertThat(implementationReportViewModels.size(), is(numberOfMalariaReportRows));
        assertThat(implementationReportViewModels.get(2).getExistingStock6x1(), is(implementationReportViewModel.getExistingStock6x1() + apeImplementationReportViewModel.getExistingStock6x1()));
        assertThat(implementationReportViewModels.get(2).getCurrentTreatment6x3(), is(implementationReportViewModel.getCurrentTreatment6x3() + apeImplementationReportViewModel.getCurrentTreatment6x3()));
    }

    @Test
    public void shouldReturnTrueWhenMalariaProgramHasMissingStatus() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        initializeImplementationReportViewModelsWhenMalariaProgramIsNull();

        boolean isSubmittedForApproval = malariaDataReportFormPresenter.isSubmittedForApproval();

        assertThat(isSubmittedForApproval, is(true));
    }

    @Test
    public void shouldReturnFalseWhenMalariaProgramHasSubmittedStatus() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        initializeImplementationReportViewModels();

        boolean isSubmittedForApproval = malariaDataReportFormPresenter.isSubmittedForApproval();

        assertThat(isSubmittedForApproval, is(false));
    }

    private List<ImplementationReportViewModel> initializeImplementationReportViewModelsWhenMalariaProgramIsNull() throws LMISException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        malariaProgram = null;
        malariaDataReportViewModel = new MalariaDataReportViewModel();
        when(patientDataService.findForPeriod(period.getBegin(), period.getEnd())).thenReturn(malariaProgram);
        when(patientDataService.getMalariaProductsStockHand()).thenReturn(stocks);
        when(malariaProgramToMalariaDataReportViewModelMapper.Map(malariaProgram)).thenReturn(malariaDataReportViewModel);

        Observable<List<ImplementationReportViewModel>> observable = malariaDataReportFormPresenter.getImplementationViewModelsForCurrentMalariaProgram(period);
        observable.subscribe(listImplementationViewModelSubscriber);
        listImplementationViewModelSubscriber.awaitTerminalEvent();

        listImplementationViewModelSubscriber.assertNoErrors();
        return listImplementationViewModelSubscriber.getOnNextEvents().get(0);
    }

    private List<ImplementationReportViewModel> initializeImplementationReportViewModels() throws LMISException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        malariaProgram = new MalariaProgram("", DateTime.now(), period.getBegin(), period.getEnd(), new ArrayList<Implementation>());
        malariaProgram.setStatus(ViaReportStatus.SUBMITTED);
        malariaProgram.setCreatedBy(sign);
        malariaDataReportViewModel = new MalariaDataReportViewModel(DateTime.now(), DateTime.now(), DateTime.now(), usImplementationReportViewModel, apeImplementationReportViewModel);
        when(patientDataService.findForPeriod(period.getBegin(), period.getEnd())).thenReturn(malariaProgram);
        when(malariaProgramToMalariaDataReportViewModelMapper.Map(malariaProgram)).thenReturn(malariaDataReportViewModel);

        Observable<List<ImplementationReportViewModel>> observable = malariaDataReportFormPresenter.getImplementationViewModelsForCurrentMalariaProgram(period);
        observable.subscribe(listImplementationViewModelSubscriber);
        listImplementationViewModelSubscriber.awaitTerminalEvent();

        listImplementationViewModelSubscriber.assertNoErrors();
        return listImplementationViewModelSubscriber.getOnNextEvents().get(0);
    }

    @NonNull
    private ImplementationReportViewModel getImplementationViewModel(ImplementationReportType us) {
        return new ImplementationReportViewModel(us, getRandomIntBetweenOneAndTen()
                , getRandomIntBetweenOneAndTen(), getRandomIntBetweenOneAndTen(), getRandomIntBetweenOneAndTen(), getRandomIntBetweenOneAndTen(),
                getRandomIntBetweenOneAndTen(), getRandomIntBetweenOneAndTen(), getRandomIntBetweenOneAndTen());
    }

    private int getRandomIntBetweenOneAndTen() {
        return 1 + new Random().nextInt(10);
    }

    private void assertUSImplementationStocks(ImplementationReportViewModel implementationReportViewModel) {
        assertThat(implementationReportViewModel.getExistingStock6x1(), is(stocks.get(0)));
        assertThat(implementationReportViewModel.getExistingStock6x2(), is(stocks.get(1)));
        assertThat(implementationReportViewModel.getExistingStock6x3(), is(stocks.get(2)));
        assertThat(implementationReportViewModel.getExistingStock6x4(), is(stocks.get(3)));
    }

    @NonNull
    private ArrayList<Long> generateFourRandomLongsList() {
        return newArrayList((long) getRandomIntBetweenOneAndTen(), (long) getRandomIntBetweenOneAndTen(), (long) getRandomIntBetweenOneAndTen(), (long) getRandomIntBetweenOneAndTen());
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PatientDataService.class).toInstance(patientDataService);
            bind(MalariaProgramToMalariaDataReportViewModelMapper.class).toInstance(malariaProgramToMalariaDataReportViewModelMapper);
            bind(MalariaDataReportViewModelToMalariaProgramMapper.class).toInstance(malariaProgramMapper);
        }
    }

}