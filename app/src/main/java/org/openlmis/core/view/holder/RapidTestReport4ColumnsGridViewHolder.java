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
import android.widget.EditText;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;
import roboguice.inject.InjectView;

public class RapidTestReport4ColumnsGridViewHolder extends RapidTestReportGridViewHolder {

  @InjectView(R.id.et_positive_hiv_rapid_test_report_grid)
  EditText etPositiveHiv;

  @InjectView(R.id.et_positive_hiv_rapid_test_report_grid_total)
  TextView etPositiveHivTotal;

  @InjectView(R.id.et_positive_syphilis_rapid_test_report_grid)
  EditText etPositiveSyphilis;

  @InjectView(R.id.et_positive_syphilis_rapid_test_report_grid_total)
  TextView etPositiveSyphilisTotal;

  public RapidTestReport4ColumnsGridViewHolder(View itemView) {
    super(itemView);
  }

  @Override
  void populateDataForPositive(RapidTestFormGridViewModel viewModel) {
    (editable ? etPositiveHiv : etPositiveHivTotal).setText(viewModel.getPositiveHivValue());
    (editable ? etPositiveSyphilis : etPositiveSyphilisTotal)
        .setText(viewModel.getPositiveSyphilisValue());
  }

  @Override
  public void setBlankForPositiveTextView() {
    setCellBlankAndDisabled(etPositiveHivTotal);
    setCellBlankAndDisabled(etPositiveSyphilisTotal);
  }

  @Override
  public void setBlankForPositiveEditText() {
    setCellBlankAndDisabled(etPositiveHiv);
    viewModel.setPositiveHivValue("0");
    setCellBlankAndDisabled(etPositiveSyphilis);
    viewModel.setPositiveSyphilisValue("0");
  }

  @Override
  public void setEditTableForPositiveEditText() {
    setEditableForEditText(etPositiveHiv);
    setEditableForEditText(etPositiveSyphilis);
  }

  @Override
  void setTextWatcherForPositiveEditText() {
    setTextWatcherForEditText(etPositiveHiv);
    setTextWatcherForEditText(etPositiveSyphilis);
  }

  @Override
  void setTextColorForPositiveEditText() {
    setTextColorForEditText(etPositiveHiv);
    setTextColorForEditText(etPositiveSyphilis);
  }

  @Override
  void setTextColorForPositiveTextView() {
    setTextColorForTextView(etPositiveHiv, etPositiveHivTotal);
    setTextColorForTextView(etPositiveSyphilis, etPositiveSyphilisTotal);
  }

  @Override
  void setInvalidInput() {
    RapidTestGridColumnCode invalidColumn = viewModel.getInvalidColumn();
    if (invalidColumn != null) {
      switch (invalidColumn) {
        case CONSUMPTION:
          showError(etConsume);
          break;
        case POSITIVE_HIV:
          showError(etPositiveHiv);
          break;
        case POSITIVE_SYPHILIS:
          showError(etPositiveSyphilis);
          break;
        case UNJUSTIFIED:
          showError(etUnjustified);
          break;
        default:
          break;
      }
    }
  }

  private void showError(EditText etConsume) {
    etConsume.setError(getString(R.string.hint_error_input));
  }

  @Override
  void clearErrorForPositiveEditText() {
    clearErrorForEditText(etPositiveHiv);
    clearErrorForEditText(etPositiveSyphilis);
  }
}