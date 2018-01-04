package org.openlmis.core.view.viewmodel.malaria;

import org.joda.time.DateTime;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.model.Period;

import lombok.Getter;

@Getter
public class PatientDataReportViewModel {

    private Period period;

    private DateTime reportedDate;

    private ViaReportStatus status;

    public PatientDataReportViewModel(Period period, DateTime reportedDate, ViaReportStatus status) {
        this.period = period;
        this.reportedDate = reportedDate;
        this.status = status;
    }
}
