package org.openlmis.core.view.viewmodel.malaria;

import org.joda.time.DateTime;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.model.Period;

import lombok.Getter;

@Getter
public class PatientDataReportViewModel {

    private Period period;

    private DateTime reportedDate;

    private PatientDataProgramStatus status;

    public PatientDataReportViewModel(Period period, DateTime reportedDate, PatientDataProgramStatus status) {
        this.period = period;
        this.reportedDate = reportedDate;
        this.status = status;
    }
}
