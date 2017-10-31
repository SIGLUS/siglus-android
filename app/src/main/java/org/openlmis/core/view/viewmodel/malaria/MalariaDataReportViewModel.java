package org.openlmis.core.view.viewmodel.malaria;

import org.joda.time.DateTime;

import lombok.Data;

@Data
public class MalariaDataReportViewModel {
    private DateTime reportedDate;
    private DateTime startPeriodDate;
    private DateTime endPeriodDate;
    private ImplementationReportViewModel usImplementationReportViewModel;
    private ImplementationReportViewModel apeImplementationReportViewModel;

    public MalariaDataReportViewModel() {
        usImplementationReportViewModel = new ImplementationReportViewModel();
        usImplementationReportViewModel.setType(ImplementationReportType.US);
        apeImplementationReportViewModel = new ImplementationReportViewModel();
        apeImplementationReportViewModel.setType(ImplementationReportType.APE);
    }

    public MalariaDataReportViewModel(DateTime reportedDate,
                                      DateTime startPeriodDate,
                                      DateTime endPeriodDate,
                                      ImplementationReportViewModel usImplementationReportViewModel,
                                      ImplementationReportViewModel apeImplementationReportViewModel) {
        this.reportedDate = reportedDate;
        this.startPeriodDate = startPeriodDate;
        this.endPeriodDate = endPeriodDate;
        this.usImplementationReportViewModel = usImplementationReportViewModel;
        this.apeImplementationReportViewModel = apeImplementationReportViewModel;
    }
}
