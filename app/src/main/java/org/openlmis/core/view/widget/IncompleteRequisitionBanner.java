/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

  @SuppressWarnings("squid:S3776")
  public void setIncompleteRequisitionBanner() {
    try {
      ReportTypeForm mmiaReportTypeForm = reportTypeFormRepository
          .getReportType(Constants.MMIA_PROGRAM_CODE);
      ReportTypeForm viaReportTypeForm = reportTypeFormRepository
          .getReportType(Constants.VIA_PROGRAM_CODE);
      ReportTypeForm ptvReportTypeForm = reportTypeFormRepository
          .getReportType(Constants.PTV_REPORT);
      int periodOffsetMonthMmia = (mmiaReportTypeForm == null || !mmiaReportTypeForm.active)
          ? 0
          : requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE);
      int periodOffsetMonthVia = (viaReportTypeForm == null || !viaReportTypeForm.active)
          ? 0 : requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE);
      int periodOffsetMonthPTV = (ptvReportTypeForm == null || !ptvReportTypeForm.active)
          ? 0 : requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.PTV_PROGRAM_CODE);
      String tipMessage = "";

      if (periodOffsetMonthMmia == 0 && periodOffsetMonthVia == 0 && periodOffsetMonthPTV == 0) {
        this.setVisibility(View.GONE);
        return;
      } else if (periodOffsetMonthVia > 0 && (periodOffsetMonthPTV > 0
          || periodOffsetMonthMmia > 0)) {
        tipMessage = periodOffsetMonthMmia > 0 ? getResources()
            .getString(R.string.via_and_mmia_requisition_alert)
            : getResources().getString(R.string.via_and_ptv_requisition_alert);
      } else if (periodOffsetMonthVia > 0) {
        tipMessage = getResources().getString(R.string.via_requisition_alert);
      } else {
        tipMessage =
            periodOffsetMonthMmia > 0 ? getResources().getString(R.string.mmia_requisition_alert)
                : getResources().getString(R.string.ptv_requisition_alert);
      }
      txMissedRequisition.setText(tipMessage);
      this.setVisibility(VISIBLE);
    } catch (LMISException e) {
      Log.w("IncompleteBanner", e);
    }
  }

  @NonNull
  private String getPeriodRangeForIncompleteRequisition(String programCode) throws LMISException {
    Period period = requisitionPeriodService.generateNextPeriod(programCode, null);
    DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM yyyy");
    return getResources()
        .getString(R.string.missed_requisition_time_range, fmt.print(period.getBegin()),
            fmt.print(period.getEnd()));
  }
}
