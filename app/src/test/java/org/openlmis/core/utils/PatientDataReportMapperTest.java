package org.openlmis.core.utils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportMapperTest {

    private List<PatientDataReport> patientDataReports;
    private MalariaProgram expectedMalariaProgram;
    private PatientDataReport firstExpectedReport;
    private PatientDataReport secondExpectedReport;
    private MalariaProgram malariaProgram;


    @Before
    public void init() throws SQLException {
        boolean missing = false;
        boolean draft = true;
        boolean complete = false;
        boolean synced = true;
        DateTime reportedDate = DateTime.now();
        DateTime startedDatePeriod = DateTime.now().plusDays(1);
        DateTime endDatePeriod = DateTime.now().plusDays(2);
        Treatment firstTreatmentForFirstCollection = new Treatment(getRandomNumber(), getRandomNumber());
        Treatment secondTreatmentForFirstCollection = getRandomTreatment();
        Treatment thirdTreatmentForFirstCollection = getRandomTreatment();
        Treatment fourthTreatmentForFirstCollection = getRandomTreatment();
        Treatment firstTreatmentForSecondCollection = getRandomTreatment();
        Treatment secondTreatmentForSecondCollection = getRandomTreatment();
        Treatment thirdTreatmentForSecondCollection = getRandomTreatment();
        Treatment fourthTreatmentForSecondCollection = getRandomTreatment();
        firstExpectedReport = createPatientDataReportByType(MalariaProgramMapper.US_EXECUTOR, firstTreatmentForFirstCollection, secondTreatmentForFirstCollection, thirdTreatmentForFirstCollection, fourthTreatmentForFirstCollection, reportedDate, startedDatePeriod, endDatePeriod, missing, draft, complete, synced);
        secondExpectedReport = createPatientDataReportByType(MalariaProgramMapper.APE_EXECUTOR, firstTreatmentForSecondCollection, secondTreatmentForSecondCollection, thirdTreatmentForSecondCollection, fourthTreatmentForSecondCollection, reportedDate, startedDatePeriod, endDatePeriod, missing, draft, complete, synced);
        patientDataReports = newArrayList(firstExpectedReport, secondExpectedReport);
        List<Treatment> treatmentsForFirstImplementation = newArrayList(firstTreatmentForFirstCollection, secondTreatmentForFirstCollection, thirdTreatmentForFirstCollection, fourthTreatmentForFirstCollection);
        List<Treatment> treatmentsForSecondImplementation = newArrayList(firstTreatmentForSecondCollection, secondTreatmentForSecondCollection, thirdTreatmentForSecondCollection, fourthTreatmentForSecondCollection);
        Implementation firstImplementation = new Implementation(MalariaProgramMapper.US_EXECUTOR, treatmentsForFirstImplementation);
        Implementation secondImplementation = new Implementation(MalariaProgramMapper.APE_EXECUTOR, treatmentsForSecondImplementation);
        List<Implementation> implementations = newArrayList(firstImplementation, secondImplementation);
        expectedMalariaProgram = new MalariaProgram(reportedDate, startedDatePeriod, endDatePeriod,
                missing, draft, implementations);
        expectedMalariaProgram.setStatusSynced(synced);
        expectedMalariaProgram.setStatusComplete(complete);
        PatientDataReportMapper mapper = new PatientDataReportMapper();
        malariaProgram = mapper.mapToMalariaProgramFromAListOfPatientDataReport(patientDataReports);

    }

    private PatientDataReport createPatientDataReportByType(String type, Treatment firstTreatmentForFirstCollection, Treatment secondTreatmentForFirstCollection, Treatment thirdTreatmentForFirstCollection, Treatment fourthTreatmentForFirstCollection, DateTime reportedDate, DateTime startedDatePeriod, DateTime endDatePeriod, boolean missing, boolean draft, boolean complete, boolean synced) {
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setReportedDate(reportedDate);
        patientDataReport.setStartDatePeriod(startedDatePeriod);
        patientDataReport.setEndDatePeriod(endDatePeriod);
        patientDataReport.setType(type);
        patientDataReport.setCurrentTreatment6x1(firstTreatmentForFirstCollection.getAmount());
        patientDataReport.setCurrentTreatment6x2(secondTreatmentForFirstCollection.getAmount());
        patientDataReport.setCurrentTreatment6x3(thirdTreatmentForFirstCollection.getAmount());
        patientDataReport.setCurrentTreatment6x4(fourthTreatmentForFirstCollection.getAmount());

        patientDataReport.setExistingStock6x1(firstTreatmentForFirstCollection.getStock());
        patientDataReport.setExistingStock6x2(secondTreatmentForFirstCollection.getStock());
        patientDataReport.setExistingStock6x3(thirdTreatmentForFirstCollection.getStock());
        patientDataReport.setExistingStock6x4(fourthTreatmentForFirstCollection.getStock());

        patientDataReport.setStatusMissing(missing);
        patientDataReport.setStatusDraft(draft);
        patientDataReport.setStatusComplete(complete);
        patientDataReport.setStatusSynced(synced);
        return patientDataReport;
    }

    private Treatment getRandomTreatment() {
        return new Treatment(getRandomNumber(), getRandomNumber());
    }

    private int getRandomNumber() {
        int min = 1;
        int max = 100;
        int offset = 1;
        return min + (int) (Math.random() * ((max - min) + offset));
    }

    @Test
    public void shouldReturnSameDatesInPatientDataReportAndInMalariaProgramObject() {
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getReportedDate(), is(malariaProgram.getReportedDate()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getStartDatePeriod(), is(malariaProgram.getStartDatePeriod()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getEndDatePeriod(), is(malariaProgram.getEndDatePeriod()));
    }

    @Test
    public void shouldReturnSameValuesInTheDifferentStatusesInBothRecords() throws Exception {
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusDraft(), is(malariaProgram.isStatusDraft()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusMissing(), is(malariaProgram.isStatusMissing()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusComplete(), is(malariaProgram.isStatusComplete()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusSynced(), is(malariaProgram.isStatusSynced()));
    }

    @Test
    public void shouldReturnSameNumberOfImplementationsElements() {
        assertThat(malariaProgram.getImplementations().size(), is(2));
    }

    @Test
    public void shouldTheFirstElementBeUSAndTheSecondBeAPE() throws Exception {
        ArrayList<Implementation> implementations = (ArrayList<Implementation>) malariaProgram.getImplementations();
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getType(), is(implementations.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getExecutor()));
        assertThat(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getType(), is(implementations.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getExecutor()));
    }

    @Test
    public void shouldHaveFourElementsOfTreatmentAndStockForEachImplementation() throws Exception {
        ArrayList<Implementation> implementations = (ArrayList<Implementation>) malariaProgram.getImplementations();
        assertThat(implementations.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getTreatments().size(), is(4));
        assertThat(implementations.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getTreatments().size(), is(4));
    }

    @Test
    public void shouldReturnTheExpectedElementAfterMappingListOfPatientDataReport() {
        assertTrue(EqualsBuilder.reflectionEquals(expectedMalariaProgram, malariaProgram));
    }
}