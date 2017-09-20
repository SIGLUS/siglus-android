package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.service.PatientDataService;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PatientDataReportPresenterTest {

    private PatientDataService patientDataService;
    private PatientDataReportPresenter presenter;

    @Before
    public void setup() {
        patientDataService = mock(PatientDataService.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PatientDataReportPresenterTest.MyTestModule());
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataReportPresenter.class);
    }

    @Test
    public void shouldGenerateViewModelsForAllPeriods() throws LMISException {
        DateTime firstDate = calculateDateWithinRequisitionPeriod();
        DateTime secondDate = firstDate.plusMonths(1);
        Period firstPeriod = new Period(firstDate);
        Period secondPeriod = new Period(secondDate);
        List<Period> periodsExpected = Arrays.asList(new Period[]{firstPeriod, secondPeriod});
        when(patientDataService.calculatePeriods()).thenReturn(periodsExpected);
        presenter.generateViewModelsForAvailablePeriods();
        assertThat(presenter.getViewModels().size(), is(periodsExpected.size()));
    }

    private DateTime calculateDateWithinRequisitionPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int requisitionPeriodStartingDay = 18;
        int daysToAddWithinCurrentPeriod = nextInt(0, 3);
        calendar.set(Calendar.DAY_OF_MONTH, requisitionPeriodStartingDay);
        DateTime actualDate = new DateTime(calendar.getTime().getTime()).plusDays(daysToAddWithinCurrentPeriod);
        return actualDate;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PatientDataService.class).toInstance(patientDataService);
        }
    }
}