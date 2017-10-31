package org.openlmis.core.view.viewmodel.malaria;

import org.joda.time.DateTime;
import org.openlmis.core.model.MalariaProgramStatus;
import org.openlmis.core.model.Period;

import lombok.Getter;

@Getter
public class PatientDataReportViewModel {

    public static long DEFAULT_FORM_ID = 0;

    private Period period;

    private DateTime reportedDate;

    private MalariaProgramStatus status;

    public PatientDataReportViewModel(Period period, DateTime reportedDate, MalariaProgramStatus status) {
        this.period = period;
        this.reportedDate = reportedDate;
        this.status = status;
    }
}
