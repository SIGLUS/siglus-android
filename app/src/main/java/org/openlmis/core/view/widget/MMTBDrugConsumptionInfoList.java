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
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.openlmis.core.R;

public class MMTBDrugConsumptionInfoList extends LinearLayout {

  private static final List<TreatmentPhaseItem> mockData = Collections.unmodifiableList(Arrays.asList(
      new TreatmentPhaseItem("Consumption of drugs 1", ""),
      new TreatmentPhaseItem("Consumption of drugs 2", ""),
      new TreatmentPhaseItem("Consumption of drugs 3", ""),
      new TreatmentPhaseItem("Consumption of drugs 4", ""),
      new TreatmentPhaseItem("Consumption of drugs 5 Consumption of drugs 5", ""),
      new TreatmentPhaseItem("Consumption of drugs 6", ""),
      new TreatmentPhaseItem("Consumption of drugs 7", ""),
      new TreatmentPhaseItem("Consumption of drugs 8", "")
  ));

  private final TextView rtvPharmacyOutpatient;
  private final LinearLayout llConsumptionContainer;
  private final LinearLayout llTitleContainer;
  private final LayoutInflater layoutInflater;
  private final List<EditText> editTexts = new ArrayList<>();

  public MMTBDrugConsumptionInfoList(Context context) {
    this(context, null);
  }

  public MMTBDrugConsumptionInfoList(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MMTBDrugConsumptionInfoList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    layoutInflater = LayoutInflater.from(context);
    layoutInflater.inflate(R.layout.layout_mmtb_requisition_drug_consumption_info, this, true);
    llConsumptionContainer = findViewById(R.id.ll_consumption_container);
    llTitleContainer = findViewById(R.id.ll_title_container);
    rtvPharmacyOutpatient = findViewById(R.id.rtv_pharmacy_outpatient);
  }

  public boolean isCompleted() {
    for (EditText editText : editTexts) {
      if (TextUtils.isEmpty(editText.getText().toString())) {
        editText.setError(getContext().getString(R.string.hint_error_input));
        editText.requestFocus();
        return false;
      }
    }
    return true;
  }

  public void setData() {
    llConsumptionContainer.removeAllViews();
    editTexts.clear();
    for (TreatmentPhaseItem phaseItem : mockData) {
      View itemView = layoutInflater.inflate(R.layout.item_mmtb_requisition_treatment_phase, this, false);
      TextView tvTitle = itemView.findViewById(R.id.tv_title);
      tvTitle.setText(phaseItem.name);
      EditText etAmount = itemView.findViewById(R.id.et_treatment_phase_amount);
      etAmount.setText(phaseItem.value);
      etAmount.addTextChangedListener(new EditTextWatcher(phaseItem));
      editTexts.add(etAmount);
      llConsumptionContainer.addView(itemView);
    }
    post(this::updateLeftHeader);
  }

  private void updateLeftHeader() {
    LayoutParams adultParams = (LayoutParams) rtvPharmacyOutpatient.getLayoutParams();
    adultParams.height = llConsumptionContainer.getHeight() + llTitleContainer.getHeight();
    rtvPharmacyOutpatient.setLayoutParams(adultParams);
  }

  @AllArgsConstructor
  @Data
  private static class TreatmentPhaseItem {

    private String name;
    private String value;
  }

  private static class EditTextWatcher implements android.text.TextWatcher {

    private final TreatmentPhaseItem item;

    public EditTextWatcher(TreatmentPhaseItem item) {
      this.item = item;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      // do nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      // do nothing
    }

    @Override
    public void afterTextChanged(Editable editable) {
      item.setValue(editable.toString());
    }
  }
}
