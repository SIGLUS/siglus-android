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

import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_MR_INDUCTION;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_MR_INTENSIVE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_MR_MAINTENANCE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_SENSITIVE_INTENSIVE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_SENSITIVE_MAINTENANCE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_TABLE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_XR_INDUCTION;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_ADULT_XR_MAINTENANCE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_MR_INDUCTION;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_MR_INTENSIVE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_MR_MAINTENANCE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_SENSITIVE_INTENSIVE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_SENSITIVE_MAINTENANCE;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_XR_INDUCTION;
import static org.openlmis.core.constant.ReportConstants.KEY_TREATMENT_PEDIATRIC_XR_MAINTENANCE;

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
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.R;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.utils.SimpleTextWatcher;

public class MMTBTreatmentPhaseInfoList extends LinearLayout {

  private final Map<String, String> keyToFieldNameMap = new HashMap<>();
  private RotateTextView rtvAdult;
  private RotateTextView rtvPediatric;
  private final LinearLayout llAdultContainer;
  private final LinearLayout llPediatricContainer;
  private final LayoutInflater layoutInflater;
  private final List<EditText> editTexts = new ArrayList<>();

  public MMTBTreatmentPhaseInfoList(Context context) {
    this(context, null);
  }

  public MMTBTreatmentPhaseInfoList(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MMTBTreatmentPhaseInfoList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    layoutInflater = LayoutInflater.from(context);
    layoutInflater.inflate(R.layout.layout_mmtb_requisition_treatment_phase_info, this, true);
    llAdultContainer = findViewById(R.id.ll_adult_container);
    llPediatricContainer = findViewById(R.id.ll_pediatric_container);
    rtvAdult = findViewById(R.id.rtv_adult);
    rtvPediatric = findViewById(R.id.rtv_pediatric);
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
    llAdultContainer.removeAllViews();
    llPediatricContainer.removeAllViews();
    editTexts.clear();
    for (BaseInfoItem baseInfoItem : baseInfoItemList) {
      View itemView = layoutInflater.inflate(R.layout.item_mmtb_requisition_treatment_phase, this, false);
      TextView tvTitle = itemView.findViewById(R.id.tv_title);
      tvTitle.setText(keyToFieldNameMap.get(baseInfoItem.getName()));
      EditText etAmount = itemView.findViewById(R.id.et_treatment_phase_amount);
      if (baseInfoItem.getValue() != null) {
        etAmount.setText(String.valueOf(baseInfoItem.getValue()));
      }
      etAmount.addTextChangedListener(new EditTextWatcher(baseInfoItem));
      editTexts.add(etAmount);
      if (KEY_TREATMENT_ADULT_TABLE.equals(baseInfoItem.getTableName())) {
        itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_e5f2ff));
        etAmount.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_blue_no_border));
        llAdultContainer.addView(itemView);
      } else {
        itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_cce5ff));
        etAmount.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_light_blue_no_border));
        llPediatricContainer.addView(itemView);
      }
    }
    post(this::updateLeftHeader);
  }

  private void initKeyToFieldNameMap() {
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_SENSITIVE_INTENSIVE, "Sensível Intensiva");
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_SENSITIVE_MAINTENANCE, "Sensível Manutenção");
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_MR_INDUCTION, "MR Indução");
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_MR_INTENSIVE, "MR Intensiva");
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_MR_MAINTENANCE, "MR Manutenção");
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_XR_INDUCTION, "XR Indução");
    keyToFieldNameMap.put(KEY_TREATMENT_ADULT_XR_MAINTENANCE, "XR Manutenção");

    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_SENSITIVE_INTENSIVE, "Sensível Intensiva");
    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_SENSITIVE_MAINTENANCE, "Sensível Manutenção");
    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_MR_INDUCTION, "MR Indução");
    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_MR_INTENSIVE, "MR Intensiva");
    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_MR_MAINTENANCE, "MR Manutenção");
    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_XR_INDUCTION, "XR Intensiva");
    keyToFieldNameMap.put(KEY_TREATMENT_PEDIATRIC_XR_MAINTENANCE, "XR Manutenção");
  }

  private void updateLeftHeader() {
    LayoutParams adultParams = (LayoutParams) rtvAdult.getLayoutParams();
    adultParams.height = llAdultContainer.getHeight();
    rtvAdult.setLayoutParams(adultParams);
    LayoutParams childrenParams = (LayoutParams) rtvPediatric.getLayoutParams();
    childrenParams.height = llPediatricContainer.getHeight();
    rtvPediatric.setLayoutParams(childrenParams);
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
