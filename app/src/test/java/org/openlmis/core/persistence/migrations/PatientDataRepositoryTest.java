package org.openlmis.core.persistence.migrations;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.PatientDataRepository;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.BoundType;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.Random;

import roboguice.RoboGuice;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class PatientDataRepositoryTest {

    public static final long CURRENT_TREATMENT_VALUE = 123;
    private static final long EXISTING_STOCK_VALUE = 300;
    public static final int MISSING_TYPE = 1;
    public static final int DRAFT_TYPE = 2;
    private static final int COMPLETE_TYPE = 1;
    private static final int SYNCED_TYPE = 2;

    private PatientDataRepository patientDataReportRepository;
    private PatientDataReport patientDataReport;
    private Period period;

    @Before
    public void setup() {
        period = new Period(DateTime.parse("2017-06-18"));
        patientDataReportRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataRepository.class);
        patientDataReport = createDummyPatientDataReport(period);
    }

    private PatientDataReport createDummyPatientDataReport(Period period) {
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setType("US");
        patientDataReport.setReportedDate(DateTime.parse("2017-06-18"));
        patientDataReport.setCurrentTreatment6x1(CURRENT_TREATMENT_VALUE);
        patientDataReport.setCurrentTreatment6x2(CURRENT_TREATMENT_VALUE);
        patientDataReport.setCurrentTreatment6x3(CURRENT_TREATMENT_VALUE);
        patientDataReport.setCurrentTreatment6x4(CURRENT_TREATMENT_VALUE);
        patientDataReport.setExistingStock6x1(EXISTING_STOCK_VALUE);
        patientDataReport.setExistingStock6x2(EXISTING_STOCK_VALUE);
        patientDataReport.setExistingStock6x3(EXISTING_STOCK_VALUE);
        patientDataReport.setExistingStock6x4(EXISTING_STOCK_VALUE);
        patientDataReport.setStatusMissing(Boolean.FALSE);
        patientDataReport.setStatusDraft(Boolean.FALSE);
        patientDataReport.setStatusComplete(Boolean.FALSE);
        patientDataReport.setStatusSynced(Boolean.FALSE);
        patientDataReport.setStartDatePeriod(period.getBegin());
        patientDataReport.setEndDatePeriod(period.getEnd());
        return patientDataReport;
    }

    @Test
    public void shouldReturnPatientDataReportedWhenPatientDataReportWasSavedSuccessfullyInDatabaseAndStatusIsMissingOrDraft() throws LMISException {
        patientDataReport.setStatusMissing(Boolean.TRUE);
        Optional<PatientDataReport> patientDataReportSaved = patientDataReportRepository.saveMovement(patientDataReport);
        assertThat(patientDataReportSaved, is(Optional.of(patientDataReport)));
        assertThat(patientDataReportSaved.get().isStatusComplete(), is(Boolean.FALSE));
        assertThat(patientDataReportSaved.get().isStatusDraft(), is(Boolean.TRUE));
        assertThat(patientDataReportSaved.get().isStatusMissing(), is(Boolean.FALSE));
        assertThat(patientDataReportSaved.get().isStatusSynced(), is(Boolean.FALSE));
    }

    @Test
    public void shouldNotReturnPatientDataReportedWhenPatientDataReportWasSavedSuccessfullyInDatabaseAndStatusIsCompleteOrSynced() throws LMISException {
        int typeStatus = nextInt(1, 3);;
        if (typeStatus == COMPLETE_TYPE) {
            patientDataReport.setStatusComplete(Boolean.TRUE);
        }
        if (typeStatus == SYNCED_TYPE) {
            patientDataReport.setStatusSynced(Boolean.TRUE);
        }
        Optional<PatientDataReport> patientDataReportSaved = patientDataReportRepository.saveMovement(patientDataReport);
        assertThat(patientDataReportSaved, is(Optional.<PatientDataReport>absent()));
    }

    @Test
    public void shouldNotReturnPatientDataWhenThereIsAPatientDataReportedInThatPeriodAndIsNotCheckedAsMissingOrDraft() throws LMISException {
        patientDataReport.setStatusComplete(Boolean.TRUE);
        Optional<PatientDataReport> patientDataReportSaved = patientDataReportRepository.saveMovement(patientDataReport);
        assertThat(patientDataReportSaved.isPresent(), is(false));
    }

    @Test
    public void shouldReturnPatientDataReports() throws LMISException {
        patientDataReport.setStatusMissing(Boolean.TRUE);
        patientDataReport.setReportedDate(DateTime.parse("2017-07-18"));
        patientDataReportRepository.saveMovement(patientDataReport);
        PatientDataReport anotherPatientDataReport = new PatientDataReport();
        DateTime beginDateNextMonth = DateTime.parse("2017-08-18");
        anotherPatientDataReport.setReportedDate(beginDateNextMonth);
        Period period = new Period(beginDateNextMonth);
        anotherPatientDataReport = createDummyPatientDataReport(period);
        anotherPatientDataReport.setStatusMissing(Boolean.TRUE);
        patientDataReportRepository.saveMovement(anotherPatientDataReport);
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

    @Test
    public void shouldReturnDraftedPatientDataReportWhenThePatientDataReportIsAlreadySaved() throws LMISException {
        patientDataReport.setStatusMissing(Boolean.TRUE);
        patientDataReport.setReportedDate(DateTime.parse("2017-07-18"));
        Optional<PatientDataReport> patientDataReportSaved = patientDataReportRepository.saveMovement(patientDataReport);
        Optional<PatientDataReport> patientDataReportAfterModify = patientDataReportRepository.saveMovement(patientDataReportSaved.get());
        assertThat(patientDataReportAfterModify, not(Optional.<PatientDataReport>absent()));
        assertThat(patientDataReportAfterModify.get().isStatusDraft(), is(Boolean.TRUE));
    }
}
