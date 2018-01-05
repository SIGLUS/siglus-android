package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.builders.PTVProgramBuilder;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.PTVProgramRepository;
import org.openlmis.core.utils.PTVUtil;
import org.openlmis.core.utils.mapper.PTVProgramToPTVViewModelMapper;
import org.openlmis.core.utils.mapper.PTVViewModelToPTVProgramMapper;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PtvProgramPresenterTest {

    private PtvProgramPresenter ptvProgramPresenter;
    private PTVProgramBuilder ptvProgramBuilder;
    private PTVProgramRepository ptvProgramRepository;
    private PTVProgramToPTVViewModelMapper ptvProgramToPTVViewModelMapper;
    private PTVViewModelToPTVProgramMapper ptvViewModelToPTVProgramMapper;
    private Period period;
    private PTVProgram ptvProgram;
    private TestSubscriber<PTVProgram> subscriber;
    private long[] quantities;
    private List<PTVViewModel> ptvViewModels;

    @Before
    public void setUp() throws Exception {
        period = new Period(DateTime.now());
        quantities = PTVUtil.getRandomQuantitiesForPTVViewModels();
        ptvViewModels = PTVUtil.getPtvViewModels(quantities);
        ptvProgram = PTVUtil.createDummyPTVProgram(period);
        subscriber = new TestSubscriber<>();
        ptvProgramBuilder = mock(PTVProgramBuilder.class);
        ptvProgramRepository = mock(PTVProgramRepository.class);
        ptvProgramToPTVViewModelMapper = mock(PTVProgramToPTVViewModelMapper.class);
        ptvViewModelToPTVProgramMapper = mock(PTVViewModelToPTVProgramMapper.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PtvProgramPresenterTest.MyTestModule());
        ptvProgramPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PtvProgramPresenter.class);
        ptvProgramPresenter.setPeriod(period);
    }

    @Test
    public void shouldCreatePTVProgramForCurrentPeriodWhenPTVProgramIsNotSaved() throws LMISException {
        Period period = new Period(DateTime.now());
        PTVProgram expectedPTVProgram = PTVUtil.createDummyPTVProgram(period);
        expectedPTVProgram.setPatientDispensations(PTVUtil.createDummyPatientDispensations());
        expectedPTVProgram.setPtvProgramStocksInformation(PTVUtil.createDummyPTVProgramStocksInformation());
        when(ptvProgramBuilder.buildInitialPTVProgram(period)).thenReturn(expectedPTVProgram);
        ptvProgramPresenter.setPeriod(period);

        Observable<PTVProgram> observable = ptvProgramPresenter.buildInitialPtvProgram();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);

        assertThat(expectedPTVProgram, is(actualPTVProgram));
    }

    @Test
    public void shouldThrowAnExceptionWhenPeriodIsNotSetUp() throws Exception {
        ptvProgramPresenter.setPeriod(null);
        Observable<PTVProgram> observable = ptvProgramPresenter.buildInitialPtvProgram();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(new LMISException("Period cannot be null"));
    }

    @Test
    public void shouldUpdatePTVProgramWhenThereIsAPTVProgramStored() throws LMISException, SQLException {
        PTVProgram expectedPtvProgram = PTVUtil.createDummyPTVProgram(period);
        expectedPtvProgram.setStatus(ViaReportStatus.DRAFT);
        buildExistentPTVProgram(expectedPtvProgram);

        when(ptvProgramRepository.save(expectedPtvProgram)).thenReturn(expectedPtvProgram);
        Observable<PTVProgram> observableSave = ptvProgramPresenter.savePTVProgram(false);
        observableSave.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);
        assertThat(actualPTVProgram, is(expectedPtvProgram));
        assertThat(actualPTVProgram.getStatus(), is(ViaReportStatus.DRAFT));
    }

    @Test
    public void shouldSavePTVProgramWhenThereIsNotPTVProgramStored() throws LMISException, SQLException {
        PTVProgram expectedPtvProgram = createInitialPTVProgram();

        when(ptvProgramRepository.save(expectedPtvProgram)).thenReturn(expectedPtvProgram);
        Observable<PTVProgram> observableSave = ptvProgramPresenter.savePTVProgram(false);
        observableSave.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);
        assertThat(actualPTVProgram, is(expectedPtvProgram));
    }

    @Test
    public void shouldSetSubmittedStatusWhenPTVProgramIsCompleted() throws LMISException, SQLException {
        PTVProgram expectedPtvProgram = PTVUtil.createDummyPTVProgram(period);
        expectedPtvProgram.setStatus(ViaReportStatus.SUBMITTED);
        buildExistentPTVProgram(expectedPtvProgram);

        when(ptvProgramRepository.save(expectedPtvProgram)).thenReturn(expectedPtvProgram);
        Observable<PTVProgram> observableSave = ptvProgramPresenter.savePTVProgram(true);
        observableSave.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);
        assertThat(actualPTVProgram, is(expectedPtvProgram));
        assertThat(actualPTVProgram.getStatus(), is(ViaReportStatus.SUBMITTED));
    }

    @Test
    public void shouldUpdatePTVProgramFromPTVViewModelsAndChildAndWomenNumber() throws LMISException {
        ptvProgram = createInitialPTVProgram();
        ptvProgram.setPatientDispensations(PTVUtil.createDummyPatientDispensations());
        when(ptvViewModelToPTVProgramMapper.convertToPTVProgram(ptvViewModels, ptvProgram)).thenReturn(ptvProgram);
        String numberOfChild = String.valueOf(1 + new Random().nextInt(99));
        String numberOfWomen = String.valueOf(1 + new Random().nextInt(99));

        PTVProgram actualPTVProgram = ptvProgramPresenter.updatePTVProgram(numberOfWomen, numberOfChild);

        assertWomanAndChildQuantitiesAreSetUp(numberOfChild, numberOfWomen, actualPTVProgram);
    }

    @Test
    public void shouldThrowLMISExceptionWhenPTVProgramIsNull() throws Exception {
        Observable<PTVProgram> observable = ptvProgramPresenter.savePTVProgram(false);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(new LMISException("PTV Program cannot be null"));
    }

    @Test
    public void shouldThrowLMISExceptionWhenSavePTVProgramWithErrors() throws Exception {
        buildExistentPTVProgram(ptvProgram);

        subscriber = new TestSubscriber<>();
        doThrow(new LMISException("Cannot create PTV program")).when(ptvProgramRepository).save(ptvProgram);
        Observable<PTVProgram> observable = ptvProgramPresenter.savePTVProgram(false);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(new LMISException("Cannot create PTV program"));
    }

    @Test
    public void shouldThrowSQLExceptionWhenSavePTVProgramWithErrors() throws Exception {
        buildExistentPTVProgram(ptvProgram);

        subscriber = new TestSubscriber<>();
        doThrow(SQLException.class).when(ptvProgramRepository).save(ptvProgram);
        Observable<PTVProgram> observable = ptvProgramPresenter.savePTVProgram(false);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(SQLException.class);
    }

    @Test
    public void shouldReturnTrueWhenPTVProgramDoesNotHaveCreatedByFieldFilled() throws LMISException {
        ptvProgram = createInitialPTVProgram();

        boolean isNotSubmittedForApproval = ptvProgramPresenter.isNotSubmittedForApproval();

        assertThat(true, is(isNotSubmittedForApproval));
    }

    @Test
    public void shouldReturnTrueWhenStatusIsMissingOrDraftAndCreatedByFieldIsNotFilled() throws LMISException {
        ptvProgram = createInitialPTVProgram();
        ptvProgram.setStatus(ViaReportStatus.DRAFT);

        boolean isNotSubmittedForApproval = ptvProgramPresenter.isNotSubmittedForApproval();

        assertThat(true, is(isNotSubmittedForApproval));
    }

    @Test
    public void shouldReturnFalseWhenStatusIsNotMissingOrDraft() throws LMISException {
        ptvProgram = createInitialPTVProgram();
        ptvProgram.setStatus(ViaReportStatus.SUBMITTED);

        boolean isNotSubmittedForApproval = ptvProgramPresenter.isNotSubmittedForApproval();

        assertThat(false, is(isNotSubmittedForApproval));
    }

    @Test
    public void shouldReturnFalseWhenCreatedByFieldIsNotEmpty() throws LMISException {
        ptvProgram = createInitialPTVProgram();
        ptvProgram.setCreatedBy("TWUIO");

        boolean isNotSubmittedForApproval = ptvProgramPresenter.isNotSubmittedForApproval();

        assertThat(false, is(isNotSubmittedForApproval));
    }

    @Test
    public void shouldCallSavePTVProgram() throws LMISException, SQLException {
        ptvProgram = createInitialPTVProgram();
        PTVProgram resultPTVProgram = PTVUtil.createDummyPTVProgram(period);
        when(ptvProgramRepository.save(ptvProgram)).thenReturn(resultPTVProgram);
        Observable<PTVProgram> observableSave = ptvProgramPresenter.savePTVProgram(true);
        observableSave.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        ptvProgramPresenter.signReport("TEST", true);

        assertThat(ptvProgram.getCreatedBy(), is("TEST"));
    }


    private PTVProgram createInitialPTVProgram() throws LMISException {
        PTVProgram expectedPtvProgram = PTVUtil.createDummyPTVProgram(period);
        when(ptvProgramRepository.getByPeriod(period)).thenReturn(null);
        when(ptvProgramBuilder.buildInitialPTVProgram(period)).thenReturn(expectedPtvProgram);
        when(ptvProgramToPTVViewModelMapper.buildPlaceholderRows(ptvProgram)).thenReturn(ptvViewModels);
        Observable<PTVProgram> observableBuild = ptvProgramPresenter.buildInitialPtvProgram();
        observableBuild.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        return expectedPtvProgram;
    }

    private void buildExistentPTVProgram(PTVProgram ptvProgram) throws LMISException {
        when(ptvProgramRepository.getByPeriod(period)).thenReturn(ptvProgram);
        when(ptvProgramBuilder.buildExistentPTVProgram(ptvProgram)).thenReturn(ptvProgram);
        Observable<PTVProgram> observableBuild = ptvProgramPresenter.buildInitialPtvProgram();
        observableBuild.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
    }


    private void assertWomanAndChildQuantitiesAreSetUp(String numberOfChild, String numberOfWomen, PTVProgram actualPTVProgram) {
        List<PatientDispensation> patientDispensations = new ArrayList<>(actualPTVProgram.getPatientDispensations());
        for (PatientDispensation patientDispensation : patientDispensations) {
            if (patientDispensation.getType().equals(PatientDispensation.Type.WOMAN)) {
                assertThat(patientDispensation.getTotal(), is(Long.parseLong(numberOfWomen)));
            }
            if (patientDispensation.getType().equals(PatientDispensation.Type.CHILD)) {
                assertThat(patientDispensation.getTotal(), is(Long.parseLong(numberOfChild)));
            }
        }
    }

    public class MyTestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(PTVProgramBuilder.class).toInstance(ptvProgramBuilder);
            bind(PTVProgramRepository.class).toInstance(ptvProgramRepository);
            bind(PTVProgramToPTVViewModelMapper.class).toInstance(ptvProgramToPTVViewModelMapper);
            bind(PTVViewModelToPTVProgramMapper.class).toInstance(ptvViewModelToPTVProgramMapper);
        }
    }
}
