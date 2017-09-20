package org.openlmis.core.persistence.migrations;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.repository.PatientDataRepository;
import org.roboguice.shaded.goole.common.base.Optional;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class PatientDataRepositoryTest {

    private PatientDataRepository patientDataReportRepository;
    private PatientDataReport patientDataReport;

    @Before
    public void setup() {
        patientDataReportRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataRepository.class);
        patientDataReport = new PatientDataReport();
        patientDataReport.setReportedDate(DateTime.parse("2017-06-18"));
    }

    @Test
    public void shouldReturnPatientDataReportedWhenPatientDataReportWasSavedSuccessfullyInDatabase() throws LMISException {
        Optional<PatientDataReport> patientDataReportSaved = patientDataReportRepository.saveMovement(patientDataReport);
        assertThat(patientDataReportSaved, is(Optional.of(patientDataReport)));
    }

    @Test(expected = LMISException.class)
    public void shouldThrowLMISExceptionWhenReportedDateIsNullAndPatientDataReportWasNotSavedInDatabase() throws LMISException {
        patientDataReport.setReportedDate(null);
        patientDataReportRepository.saveMovement(patientDataReport);
    }

    @Test
    public void shouldReturnPatientDataReports() throws LMISException {
        patientDataReport.setReportedDate(DateTime.parse("2017-07-18"));
        patientDataReportRepository.saveMovement(patientDataReport);
        patientDataReport.setReportedDate(DateTime.parse("2017-08-18"));
        patientDataReportRepository.saveMovement(patientDataReport);
        List<PatientDataReport> reports = patientDataReportRepository.getAllMovements();
        assertThat(reports.size(), is(2));
    }

    @Test
    public void shouldReturnTheFirstMovementWhenExistsInTheDatabase() throws LMISException {
        Optional<PatientDataReport> patientDataReportSaved = patientDataReportRepository.saveMovement(patientDataReport);
        assertThat(patientDataReportRepository.getFirstMovement(), is(patientDataReportSaved));
    }

    @Test
    public void shouldNotReturnTheFirstMovementWhenNotExistsInTheDatabase() throws LMISException {
        Optional<PatientDataReport> patientDataReportedExpected = Optional.absent();
        assertThat(patientDataReportRepository.getFirstMovement().isPresent(), is(false));
    }
}
