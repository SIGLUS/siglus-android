package org.openlmis.core.view.viewmodel;

import org.joda.time.DateTime;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;

import lombok.Data;

@Data
public class RapidTestReportViewModel {
    Period period;
    Status status;
    private DateTime syncedTime;

    private ProgramDataForm rapidTestForm;

    public RapidTestReportViewModel(Period period) {
        this.period = period;
        status = Status.MISSING;
    }

    public DateTime getSyncedTime() {
        return syncedTime;
    }

    public enum Status {
        MISSING(0),
        DRAFT(1),
        SUBMITTED(2),
        AUTHORIZED(3),
        SYNCED(4);

        public int type;

        Status(int type) {
            this.type = type;
        }
    }
}

