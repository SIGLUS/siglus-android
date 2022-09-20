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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

public class MMTBPatientInfoList extends LinearLayout {

  private final Map<String, String> keyToFieldNameMap = new HashMap<>();
  private final List<EditText> editTexts = new ArrayList<>();
  private final Map<String, List<BaseInfoItem>> tableMap = new HashMap<>();
  private final LinearLayout llNewPatientContainer;
  private final LinearLayout llProphylaxisPhasesContainer;
  private final LinearLayout llDispensationTypeContainer;
  private final LayoutInflater layoutInflater;

  public MMTBPatientInfoList(Context context) {
    this(context, null);
  }

  public MMTBPatientInfoList(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MMTBPatientInfoList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    layoutInflater = LayoutInflater.from(context);
    layoutInflater.inflate(R.layout.layout_mmtb_requisition_patient_info, this, true);
    llNewPatientContainer = findViewById(R.id.ll_new_patient_container);
    llProphylaxisPhasesContainer = findViewById(R.id.ll_prophylaxis_phases_container);
    llDispensationTypeContainer = findViewById(R.id.ll_dispensation_type_container);
    initKeyToFieldNameMap();
  }

  public void setData(List<BaseInfoItem> data) {
    for (BaseInfoItem item : data) {
      List<BaseInfoItem> baseInfoItems = tableMap.get(item.getTableName());
      List<BaseInfoItem> tableList = baseInfoItems == null ? new ArrayList<>() : baseInfoItems;
      tableList.add(item);
      tableMap.put(item.getTableName(), tableList);
    }
    addTableView(tableMap.get(ReportConstants.KEY_MMTB_NEW_PATIENT_TABLE), llNewPatientContainer);
    addTableView(tableMap.get(ReportConstants.KEY_MMTB_FOLLOW_UP_PROPHYLAXIS_TABLE), llProphylaxisPhasesContainer);
    addTableView(tableMap.get(ReportConstants.KEY_MMTB_TYPE_OF_DISPENSATION_TABLE), llDispensationTypeContainer);
    editTexts.get(editTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
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

  private void addTableView(List<BaseInfoItem> list, ViewGroup container) {
    container.removeAllViews();
    for (int i = 0; i < list.size(); i++) {
      BaseInfoItem item = list.get(i);
      View itemView = layoutInflater.inflate(R.layout.item_mmtb_base_info, this, false);
      TextView tvName = itemView.findViewById(R.id.tv_name);
      EditText etValue = itemView.findViewById(R.id.et_value);
      tvName.setText(keyToFieldNameMap.get(item.getName()));
      editTexts.add(etValue);
      etValue.setText(item.getValue());
      etValue.addTextChangedListener(new EditTextWatcher(item));
      container.addView(itemView);
      etValue.setOnEditorActionListener(getOnEditorActionListener(getPosition(i, item.getTableName())));
    }
  }

  private int getPosition(int i, String tableName) {
    if (ReportConstants.KEY_MMTB_NEW_PATIENT_TABLE.equals(tableName)) {
      return i;
    } else if (ReportConstants.KEY_MMTB_FOLLOW_UP_PROPHYLAXIS_TABLE.equals(tableName)) {
      return i + tableMap.get(ReportConstants.KEY_MMTB_FOLLOW_UP_PROPHYLAXIS_TABLE).size();
    } else {
      return i + tableMap.get(ReportConstants.KEY_MMTB_NEW_PATIENT_TABLE).size()
          + tableMap.get(ReportConstants.KEY_MMTB_FOLLOW_UP_PROPHYLAXIS_TABLE).size();
    }
  }

  private TextView.OnEditorActionListener getOnEditorActionListener(int position) {
    return (v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_NEXT && (position + 1) < editTexts.size()) {
        editTexts.get(position + 1).requestFocus();
        return true;
      }
      return false;
    };
  }

  private void initKeyToFieldNameMap() {
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_NEW_ADULT_SENSITIVE,
        getContext().getString(R.string.mmtb_new_adult_sensitive));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_NEW_ADULT_MR, getContext().getString(R.string.mmtb_new_adult_mr));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_NEW_ADULT_XR, getContext().getString(R.string.mmtb_new_adult_xr));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_NEW_CHILD_SENSITIVE,
        getContext().getString(R.string.mmtb_new_child_sensitive));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_NEW_CHILD_MR, getContext().getString(R.string.mmtb_new_child_mr));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_NEW_CHILD_XR, getContext().getString(R.string.mmtb_new_child_xr));

    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_START_PHASE, getContext().getString(R.string.mmtb_start_phase));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_CONTINUE_PHASE,
        getContext().getString(R.string.mmtb_continue_phase));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_FINAL_PHASE, getContext().getString(R.string.mmtb_final_phase));

    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_FREQUENCY_MONTHLY,
        getContext().getString(R.string.mmtb_frequency_monthly));
    keyToFieldNameMap.put(ReportConstants.KEY_MMTB_FREQUENCY_QUARTERLY,
        getContext().getString(R.string.mmtb_frequency_quarterly));
  }

  private static class EditTextWatcher implements android.text.TextWatcher {

    private final BaseInfoItem item;

    public EditTextWatcher(BaseInfoItem item) {
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
