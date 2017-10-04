package org.openlmis.core.utils;

import android.support.annotation.NonNull;

import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Treatment;

import java.util.List;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportMapper {

    public MalariaProgram mapToMalariaProgramFromAListOfPatientDataReport(List<PatientDataReport> reports) {
        MalariaProgram malariaProgram = new MalariaProgram();
        mapMalariaProgramData(reports, malariaProgram);
        Implementation implementationUS = mapImplementationUS(reports);
        Implementation implementationAPE = mapImplementationApe(reports);
        mapTreatmentsForUs(reports, implementationUS);
        mapImplenetationForApe(reports, implementationAPE);
        malariaProgram.setImplementations(newArrayList(implementationUS, implementationAPE));
        return malariaProgram;
    }

    private void mapImplenetationForApe(List<PatientDataReport> reports, Implementation implementationAPE) {
        Treatment firstTreatmentAPE = new Treatment(reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getCurrentTreatment6x1(), reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getExistingStock6x1());
        Treatment secondTreatmentAPE = new Treatment(reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getCurrentTreatment6x2(), reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getExistingStock6x2());
        Treatment thirdTreatmentAPE = new Treatment(reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getCurrentTreatment6x3(), reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getExistingStock6x3());
        Treatment fourthTreatmentAPE = new Treatment(reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getCurrentTreatment6x4(), reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getExistingStock6x4());
        implementationAPE.setTreatments(newArrayList(firstTreatmentAPE,secondTreatmentAPE,thirdTreatmentAPE,fourthTreatmentAPE));
    }

    private void mapTreatmentsForUs(List<PatientDataReport> reports, Implementation implementationUS) {
        Treatment firstTreatmentUS = new Treatment(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getCurrentTreatment6x1(), reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getExistingStock6x1());
        Treatment secondTreatmentUS = new Treatment(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getCurrentTreatment6x2(), reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getExistingStock6x2());
        Treatment thirdTreatmentUS = new Treatment(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getCurrentTreatment6x3(), reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getExistingStock6x3());
        Treatment fourthTreatmentUS = new Treatment(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getCurrentTreatment6x4(), reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getExistingStock6x4());
        implementationUS.setTreatments(newArrayList(firstTreatmentUS,secondTreatmentUS,thirdTreatmentUS,fourthTreatmentUS));
    }

    @NonNull
    private Implementation mapImplementationApe(List<PatientDataReport> reports) {
        Implementation implementationAPE = new Implementation();
        implementationAPE.setExecutor(reports.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getType());
        return implementationAPE;
    }

    @NonNull
    private Implementation mapImplementationUS(List<PatientDataReport> reports) {
        Implementation implementationUS = new Implementation();
        implementationUS.setExecutor(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getType());
        return implementationUS;
    }

    private void mapMalariaProgramData(List<PatientDataReport> reports, MalariaProgram malariaProgram) {
        malariaProgram.setReportedDate(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getReportedDate());
        malariaProgram.setStartDatePeriod(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getStartDatePeriod());
        malariaProgram.setEndDatePeriod(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getEndDatePeriod());
        malariaProgram.setStatusMissing(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusMissing());
        malariaProgram.setStatusDraft(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusDraft());
        malariaProgram.setStatusComplete(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusComplete());
        malariaProgram.setStatusSynced(reports.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).isStatusSynced());
    }
}
