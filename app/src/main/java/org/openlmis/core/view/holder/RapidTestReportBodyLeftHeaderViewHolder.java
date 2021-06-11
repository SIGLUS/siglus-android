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

package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import roboguice.inject.InjectView;

public class RapidTestReportBodyLeftHeaderViewHolder extends BaseViewHolder {

  @InjectView(R.id.rapid_body_left_title)
  TextView mTitle;

  public RapidTestReportBodyLeftHeaderViewHolder(View itemView) {
    super(itemView);
  }

  public void setUpHeader(RapidTestFormItemViewModel viewModel) {
    mTitle.setText(viewModel.getIssueReason().getDescription());
    if (isTotal(viewModel) || isAPEs(viewModel)) {
      mTitle.setBackgroundResource(R.drawable.border_top_rapid_test_body_left_ape);
    } else {
      mTitle.setBackgroundResource(R.drawable.border_top_rapid_test_body_left);
    }
  }

  public void setUpObservationLeftHeaderViewHolder() {
    mTitle.setBackgroundResource(R.drawable.border_top_rapid_test_body_left_ape);
    mTitle.setText(LMISApp.getContext().getResources().getString(R.string.hint_mmia_comment));
  }

  private boolean isTotal(RapidTestFormItemViewModel viewModel) {
    return viewModel.getIssueReason().getDescription()
        .equals(LMISApp.getInstance().getString(R.string.total));
  }

  private boolean isAPEs(RapidTestFormItemViewModel viewModel) {
    return viewModel.getIssueReason().getDescription()
        .equals(LMISApp.getInstance().getString(R.string.ape));
  }
}
