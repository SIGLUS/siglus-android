package org.openlmis.core.service;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openlmis.core.model.Period;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PatientDataServiceTest {

    @Test
    public void shouldReturnAListWithTheCurrentPeriodWhenThereAreNotPatientDataReported() throws Exception {
        PatientDataService patientDataService = new PatientDataService();
        List<Period> periods = patientDataService.calculatePeriods();
        Period expectedPeriod = new Period(DateTime.now());
        assertThat(periods.size(), is(1));
        assertThat(EqualsBuilder.reflectionEquals(expectedPeriod, periods.get(0)), is(true));
    }
}
