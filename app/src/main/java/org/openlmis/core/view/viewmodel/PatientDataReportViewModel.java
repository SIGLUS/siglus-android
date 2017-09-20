package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.Range;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import java.util.List;

import lombok.Getter;

public class PatientDataReportViewModel {

    public static long DEFAULT_FORM_ID = 0;

    private Period period;

    private List<String> existingStock;

    private String usApe;
    private List<String> currentTreatments;

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

    public List<String> getCurrentTreatments() {
        return currentTreatments;
    }

    public List<String> getExistingStock() {
        return existingStock;
    }

    public void setExistingStock(List<String> existingStock) {
        this.existingStock = existingStock;
    }

    public void setUsApe(String usApe) {
        this.usApe = usApe;
    }

    public void setCurrentTreatments(List<String> currentTreatments) {
        this.currentTreatments = currentTreatments;
    }
}
