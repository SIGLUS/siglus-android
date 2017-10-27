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
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PtvProgramPresenterTest {

    private PtvProgramPresenter ptvProgramPresenter;
    private PTVProgramBuilder ptvProgramBuilder;

    @Before
    public void setUp() throws Exception {
        ptvProgramBuilder = mock(PTVProgramBuilder.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PtvProgramPresenterTest.MyTestModule());
        ptvProgramPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PtvProgramPresenter.class);
    }

    @Test
    public void shouldCreatePTVProgramForCurrentPeriodWhenPTVProgramDoesNotSave() throws LMISException {
        Period period = new Period(DateTime.now());
        PTVProgram expectedPTVProgram = PTVUtil.createDummyPTVProgram(period);
        expectedPTVProgram.setPatientDispensations(PTVUtil.createDummyPatientDispensations());
        expectedPTVProgram.setPtvProgramStocksInformation(PTVUtil.createDummyPTVProgramStocksInformation());
        when(ptvProgramBuilder.buildPTVProgram(period)).thenReturn(expectedPTVProgram);
        ptvProgramPresenter.setPeriod(period);

        TestSubscriber<PTVProgram> subscriber = new TestSubscriber<>();
        Observable<PTVProgram> observable = ptvProgramPresenter.buildInitialPtvProgram();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        PTVProgram actualPTVProgram = subscriber.getOnNextEvents().get(0);

        assertThat(expectedPTVProgram, is(actualPTVProgram));
    }

    @Test
    public void shouldThrowAnExceptionWhenPeriodIsNotSetUp() throws Exception {
        TestSubscriber<PTVProgram> subscriber = new TestSubscriber<>();
        Observable<PTVProgram> observable = ptvProgramPresenter.buildInitialPtvProgram();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(new LMISException("Period cannot be null"));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PTVProgramBuilder.class).toInstance(ptvProgramBuilder);
        }
    }
}