package org.openlmis.core.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
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

public class MalariaProgramMapperTest {

    private List<PatientDataReport> patientDataReports;
    private MalariaProgram malariaProgram;
    private PatientDataReport firstExpectedReport;
    private PatientDataReport secondExpectedReport;

    @Before
    public void init() throws SQLException {
        Treatment firstTreatmentForFirstCollection = new Treatment(getRandomNumber(),getRandomNumber());
        Treatment secondTreatmentForFirstCollection = getRandomTreatment();
        Treatment thirdTreatmentForFirstCollection = getRandomTreatment();
        Treatment fourthTreatmentForFirstCollection = getRandomTreatment();
        Treatment firstTreatmentForSecondCollection = getRandomTreatment();
        Treatment secondTreatmentForSecondCollection = getRandomTreatment();
        Treatment thirdTreatmentForSecondCollection = getRandomTreatment();
        Treatment fourthTreatmentForSecondCollection = getRandomTreatment();
        List<Treatment> treatmentsForFirstImplementation = newArrayList(firstTreatmentForFirstCollection,secondTreatmentForFirstCollection,thirdTreatmentForFirstCollection,fourthTreatmentForFirstCollection);
        List<Treatment> treatmentsForSecondImplementation = newArrayList(firstTreatmentForSecondCollection,secondTreatmentForSecondCollection,thirdTreatmentForSecondCollection,fourthTreatmentForSecondCollection);
        Implementation firstImplementation = new Implementation(MalariaProgramMapper.US_EXECUTOR,treatmentsForFirstImplementation);
        Implementation secondImplementation = new Implementation(MalariaProgramMapper.APE_EXECUTOR,treatmentsForSecondImplementation);
        List<Implementation> implementations = newArrayList(firstImplementation, secondImplementation);
        DateTime reportedDate = DateTime.now();
        DateTime startedDatePeriod = DateTime.now().plusDays(1);
        DateTime endDatePeriod = DateTime.now().plusDays(2);
        boolean missing = false;
        boolean draft = true;
        boolean complete = false;
        boolean synced = true;
        malariaProgram = new MalariaProgram(reportedDate, startedDatePeriod,endDatePeriod,
                missing, draft, implementations);
        malariaProgram.setStatusComplete(complete);
        malariaProgram.setStatusSynced(synced);
        MalariaProgramMapper mapper = new MalariaProgramMapper();
        patientDataReports = mapper.mapMalariaProgramToPatientDataReport(malariaProgram);

        firstExpectedReport = createPatientDataReportByType(MalariaProgramMapper.US_EXECUTOR,firstTreatmentForFirstCollection, secondTreatmentForFirstCollection, thirdTreatmentForFirstCollection, fourthTreatmentForFirstCollection, reportedDate, startedDatePeriod, endDatePeriod, missing, draft, complete, synced);

        secondExpectedReport = createPatientDataReportByType(MalariaProgramMapper.APE_EXECUTOR,firstTreatmentForSecondCollection, secondTreatmentForSecondCollection, thirdTreatmentForSecondCollection, fourthTreatmentForSecondCollection, reportedDate, startedDatePeriod, endDatePeriod, missing, draft, complete, synced);
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

    private Treatment getRandomTreatment(){
        return new Treatment(getRandomNumber(),getRandomNumber());
    }

    private int getRandomNumber(){
        int min = 1;
        int max = 100;
        int offset = 1;
        return min + (int)(Math.random() * ((max - min) + offset));
    }

    @Test
    public void shouldReturnTwoPatientDataModels() {
        int IMPLEMENTATIONS_NUMBER = 2;
        assertThat(patientDataReports.size(), is(IMPLEMENTATIONS_NUMBER));
    }

    @Test
    public void shouldReturnSameValuesInReportedDateStartDatePeriodEndDatePeriodInBothRecords() {
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getReportedDate(), is(malariaProgram.getReportedDate()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getEndDatePeriod(), is(malariaProgram.getEndDatePeriod()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getStartDatePeriod(), is(malariaProgram.getStartDatePeriod()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getReportedDate(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getReportedDate()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getEndDatePeriod(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getEndDatePeriod()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getStartDatePeriod(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getStartDatePeriod()));
    }

    @Test
    public void shouldReturnRecordsWithTypesUSAndAPERespectively() {
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getType(), is("US"));
        assertThat(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getType(), is("APE"));
    }

    @Test
    public void shouldReturnSameValuesInTheDifferentStatusesInBothRecords() {
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusComplete(), is(malariaProgram.isStatusComplete()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusDraft(), is(malariaProgram.isStatusDraft()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusMissing(), is(malariaProgram.isStatusMissing()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusSynced(), is(malariaProgram.isStatusSynced()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusComplete(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).isStatusComplete()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusDraft(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).isStatusDraft()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusMissing(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).isStatusMissing()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusSynced(), is(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).isStatusSynced()));
    }

    @Test
    public void shouldReturnTheCorrectTreatmentsForEachPatientDataReport() throws Exception {
        ArrayList<Implementation> implementations = (ArrayList<Implementation>) malariaProgram.getImplementations();
        ArrayList<Treatment> treatmentsForFirstImplementation = (ArrayList<Treatment>) implementations.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getTreatments();
        ArrayList<Treatment> treatmentsForSecondImplementation = (ArrayList<Treatment>) implementations.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getTreatments();
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getCurrentTreatment6x1(), is(treatmentsForFirstImplementation.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getAmount()));
        assertThat(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getExistingStock6x1(), is(treatmentsForFirstImplementation.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getStock()));
        assertThat(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getCurrentTreatment6x1(), is(treatmentsForSecondImplementation.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getAmount()));
        assertThat(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getExistingStock6x1(), is(treatmentsForSecondImplementation.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getStock()));
    }

    @Test
    public void shouldReturnTheExpectedElementsAfterMappingMalariaProgram() {
        assertTrue(EqualsBuilder.reflectionEquals(patientDataReports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX),firstExpectedReport));
        assertTrue(EqualsBuilder.reflectionEquals(patientDataReports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX),secondExpectedReport));
    }
}