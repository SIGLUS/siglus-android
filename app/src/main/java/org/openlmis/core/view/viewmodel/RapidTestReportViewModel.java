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

    public void setRapidTestForm(ProgramDataForm rapidTestForm) {
        this.rapidTestForm = rapidTestForm;
        switch (rapidTestForm.getStatus()) {
            case DRAFT:
            case SUBMITTED:
                this.status = Status.INCOMPLETE;
                break;
            case AUTHORIZED:
                this.status = Status.COMPLETED;
                break;
            default:
                this.status = Status.MISSING;
        }
        if (rapidTestForm.isSynced()) {
            this.status = Status.SYNCED;
        }
    }

    public DateTime getSyncedTime() {
        return syncedTime;
    }

    public enum Status {
        MISSING(0),
        INCOMPLETE(1),
        COMPLETED(2),
        SYNCED(3);

        public int type;

        Status(int type) {
            this.type = type;
        }
    }
}

