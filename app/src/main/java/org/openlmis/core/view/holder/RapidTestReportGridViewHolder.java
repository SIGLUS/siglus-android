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

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;
import roboguice.inject.InjectView;

public class RapidTestReportGridViewHolder extends BaseViewHolder {

  @InjectView(R.id.et_consume_rapid_test_report_grid)
  EditText etConsume;

  @InjectView(R.id.et_consume_rapid_test_report_grid_total)
  TextView etConsumeTotal;

  @InjectView(R.id.et_positive_rapid_test_report_grid)
  EditText etPositive;

  @InjectView(R.id.et_positive_rapid_test_report_grid_total)
  TextView etPositiveTotal;

  @InjectView(R.id.et_unjustified_rapid_test_report_grid)
  EditText etUnjustified;

  @InjectView(R.id.et_unjustified_rapid_test_report_grid_total)
  TextView etUnjustifiedTotal;

  @InjectView(R.id.et_warning_border)
  LinearLayout warningLinerLayout;

  RapidTestFormGridViewModel viewModel;
  private boolean editable;
  private QuantityChangeListener quantityChangeListener;

  public RapidTestReportGridViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(RapidTestFormGridViewModel viewModel, boolean editable,
      QuantityChangeListener quantityChangeListener) {
    this.viewModel = viewModel;
    this.editable = editable;
    this.quantityChangeListener = quantityChangeListener;
    populateData(viewModel);
    setEditable(editable);
    setTextWatcher();
    updateAlert();
    updateGridViewHaveValueAlert();
  }

  private void updateGridViewHaveValueAlert() {
    if (viewModel.isNeedAddGridViewWarning()) {
      warningLinerLayout
          .setBackground(context.getResources().getDrawable(R.drawable.border_bg_red));
    }
  }

  public void setEditable(Boolean editable) {
    if (editable) {
      etConsume.setFocusable(editable);
      etPositive.setFocusable(editable);
      etUnjustified.setFocusable(editable);
    }
  }

  public void populateData(RapidTestFormGridViewModel viewModel) {
    (editable ? etConsume : etConsumeTotal).setText(viewModel.getConsumptionValue());
    (editable ? etPositive : etPositiveTotal).setText(viewModel.getPositiveValue());
    (editable ? etUnjustified : etUnjustifiedTotal).setText(viewModel.getUnjustifiedValue());
  }

  private void setTextWatcher() {
    if (editable) {
      TextWatcher textWatcherConsume = new TextWatcher(etConsume);
      TextWatcher textWatcherPositive = new TextWatcher(etPositive);
      TextWatcher textWatcherUnjustified = new TextWatcher(etUnjustified);
      etConsume.addTextChangedListener(textWatcherConsume);
      etPositive.addTextChangedListener(textWatcherPositive);
      etUnjustified.addTextChangedListener(textWatcherUnjustified);
    }
  }

  private void updateAlert() {
    if (editable && !viewModel.validate()) {
      etPositive.setTextColor(context.getResources().getColor(R.color.color_red));
      etConsume.setTextColor(context.getResources().getColor(R.color.color_red));
      etUnjustified.setTextColor(context.getResources().getColor(R.color.color_red));
    } else {
      (editable ? etPositive : etPositiveTotal)
          .setTextColor(context.getResources().getColor(R.color.color_black));
      (editable ? etConsume : etConsumeTotal)
          .setTextColor(context.getResources().getColor(R.color.color_black));
      (editable ? etUnjustified : etUnjustifiedTotal)
          .setTextColor(context.getResources().getColor(R.color.color_black));
    }
  }

  class TextWatcher extends SingleTextWatcher {

    private final EditText editText;

    public TextWatcher(EditText editText) {
      this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable s) {
      RapidTestGridColumnCode gridColumnCode = switchEditIdToGridColumn(editText);
      viewModel.setValue(gridColumnCode, s.toString());
      if (!viewModel.getIsAPE()) {
        updateTotal(gridColumnCode);
      }
      updateAlert();
    }

    private RapidTestGridColumnCode switchEditIdToGridColumn(EditText editText) {
      RapidTestGridColumnCode column = RapidTestGridColumnCode.UNJUSTIFIED;
      switch (editText.getId()) {
        case R.id.et_consume_rapid_test_report_grid:
          column = RapidTestGridColumnCode.CONSUMPTION;
          break;
        case R.id.et_positive_rapid_test_report_grid:
          column = RapidTestGridColumnCode.POSITIVE;
          break;
        case R.id.et_unjustified_rapid_test_report_grid:
          column = RapidTestGridColumnCode.UNJUSTIFIED;
          break;
        default:
          // do nothing
      }
      return column;
    }

  }

  public void updateTotal(RapidTestGridColumnCode gridColumnCode) {
    if (!isInTotalRow()) {
      quantityChangeListener.updateTotal(viewModel.getColumnCode(), gridColumnCode);
    }
  }

  public boolean isInTotalRow() {
    return quantityChangeListener == null;
  }

  public interface QuantityChangeListener {

    void updateTotal(RapidTestFormGridViewModel.ColumnCode columnCode,
        RapidTestGridColumnCode gridColumnCode);
  }
}
