package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;

import java.util.List;

import lombok.Getter;

public class PatientDataReportViewModel {

    public static long DEFAULT_FORM_ID = 0;

    private Period period;

    private List<Long> existingStock;

    private String usApe;
    private List<Long> currentTreatments;

    public PatientDataReportViewModel(Period period) {
        this.period = period;
    }

    @Getter
    private PatientDataReport patientDataReport = new PatientDataReport();

    public Period getPeriod() {
        return period;
    }

    public String getUsApe() {
        return usApe;
    }

    public List<Long> getCurrentTreatments() {
        return currentTreatments;
    }

    public List<Long> getExistingStock() {
        return existingStock;
    }

    public void setExistingStock(List<Long> existingStock) {
        this.existingStock = existingStock;
    }

    public void setUsApe(String usApe) {
        this.usApe = usApe;
    }

    public void setCurrentTreatments(List<Long> currentTreatments) {
        this.currentTreatments = currentTreatments;
    }
}
