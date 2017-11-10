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
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.PTVProgramRepository;
import org.openlmis.core.utils.PTVUtil;
import org.openlmis.core.utils.mapper.PTVProgramToPTVViewModelMapper;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;

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
    private Period period;
    private PTVProgram ptvProgram;
    private TestSubscriber<PTVProgram> subscriber;

    @Before
    public void setUp() throws Exception {
        period = new Period(DateTime.now());
        ptvProgram = PTVUtil.createDummyPTVProgram(period);
        subscriber = new TestSubscriber<>();
        ptvProgramBuilder = mock(PTVProgramBuilder.class);
        ptvProgramRepository = mock(PTVProgramRepository.class);
        ptvProgramToPTVViewModelMapper = mock(PTVProgramToPTVViewModelMapper.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PtvProgramPresenterTest.MyTestModule());
        ptvProgramPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PtvProgramPresenter.class);
        ptvProgramPresenter.setPeriod(period);
    }

    @Test
    public void shouldCreatePTVProgramForCurrentPeriodWhenPTVProgramDoesNotSave() throws LMISException {
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
        expectedPtvProgram.setStatus(PatientDataProgramStatus.DRAFT);
        buildExistingPTVProgram(expectedPtvProgram);

        when(ptvProgramRepository.save(expectedPtvProgram)).thenReturn(expectedPtvProgram);
        Observable<PTVProgram> observableSave = ptvProgramPresenter.savePTVProgram(false);
        observableSave.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);
        assertThat(actualPTVProgram, is(expectedPtvProgram));
    }

    @Test
    public void shouldSavePTVProgramWhenThereIsNOtPTVProgramStored() throws LMISException, SQLException {
        PTVProgram expectedPtvProgram = createInitialPTVProgram();

        subscriber = new TestSubscriber<>();
        when(ptvProgramRepository.save(expectedPtvProgram)).thenReturn(expectedPtvProgram);
        Observable<PTVProgram> observableSave = ptvProgramPresenter.savePTVProgram(false);
        observableSave.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);
        assertThat(actualPTVProgram, is(expectedPtvProgram));
    }

    private PTVProgram createInitialPTVProgram() throws LMISException {
        PTVProgram expectedPtvProgram = PTVUtil.createDummyPTVProgram(period);
        when(ptvProgramRepository.getByPeriod(period)).thenReturn(null);
        when(ptvProgramBuilder.buildInitialPTVProgram(period)).thenReturn(expectedPtvProgram);

        Observable<PTVProgram> observableBuild = ptvProgramPresenter.buildInitialPtvProgram();
        observableBuild.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        return expectedPtvProgram;
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
        buildExistingPTVProgram(ptvProgram);

        subscriber = new TestSubscriber<>();
        doThrow(new LMISException("Cannot create PTV program")).when(ptvProgramRepository).save(ptvProgram);
        Observable<PTVProgram> observable = ptvProgramPresenter.savePTVProgram(false);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(new LMISException("Cannot create PTV program"));
    }

    @Test
    public void shouldThrowSQLExceptionWhenSavePTVProgramWithErrors() throws Exception {
        buildExistingPTVProgram(ptvProgram);

        subscriber = new TestSubscriber<>();
        doThrow(SQLException.class).when(ptvProgramRepository).save(ptvProgram);
        Observable<PTVProgram> observable = ptvProgramPresenter.savePTVProgram(false);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(SQLException.class);
    }

    private void buildExistingPTVProgram(PTVProgram ptvProgram) throws LMISException {
        when(ptvProgramRepository.getByPeriod(period)).thenReturn(ptvProgram);
        when(ptvProgramBuilder.buildExistentPTVProgram(ptvProgram)).thenReturn(ptvProgram);
        Observable<PTVProgram> observableBuild = ptvProgramPresenter.buildInitialPtvProgram();
        observableBuild.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PTVProgramBuilder.class).toInstance(ptvProgramBuilder);
            bind(PTVProgramRepository.class).toInstance(ptvProgramRepository);
            bind(PTVProgramToPTVViewModelMapper.class).toInstance(ptvProgramToPTVViewModelMapper);
        }
    }
}