package org.openlmis.core.view.viewmodel.malaria;

import org.openlmis.core.model.ViaReportStatus;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImplementationReportViewModel {

    private ImplementationReportType type;

    private ViaReportStatus status;

    private long currentTreatment6x1;

    private long currentTreatment6x2;

    private long currentTreatment6x3;

    private long currentTreatment6x4;

    private long existingStock6x1;

    private long existingStock6x2;

    private long existingStock6x3;

    private long existingStock6x4;

    public ImplementationReportViewModel(ImplementationReportType type,
                                         long currentTreatment6x1,
                                         long currentTreatment6x2,
                                         long currentTreatment6x3,
                                         long currentTreatment6x4,
                                         long existingStock6x1,
                                         long existingStock6x2,
                                         long existingStock6x3,
                                         long existingStock6x4) {
        this.type = type;
        this.currentTreatment6x1 = currentTreatment6x1;
        this.currentTreatment6x2 = currentTreatment6x2;
        this.currentTreatment6x3 = currentTreatment6x3;
        this.currentTreatment6x4 = currentTreatment6x4;
        this.existingStock6x1 = existingStock6x1;
        this.existingStock6x2 = existingStock6x2;
        this.existingStock6x3 = existingStock6x3;
        this.existingStock6x4 = existingStock6x4;
    }

    public List<Long> getExistingStock() {
        return Arrays.asList(new Long[]{existingStock6x1, existingStock6x2, existingStock6x3, existingStock6x4});
    }

    public List<Long> getCurrentTreatments() {
        return Arrays.asList(new Long[]{currentTreatment6x1, currentTreatment6x2, currentTreatment6x3, currentTreatment6x4});
    }

    public void setExistingStock(List<Long> existingStock) {
        this.existingStock6x1 = existingStock.get(0);
        this.existingStock6x2 = existingStock.get(1);
        this.existingStock6x3 = existingStock.get(2);
        this.existingStock6x4 = existingStock.get(3);
    }
    public void setCurrentTreatments(List<Long> currentTreatments) {
        this.currentTreatment6x1 = currentTreatments.get(0);
        this.currentTreatment6x2 = currentTreatments.get(1);
        this.currentTreatment6x3 = currentTreatments.get(2);
        this.currentTreatment6x4 = currentTreatments.get(3);
    }
}
