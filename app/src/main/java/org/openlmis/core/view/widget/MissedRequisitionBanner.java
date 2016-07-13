package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
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
import org.openlmis.core.model.service.PeriodService;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;


public class MissedRequisitionBanner extends LinearLayout {
    @InjectView(R.id.tx_missed_requisition)
    TextView txMissedRequisition;

    @Inject
    PeriodService periodService;

    protected Context context;

    public MissedRequisitionBanner(Context context) {
        super(context);
        init(context);
    }

    public MissedRequisitionBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_missed_requisition_banner, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
        try {
            if (periodService.hasMissedPeriod("VIA") || periodService.hasMissedPeriod("MMIA")) {
                setMissedRequisitionBannerUI();
            } else {
                this.setVisibility(View.GONE);
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    private void setMissedRequisitionBannerUI() {
        try {
            int periodOffsetMonthMmia = periodService.getMissedPeriodOffsetMonth("MMIA");
            int periodOffsetMonthVia = periodService.getMissedPeriodOffsetMonth("VIA");
            if (periodOffsetMonthMmia == 1 && periodOffsetMonthVia == 1) {
                String periodRange = getPeriodRangeForMissedRequisition("VIA");
                txMissedRequisition.setText(getResources().getString(R.string.via_and_mmia_requisition_alert, periodRange));
            } else if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia == 1) {
                String periodRange = getPeriodRangeForMissedRequisition("VIA");
                txMissedRequisition.setText(getResources().getString(R.string.via_requisition_alert, periodRange));
            } else if (periodOffsetMonthMmia == 1 && periodOffsetMonthVia == 0) {
                String periodRange = getPeriodRangeForMissedRequisition("MMIA");
                txMissedRequisition.setText(getResources().getString(R.string.mmia_requisition_alert, periodRange));
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
    private String getPeriodRangeForMissedRequisition(String programCode) throws LMISException {
        Period period = periodService.generateNextPeriod(programCode, null);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM yyyy");
        return getResources().getString(R.string.missed_requisition_time_range, fmt.print(period.getBegin()), fmt.print(period.getEnd()));
    }
}
