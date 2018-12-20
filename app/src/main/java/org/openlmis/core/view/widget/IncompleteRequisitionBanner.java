package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.Constants;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;


public class IncompleteRequisitionBanner extends LinearLayout {
    @InjectView(R.id.tx_incomplete_requisition)
    TextView txMissedRequisition;

    @Inject
    RequisitionPeriodService requisitionPeriodService;

    @Inject
    private ReportTypeFormRepository reportTypeFormRepository;

    protected Context context;

    public IncompleteRequisitionBanner(Context context) {
        super(context);
        init(context);
    }

    public IncompleteRequisitionBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_incomplete_requisition_banner, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
        setIncompleteRequisitionBanner();
    }

    public void setIncompleteRequisitionBanner() {
        try {
            ReportTypeForm mmiaReportTypeForm = reportTypeFormRepository.getReportType(Constants.MMIA_PROGRAM_CODE);
            ReportTypeForm viaReportTypeForm = reportTypeFormRepository.getReportType(Constants.VIA_PROGRAM_CODE);
            int periodOffsetMonthMmia = mmiaReportTypeForm == null? 0 : requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE);
            int periodOffsetMonthVia = viaReportTypeForm == null? 0 : requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE);
            if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia == 0) {
                this.setVisibility(View.GONE);
                return;
            } else if (periodOffsetMonthMmia == 1 && periodOffsetMonthVia == 1) {
                String periodRange = getPeriodRangeForIncompleteRequisition(Constants.VIA_PROGRAM_CODE);
                txMissedRequisition.setText(Html.fromHtml(getResources().getString(R.string.via_and_mmia_requisition_alert, periodRange)));
            } else if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia == 1) {
                String periodRange = getPeriodRangeForIncompleteRequisition(Constants.VIA_PROGRAM_CODE);
                txMissedRequisition.setText(Html.fromHtml(getResources().getString(R.string.via_requisition_alert, periodRange)));
            } else if (periodOffsetMonthMmia == 1 && periodOffsetMonthVia == 0) {
                String periodRange = getPeriodRangeForIncompleteRequisition(Constants.MMIA_PROGRAM_CODE);
                txMissedRequisition.setText(Html.fromHtml(getResources().getString(R.string.mmia_requisition_alert, periodRange)));
            } else if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia > 1) {
                txMissedRequisition.setText(getResources().getString(R.string.via_requisition_for_multiple_periods_alert));
            } else if (periodOffsetMonthMmia > 1 && periodOffsetMonthVia == 0) {
                txMissedRequisition.setText(getResources().getString(R.string.mmia_requisition_for_multiple_periods_alert));
            } else {
                txMissedRequisition.setText(getResources().getString(R.string.via_and_mmia_requisitions_for_multiple_periods_alert));
            }
            this.setVisibility(VISIBLE);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String getPeriodRangeForIncompleteRequisition(String programCode) throws LMISException {
        Period period = requisitionPeriodService.generateNextPeriod(programCode, null);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM yyyy");
        return getResources().getString(R.string.missed_requisition_time_range, fmt.print(period.getBegin()), fmt.print(period.getEnd()));
    }
}
