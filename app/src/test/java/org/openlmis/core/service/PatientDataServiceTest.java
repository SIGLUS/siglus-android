package org.openlmis.core.service;

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.PatientDataRepository;
import org.roboguice.shaded.goole.common.base.Optional;
import org.robolectric.RuntimeEnvironment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PatientDataServiceTest {

    public static final int FIRST_PERIOD_POSITION = 0;
    public static final int CURRENT_MONTH = 1;

    private PatientDataRepository patientDataRepository;
    private PatientDataService patientDataService;
    private int monthsAfterInitialReportedDate;

    @Before
    public void setup() {
        patientDataRepository = mock(PatientDataRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        patientDataService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataService.class);
    }

    @Test
    public void shouldNotReturnPeriodsWhenThereAreNotPatientDataReportedAndCurrentPeriodIsNotOpenToRequisitions() throws LMISException {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-09-13").getMillis());
        Optional<PatientDataReport> patientDataReport = Optional.absent();
        when(patientDataRepository.getFirstMovement()).thenReturn(patientDataReport);
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(periods.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnCurrentPeriodWhenThereAreNotPatientDataReportedAndCurrentPeriodIsOpenToRequisitions() throws LMISException {
        DateTime today = calculateDateWithinRequisitionPeriod();
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        Optional<PatientDataReport> patientDataReport = Optional.absent();
        when(patientDataRepository.getFirstMovement()).thenReturn(patientDataReport);
        Period expectedPeriod = new Period(new DateTime(LMISApp.getInstance().getCurrentTimeMillis()));
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(EqualsBuilder.reflectionEquals(expectedPeriod, periods.get(FIRST_PERIOD_POSITION)), is(true));
    }

    @Test
    public void shouldReturnPeriodsStartingFromFirstPatientDataReportedIncludingCurrent() throws LMISException {
        DateTime firstReportedDate = calculateDateWithinRequisitionPeriod();
        DateTime today = calculateValidDateForRequisitionPeriodWithinTwelveMonths(firstReportedDate);
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setReportedDate(firstReportedDate);
        when(patientDataRepository.getFirstMovement()).thenReturn(Optional.of(patientDataReport));
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(periods.size(), is(monthsAfterInitialReportedDate + CURRENT_MONTH));
    }

    @Test
    public void shouldReturnPeriodsStartingFromFirstPatientDataReportedExcludingCurrentWhenCurrentDateIsNotOpenToRequisitions() throws LMISException {
        DateTime firstReportedDate = calculateDateWithinRequisitionPeriod();
        DateTime today = calculateInvalidDateForRequisitionPeriodWithinTwelveMonths(firstReportedDate);
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setReportedDate(firstReportedDate);
        when(patientDataRepository.getFirstMovement()).thenReturn(Optional.of(patientDataReport));
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(periods.size(), is(monthsAfterInitialReportedDate));
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

    private DateTime calculateValidDateForRequisitionPeriodWithinTwelveMonths (DateTime startingValidDate) {
        monthsAfterInitialReportedDate = nextInt(1, 13);
        return startingValidDate.plusMonths(monthsAfterInitialReportedDate);
    }

    private DateTime calculateInvalidDateForRequisitionPeriodWithinTwelveMonths (DateTime startingValidDate) {
        int daysAfterInitialReportedDate = nextInt(3, 10);
        DateTime validActualDate = calculateValidDateForRequisitionPeriodWithinTwelveMonths(startingValidDate);
        return validActualDate.minusDays(daysAfterInitialReportedDate);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PatientDataRepository.class).toInstance(patientDataRepository);
        }
    }
}
