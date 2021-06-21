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
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.viewmodel.ALGridViewModel;
import org.openlmis.core.view.viewmodel.ALReportItemViewModel;
import org.openlmis.core.view.viewmodel.ALReportViewModel;
import roboguice.inject.InjectView;

public class ALReportViewHolder extends BaseViewHolder {

  private ALReportItemViewModel viewModel;
  private QuantityChangeListener quantityChangeListener;
  @InjectView(R.id.one_treatment)
  EditText oneTreatment;
  @InjectView(R.id.two_treatment)
  EditText twoTreatment;
  @InjectView(R.id.three_treatment)
  EditText threeTreatment;
  @InjectView(R.id.four_treatment)
  EditText fourTreatment;
  @InjectView(R.id.one_Stock)
  EditText oneStock;
  @InjectView(R.id.two_Stock)
  EditText twoStock;
  @InjectView(R.id.three_stock)
  EditText threeStock;
  @InjectView(R.id.four_stock)
  EditText fourStock;

  private final List<Pair<EditText, EditTextWatcher>> editPairWatcher = new ArrayList<>();
  private List<EditText> editTexts = new ArrayList<>();


  public ALReportViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final ALReportItemViewModel alReportViewModel,
      QuantityChangeListener changeListener,
      boolean editDisable) {
    this.viewModel = alReportViewModel;
    removeTextWatcher();
    setEditTextValue();
    if (viewModel.getItemType() != ALReportViewModel.ALItemType.TOTAL) {
      this.quantityChangeListener = changeListener;
      configureEditTextWatch();
      checkTips();
    } else {
      for (EditText editText : editTexts) {
        editText.setEnabled(false);
        editText.setError(null);
      }
    }
    if (editDisable) {
      for (EditText editText : editTexts) {
        editText.setEnabled(false);
        editText.setError(null);
      }
    }

  }

  public void setEditTextValue() {
    oneTreatment.setText(getValue(viewModel.getGridOne().getTreatmentsValue()));
    oneStock.setText(getValue(viewModel.getGridOne().getExistentStockValue()));
    twoTreatment.setText(getValue(viewModel.getGridTwo().getTreatmentsValue()));
    twoStock.setText(getValue(viewModel.getGridTwo().getExistentStockValue()));
    threeTreatment.setText(getValue(viewModel.getGridThree().getTreatmentsValue()));
    threeStock.setText(getValue(viewModel.getGridThree().getExistentStockValue()));
    fourTreatment.setText(getValue(viewModel.getGridFour().getTreatmentsValue()));
    fourStock.setText(getValue(viewModel.getGridFour().getExistentStockValue()));
    editTexts = Arrays
        .asList(oneTreatment, twoTreatment, threeTreatment, fourTreatment, oneStock, twoStock,
            threeStock, fourStock);
  }

  public void configureEditTextWatch() {
    for (EditText editText : editTexts) {
      editText.setEnabled(true);
      editText.setError(null);
      ALReportViewHolder.EditTextWatcher textWatcher = new ALReportViewHolder.EditTextWatcher(
          editText);
      editText.addTextChangedListener(textWatcher);
      editPairWatcher.add(new Pair<>(editText, textWatcher));
    }
  }

  public void checkTips() {
    if (!viewModel.isShowCheckTip()) {
      return;
    }
    for (EditText editText : editTexts) {
      if (editText.getText().length() == 0) {
        editText.setError(context.getString(R.string.hint_error_input));
        return;
      }
    }
  }

  private String getValue(Long vaule) {
    return vaule == null ? "" : String.valueOf(vaule.longValue());
  }

  class EditTextWatcher extends SimpleTextWatcher {

    private final EditText editText;

    public EditTextWatcher(EditText editText) {
      this.editText = editText;

    }

    @Override
    public void afterTextChanged(Editable etText) {
      ALGridViewModel gridViewModel = viewModel.getGridOne();
      ALGridViewModel.ALGridColumnCode gridColumnCode = ALGridViewModel.ALGridColumnCode.TREATMENT;
      switch (editText.getId()) {
        case R.id.one_treatment:
          gridViewModel = viewModel.getGridOne();
          break;
        case R.id.one_Stock:
          gridViewModel = viewModel.getGridOne();
          gridColumnCode = ALGridViewModel.ALGridColumnCode.EXISTENT_STOCK;
          break;
        case R.id.two_treatment:
          gridViewModel = viewModel.getGridTwo();
          break;
        case R.id.two_Stock:
          gridViewModel = viewModel.getGridTwo();
          gridColumnCode = ALGridViewModel.ALGridColumnCode.EXISTENT_STOCK;
          break;
        case R.id.three_treatment:
          gridViewModel = viewModel.getGridThree();
          break;
        case R.id.three_stock:
          gridViewModel = viewModel.getGridThree();
          gridColumnCode = ALGridViewModel.ALGridColumnCode.EXISTENT_STOCK;
          break;
        case R.id.four_treatment:
          gridViewModel = viewModel.getGridFour();
          break;
        case R.id.four_stock:
          gridViewModel = viewModel.getGridFour();
          gridColumnCode = ALGridViewModel.ALGridColumnCode.EXISTENT_STOCK;
          break;
        default:
          // do nothing
      }
      if (gridColumnCode == ALGridViewModel.ALGridColumnCode.TREATMENT) {
        gridViewModel.setTreatmentsValue(getEditValue(etText));
      } else {
        gridViewModel.setExistentStockValue(getEditValue(etText));
      }
      quantityChangeListener.updateTotal(gridViewModel.getColumnCode(), gridColumnCode);
    }

    private Long getEditValue(Editable etText) {
      Long editTextValue;
      try {
        editTextValue = Long.valueOf(etText.toString());
      } catch (NumberFormatException e) {
        editTextValue = null;
      }
      return editTextValue;
    }
  }

  private void removeTextWatcher() {
    for (Pair<EditText, EditTextWatcher> editText : editPairWatcher) {
      if (editText.first != null) {
        editText.first.removeTextChangedListener(editText.second);
      }
    }
    editPairWatcher.clear();
  }

  public interface QuantityChangeListener {

    void updateTotal(ALGridViewModel.ALColumnCode columnCode,
        ALGridViewModel.ALGridColumnCode gridColumnCode);
  }
}
