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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.R;
import org.openlmis.core.constant.ReportConstants;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.utils.SimpleTextWatcher;

public class MMTBDrugConsumptionInfoList extends LinearLayout {

  private final Map<String, String> keyToFieldNameMap = new HashMap<>();
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
    initKeyToFieldNameMap();
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

  public void setData(List<BaseInfoItem> baseInfoItemList) {
    llConsumptionContainer.removeAllViews();
    editTexts.clear();
    for (BaseInfoItem baseInfoItem : baseInfoItemList) {
      View itemView = layoutInflater.inflate(R.layout.item_mmtb_requisition_treatment_phase, this, false);
      TextView tvTitle = itemView.findViewById(R.id.tv_title);
      tvTitle.setText(keyToFieldNameMap.get(baseInfoItem.getName()));
      EditText etAmount = itemView.findViewById(R.id.et_treatment_phase_amount);
      if (baseInfoItem.getValue() != null) {
        etAmount.setText(baseInfoItem.getValue());
      }
      etAmount.addTextChangedListener(new EditTextWatcher(baseInfoItem));
      editTexts.add(etAmount);
      llConsumptionContainer.addView(itemView);
    }
    post(this::updateLeftHeader);
  }

  private void initKeyToFieldNameMap() {
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_ISONIAZIDA_100, "Isoniazida 100 mg");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_ISONIAZIDA_300, "Isoniazida 300 mg");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_LEVOFLOXACINA_100, "Levofloxacina 100 mg Disp");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_LEVOFLOXACINA_250, "Levofloxacina 250 mg Rev");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_RIFAPENTINA_300,
        "Rifapentina 300mg \n+Isoniazida 300mg");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_RIFAPENTINA_150, "Rifapentina 150 mg");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_PIRIDOXINA_25, "Piridoxina 25mg");
    keyToFieldNameMap.put(ReportConstants.KEY_PHARMACY_PRODUCT_PIRIDOXINA_50, "Piridoxina 50mg");
  }

  private void updateLeftHeader() {
    LayoutParams adultParams = (LayoutParams) rtvPharmacyOutpatient.getLayoutParams();
    adultParams.height = llConsumptionContainer.getHeight() + llTitleContainer.getHeight();
    rtvPharmacyOutpatient.setLayoutParams(adultParams);
  }

  private static class EditTextWatcher extends SimpleTextWatcher {

    private final BaseInfoItem item;

    public EditTextWatcher(BaseInfoItem item) {
      this.item = item;
    }

    @Override
    public void afterTextChanged(Editable editable) {
      item.setValue(editable.toString());
    }
  }
}
