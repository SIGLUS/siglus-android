package org.openlmis.core.view.viewmodel;

import org.joda.time.DateTime;
import org.openlmis.core.model.Period;

import lombok.Data;

@Data
public class RapidTestReportViewModel {
    Period period;
    Status status;
    private DateTime syncedTime;


    public RapidTestReportViewModel(Period period) {
        this.period = period;
        status = Status.MISSING;
    }

    public DateTime getSyncedTime() {
        return syncedTime;
    }

    public enum Status {
        MISSING(0),
        Draft(1),
        COMPLETED(2),
        SYNCED(3);

        public int type;

        Status(int type) {
            this.type = type;
        }
    }
}

