package org.openlmis.core.utils;

import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Treatment;

import java.util.ArrayList;
import java.util.List;

public class MalariaProgramMapper {


    public static final int FIRST_ELEMENT_INDEX = 0;
    public static final int SECOND_ELEMENT_INDEX = 1;
    public static final int THIRD_ELEMENT_INDEX = 2;
    public static final int FOURTH_ELEMENT_INDEX = 3;
    public static final String US_EXECUTOR = "US";
    public static final String APE_EXECUTOR = "APE";

    public List<PatientDataReport> mapMalariaProgramToPatientDataReport(MalariaProgram malariaProgram) {
        List<PatientDataReport> patientDataReports = new ArrayList<>();
        PatientDataReport firstPatientDataReport = new PatientDataReport();
        PatientDataReport secondPatientDataReport = new PatientDataReport();
        setCommonFields(malariaProgram, firstPatientDataReport);
        setCommonFields(malariaProgram, secondPatientDataReport);
        setImplementationsData(malariaProgram, firstPatientDataReport, secondPatientDataReport);
        patientDataReports.add(firstPatientDataReport);
        patientDataReports.add(secondPatientDataReport);
        return patientDataReports;
    }


    private void setImplementationsData(MalariaProgram malariaProgram, PatientDataReport firstPatientDataReport, PatientDataReport secondPatientDataReport) {
        for (Implementation implementation : malariaProgram.getImplementations()) {
            if (implementation.getExecutor().equals(US_EXECUTOR)) {
                setImplementationValuesToPatientDataReport(firstPatientDataReport, implementation);
            } else {
                setImplementationValuesToPatientDataReport(secondPatientDataReport, implementation);
            }
        }
    }

    private void setImplementationValuesToPatientDataReport(PatientDataReport patientDataReport, Implementation implementation) {
        patientDataReport.setType(implementation.getExecutor());
        ArrayList<Treatment> treatments = new ArrayList<>(implementation.getTreatments());
        patientDataReport.setExistingStock6x1(treatments.get(FIRST_ELEMENT_INDEX).getStock());
        patientDataReport.setCurrentTreatment6x1(treatments.get(FIRST_ELEMENT_INDEX).getAmount());
        patientDataReport.setExistingStock6x2(treatments.get(SECOND_ELEMENT_INDEX).getStock());
        patientDataReport.setCurrentTreatment6x2(treatments.get(SECOND_ELEMENT_INDEX).getAmount());
        patientDataReport.setExistingStock6x3(treatments.get(THIRD_ELEMENT_INDEX).getStock());
        patientDataReport.setCurrentTreatment6x3(treatments.get(THIRD_ELEMENT_INDEX).getAmount());
        patientDataReport.setExistingStock6x4(treatments.get(FOURTH_ELEMENT_INDEX).getStock());
        patientDataReport.setCurrentTreatment6x4(treatments.get(FOURTH_ELEMENT_INDEX).getAmount());
    }

    private void setCommonFields(MalariaProgram malariaProgram, PatientDataReport patientDataReport) {
        patientDataReport.setReportedDate(malariaProgram.getReportedDate());
        patientDataReport.setStartDatePeriod(malariaProgram.getStartDatePeriod());
        patientDataReport.setEndDatePeriod(malariaProgram.getEndDatePeriod());
        patientDataReport.setStatusComplete(malariaProgram.isStatusComplete());
        patientDataReport.setStatusDraft(malariaProgram.isStatusDraft());
        patientDataReport.setStatusMissing(malariaProgram.isStatusMissing());
        patientDataReport.setStatusSynced(malariaProgram.isStatusSynced());
    }


}
