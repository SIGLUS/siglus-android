package org.openlmis.core.model;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientDataReport {


    public PatientDataReport() {}

    private DateTime reportedDate;

    private DateTime startDatePeriod;

    private DateTime endDatePeriod;

    private String type;

    private long currentTreatment6x1;

    private long currentTreatment6x2;

    private long currentTreatment6x3;

    private long currentTreatment6x4;

    private long existingStock6x1;

    private long existingStock6x2;

    private long existingStock6x3;

    private long existingStock6x4;

    private boolean statusMissing;

    private boolean statusDraft;

    private boolean statusComplete;

    private boolean statusSynced;

    public DateTime getReportedDate() {
        return reportedDate;
    }

    public void setExistingStocks(List<Long> existingStocks) {
        this.existingStock6x1 = existingStocks.get(0);
        this.existingStock6x2 = existingStocks.get(1);
        this.existingStock6x3 = existingStocks.get(2);
        this.existingStock6x4 = existingStocks.get(3);
    }

    public List<Long> getExistingStocks() {
        return Arrays.asList(new Long[]{existingStock6x1, existingStock6x2, existingStock6x3, existingStock6x4});
    }

    public List<Long> getCurrentTreatments() {
        return Arrays.asList(new Long[]{currentTreatment6x1, currentTreatment6x2, currentTreatment6x3, currentTreatment6x4});
    }

    public void setCurrentTreatments(List<Long> currentTreatments) {
        this.currentTreatment6x1 = currentTreatments.get(0);
        this.currentTreatment6x2 = currentTreatments.get(1);
        this.currentTreatment6x3 = currentTreatments.get(2);
        this.currentTreatment6x4 = currentTreatments.get(3);
    }
}
