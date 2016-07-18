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
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.service.PeriodService;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;


public class IncompleteRequisitionBanner extends LinearLayout {
    @InjectView(R.id.tx_incomplete_requisition)
    TextView txMissedRequisition;

    @Inject
    PeriodService periodService;

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
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_incomplete_requisition_banner)) {
            setIncompleteRequisitionBanner();
        } else {
            this.setVisibility(View.GONE);
        }
    }

    public void setIncompleteRequisitionBanner() {
        try {
            int periodOffsetMonthMmia = periodService.getIncompletePeriodOffsetMonth("MMIA");
            int periodOffsetMonthVia = periodService.getIncompletePeriodOffsetMonth("VIA");
            if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia == 0) {
                this.setVisibility(View.GONE);
            } else if (periodOffsetMonthMmia == 1 && periodOffsetMonthVia == 1) {
                String periodRange = getPeriodRangeForIncompleteRequisition("VIA");
                txMissedRequisition.setText(Html.fromHtml(getResources().getString(R.string.via_and_mmia_requisition_alert, periodRange)));
            } else if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia == 1) {
                String periodRange = getPeriodRangeForIncompleteRequisition("VIA");
                txMissedRequisition.setText(Html.fromHtml(getResources().getString(R.string.via_requisition_alert, periodRange)));
            } else if (periodOffsetMonthMmia == 1 && periodOffsetMonthVia == 0) {
                String periodRange = getPeriodRangeForIncompleteRequisition("MMIA");
                txMissedRequisition.setText(Html.fromHtml(getResources().getString(R.string.mmia_requisition_alert, periodRange)));
            } else if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia > 1) {
                txMissedRequisition.setText(getResources().getString(R.string.via_requisition_for_multiple_periods_alert));
            } else if (periodOffsetMonthMmia > 1 && periodOffsetMonthVia == 0) {
                txMissedRequisition.setText(getResources().getString(R.string.mmia_requisition_for_multiple_periods_alert));
            } else {
                txMissedRequisition.setText(getResources().getString(R.string.via_and_mmia_requisitions_for_multiple_periods_alert));
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String getPeriodRangeForIncompleteRequisition(String programCode) throws LMISException {
        Period period = periodService.generateNextPeriod(programCode, null);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM yyyy");
        return getResources().getString(R.string.missed_requisition_time_range, fmt.print(period.getBegin()), fmt.print(period.getEnd()));
    }
}
