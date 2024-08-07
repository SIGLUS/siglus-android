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
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.TranslationUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;

public class IncompleteRequisitionBanner extends NotificationBanner {

  @Inject
  RequisitionPeriodService requisitionPeriodService;

  protected Context context;

  public IncompleteRequisitionBanner(Context context) {
    super(context);
  }

  public IncompleteRequisitionBanner(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void init(Context context) {
    super.init(context);
    this.context = context;
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
    setIncompleteRequisitionBanner();
  }

  public void setIncompleteRequisitionBanner() {
    try {
      List<ReportTypeForm> incompleteReports = requisitionPeriodService.getIncompleteReports();
      if (incompleteReports.isEmpty()) {
        this.setVisibility(GONE);
      } else {
        TranslationUtil.translateReportName(incompleteReports);
        setNotificationMessage(buildTipMessage(incompleteReports));
        this.setVisibility(VISIBLE);
      }
    } catch (LMISException e) {
      Log.w("IncompleteBanner", e);
    }
  }

  private String buildTipMessage(List<ReportTypeForm> incompleteReports) {
    return context.getString(incompleteReports.size() == 1 ? R.string.incomplete_alert_message_for_single_report :
        R.string.incomplete_alert_message, FluentIterable.from(incompleteReports)
        .transform(ReportTypeForm::getName)
        .toList()
        .toString()
        .replace("[", "")
        .replace("]", ""));
  }
}
