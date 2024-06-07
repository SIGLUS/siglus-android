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

import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.CONSUMPTION;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE_HIV;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE_SYPHILIS;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.UNJUSTIFIED;

import android.text.Editable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import org.openlmis.core.LMISApp;
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
  boolean editable;
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
    setInvalidInput();
  }

  private void updateGridViewHaveValueAlert() {
    if (viewModel.isNeedAddGridViewWarning()) {
      warningLinerLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.border_bg_red));
    }
  }

  public void setEditable(Boolean editable) {
    if (Boolean.TRUE.equals(editable)) {
      setEditableForEditText(etConsume);
      setEditTableForPositiveEditText();
      setEditableForEditText(etUnjustified);
    }
  }

  public void setEditTableForPositiveEditText() {
    setEditableForEditText(etPositive);
  }

  void setEditableForEditText(EditText editText) {
    editText.setFocusable(true);
    editText.setOnFocusChangeListener(getOnFocusChangeListener());
  }

  public void populateData(RapidTestFormGridViewModel viewModel) {
    (editable ? etConsume : etConsumeTotal).setText(viewModel.getConsumptionValue());
    populateDataForPositive(viewModel);
    (editable ? etUnjustified : etUnjustifiedTotal).setText(viewModel.getUnjustifiedValue());
  }

  void populateDataForPositive(RapidTestFormGridViewModel viewModel) {
    (editable ? etPositive : etPositiveTotal).setText(viewModel.getPositiveValue());
  }

  private void setTextWatcher() {
    if (editable) {
      setTextWatcherForEditText(etConsume);
      setTextWatcherForPositiveEditText();
      setTextWatcherForEditText(etUnjustified);
    }
  }

  void setTextWatcherForPositiveEditText() {
    setTextWatcherForEditText(etPositive);
  }

  void setTextWatcherForEditText(EditText editText) {
    TextWatcher textWatcherConsume = new TextWatcher(editText);
    editText.addTextChangedListener(textWatcherConsume);
  }

  private void updateAlert() {
    if (editable && !viewModel.validate()) {
      setTextColorForEditText(etConsume);
      setTextColorForPositiveEditText();
      setTextColorForEditText(etUnjustified);
    } else {
      setTextColorForTextView(etConsume, etConsumeTotal);
      setTextColorForPositiveTextView();
      setTextColorForTextView(etUnjustified, etUnjustifiedTotal);
    }
  }

  void setTextColorForPositiveTextView() {
    setTextColorForTextView(etPositive, etPositiveTotal);
  }

  void setTextColorForTextView(EditText editableView, TextView uneditableView) {
    (editable ? editableView : uneditableView)
        .setTextColor(ContextCompat.getColor(context, R.color.color_black));
  }

  void setTextColorForPositiveEditText() {
    setTextColorForEditText(etPositive);
  }

  void setTextColorForEditText(EditText etPositive) {
    etPositive.setTextColor(ContextCompat.getColor(context, R.color.color_red));
  }

  void setInvalidInput() {
    if (viewModel.getInvalidColumn() != null) {
      switch (viewModel.getInvalidColumn()) {
        case CONSUMPTION:
          etConsume.setError(getString(R.string.hint_error_input));
          break;
        case POSITIVE:
          etPositive.setError(getString(R.string.hint_error_input));
          break;
        case UNJUSTIFIED:
          etUnjustified.setError(getString(R.string.hint_error_input));
          break;
        default:
          break;
      }
    }
  }

  OnFocusChangeListener getOnFocusChangeListener() {
    return (v, hasFocus) -> {
      if (hasFocus) {
        clearError();
      }
    };
  }

  private void clearError() {
    clearErrorForEditText(etConsume);
    clearErrorForPositiveEditText();
    clearErrorForEditText(etUnjustified);
  }

  void clearErrorForPositiveEditText() {
    clearErrorForEditText(etPositive);
  }

  void clearErrorForEditText(EditText editText) {
    editText.setError(null);
  }

  String getString(int id) {
    return LMISApp.getContext().getString(id);
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

  class TextWatcher extends SingleTextWatcher {

    private final EditText editText;

    public TextWatcher(EditText editText) {
      this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable s) {
      RapidTestGridColumnCode gridColumnCode = switchEditIdToGridColumn(editText);
      viewModel.setValue(gridColumnCode, s.toString());
      if (Boolean.FALSE.equals(viewModel.getIsAPE())) {
        updateTotal(gridColumnCode);
      }
      updateAlert();
    }

    private RapidTestGridColumnCode switchEditIdToGridColumn(EditText editText) {
      RapidTestGridColumnCode column = UNJUSTIFIED;
      switch (editText.getId()) {
        case R.id.et_consume_rapid_test_report_grid:
          column = CONSUMPTION;
          break;
        case R.id.et_positive_rapid_test_report_grid:
          column = POSITIVE;
          break;
        case R.id.et_positive_hiv_rapid_test_report_grid:
          column = POSITIVE_HIV;
          break;
        case R.id.et_positive_syphilis_rapid_test_report_grid:
          column = POSITIVE_SYPHILIS;
          break;
        default:
          // do nothing
      }
      return column;
    }

  }
}
