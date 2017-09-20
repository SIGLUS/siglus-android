package org.openlmis.core.view.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.service.PatientDataService;
import org.robolectric.RuntimeEnvironment;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PatientDataReportPresenterTest {

    @Inject
    private PatientDataService patientDataService;

    @Before
    public void setup() {
        patientDataService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataService.class);
    }

    @Test
    public void shouldGenerateViewModelsForAllPeriods() throws LMISException {
        DateTime firstDate = calculateDateWithinRequisitionPeriod();
        DateTime secondDate = firstDate.plusMonths(1);
        Period firstPeriod = new Period(firstDate);
        Period secondPeriod = new Period(secondDate);
        Period[] periodsArray = new Period[]{firstPeriod, secondPeriod};
        List<Period> periodsExpected = new Arrays.asList(periodsArray);
        when(patientDataService.calculatePeriods()).thenReturn();
        List<Period> periods = patientDataService.calculatePeriods();
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
}