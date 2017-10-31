package org.openlmis.core.view.viewmodel.malaria;

import org.openlmis.core.model.Period;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PatientDataReportViewModel {

    public static long DEFAULT_FORM_ID = 0;

    private Period period;

    @Setter
    private String executor;

    public PatientDataReportViewModel(Period period) {
        this.period = period;
    }
}
