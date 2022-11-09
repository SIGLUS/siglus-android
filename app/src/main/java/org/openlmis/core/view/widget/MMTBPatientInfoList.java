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

import static org.openlmis.core.constant.ReportConstants.KEY_FREQUENCY_TOTAL;
import static org.openlmis.core.constant.ReportConstants.KEY_NEW_PATIENT_TOTAL;
import static org.openlmis.core.constant.ReportConstants.KEY_PROPHYLAXIS_TABLE_TOTAL;

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
  private final Map<String, List<BaseInfoItem>> tableNameToShowItemMap = new HashMap<>();
  private final Map<String, BaseInfoItem> tableNameToTotalItemMap = new HashMap<>();
  private final LinearLayout llNewPatientContainer;
  private final LinearLayout llProphylaxisPhasesContainer;
  private final LinearLayout llDispensationTypeContainer;
  private final TextView tvPatientTotal;
  private final TextView tvProphylaxisPhaseTotal;
  private final TextView tvDispensationTypeTotal;
  private final LayoutInflater layoutInflater;
  private List<BaseInfoItem> data;

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
    tvPatientTotal = findViewById(R.id.tv_patient_total);
    tvProphylaxisPhaseTotal = findViewById(R.id.tv_prophylaxis_phases_total);
    tvDispensationTypeTotal = findViewById(R.id.tv_dispensation_type_total);
    initKeyToFieldNameMap();
  }

  public void setData(List<BaseInfoItem> data) {
    this.data = data;
    editTexts.clear();
    tableNameToShowItemMap.clear();
    tableNameToTotalItemMap.clear();
    for (BaseInfoItem item : data) {
      if (isTotalItem(item)) {
        // total base info item
        tableNameToTotalItemMap.put(item.getName(), item);
      } else {
        List<BaseInfoItem> baseInfoItems = tableNameToShowItemMap.get(item.getTableName());
        List<BaseInfoItem> tableList = baseInfoItems == null ? new ArrayList<>() : baseInfoItems;
        tableList.add(item);
        tableNameToShowItemMap.put(item.getTableName(), tableList);
      }
    }
    if (tableNameToShowItemMap.containsKey(ReportConstants.KEY_NEW_PATIENT_TABLE)) {
      addTableView(tableNameToShowItemMap.get(ReportConstants.KEY_NEW_PATIENT_TABLE), llNewPatientContainer);
    }
    if (tableNameToShowItemMap.containsKey(ReportConstants.KEY_PROPHYLAXIS_TABLE)) {
      addTableView(tableNameToShowItemMap.get(ReportConstants.KEY_PROPHYLAXIS_TABLE), llProphylaxisPhasesContainer);
    }
    if (tableNameToShowItemMap.containsKey(ReportConstants.KEY_TYPE_OF_DISPENSATION_TABLE)) {
      addTableView(tableNameToShowItemMap.get(ReportConstants.KEY_TYPE_OF_DISPENSATION_TABLE),
          llDispensationTypeContainer);
    }
    if (!editTexts.isEmpty()) {
      editTexts.get(editTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
    }
    calculateTotal();
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
    if (ReportConstants.KEY_NEW_PATIENT_TABLE.equals(tableName)) {
      return i;
    } else if (ReportConstants.KEY_PROPHYLAXIS_TABLE.equals(tableName)) {
      return i + tableNameToShowItemMap.get(ReportConstants.KEY_NEW_PATIENT_TABLE).size();
    } else {
      return i + tableNameToShowItemMap.get(ReportConstants.KEY_NEW_PATIENT_TABLE).size()
          + tableNameToShowItemMap.get(ReportConstants.KEY_PROPHYLAXIS_TABLE).size();
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
    keyToFieldNameMap.put(ReportConstants.KEY_NEW_ADULT_SENSITIVE,
        getContext().getString(R.string.mmtb_new_adult_sensitive));
    keyToFieldNameMap.put(ReportConstants.KEY_NEW_ADULT_MR, getContext().getString(R.string.mmtb_new_adult_mr));
    keyToFieldNameMap.put(ReportConstants.KEY_NEW_ADULT_XR, getContext().getString(R.string.mmtb_new_adult_xr));
    keyToFieldNameMap.put(ReportConstants.KEY_NEW_CHILD_SENSITIVE,
        getContext().getString(R.string.mmtb_new_child_sensitive));
    keyToFieldNameMap.put(ReportConstants.KEY_NEW_CHILD_MR, getContext().getString(R.string.mmtb_new_child_mr));
    keyToFieldNameMap.put(ReportConstants.KEY_NEW_CHILD_XR, getContext().getString(R.string.mmtb_new_child_xr));

    keyToFieldNameMap.put(ReportConstants.KEY_START_PHASE, getContext().getString(R.string.mmtb_start_phase));
    keyToFieldNameMap.put(ReportConstants.KEY_CONTINUE_PHASE, getContext().getString(R.string.mmtb_continue_phase));
    keyToFieldNameMap.put(ReportConstants.KEY_FINAL_PHASE, getContext().getString(R.string.mmtb_final_phase));

    keyToFieldNameMap.put(ReportConstants.KEY_FREQUENCY_MONTHLY,
        getContext().getString(R.string.mmtb_frequency_monthly));
    keyToFieldNameMap.put(ReportConstants.KEY_FREQUENCY_QUARTERLY,
        getContext().getString(R.string.mmtb_frequency_quarterly));
  }

  private boolean isTotalItem(BaseInfoItem item) {
    return KEY_NEW_PATIENT_TOTAL.equals(item.getName()) || KEY_PROPHYLAXIS_TABLE_TOTAL.equals(item.getName())
        || KEY_FREQUENCY_TOTAL.equals(item.getName());
  }

  private class EditTextWatcher implements android.text.TextWatcher {

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
      calculateTotal();
    }
  }

  private void calculateTotal() {
    long patientTotal = 0;
    long prophylaxisPhaseTotal = 0;
    long dispensationTypeTotal = 0;
    for (BaseInfoItem infoItem : data) {
      if (isTotalItem(infoItem)) {
        continue;
      }
      Long itemValue = valueOfString(infoItem.getValue());
      switch (infoItem.getTableName()) {
        case ReportConstants.KEY_NEW_PATIENT_TABLE:
          if (itemValue != null) {
            patientTotal += itemValue;
          }
          break;
        case ReportConstants.KEY_PROPHYLAXIS_TABLE:
          if (itemValue != null) {
            prophylaxisPhaseTotal += itemValue;
          }
          break;
        case ReportConstants.KEY_TYPE_OF_DISPENSATION_TABLE:
          if (itemValue != null) {
            dispensationTypeTotal += itemValue;
          }
          break;
        default:
          break;
      }
    }
    String patientTotalValue = String.valueOf(patientTotal);
    tvPatientTotal.setText(patientTotalValue);
    setValue(tableNameToTotalItemMap.get(KEY_NEW_PATIENT_TOTAL), patientTotalValue);
    String prophylaxisTotal = String.valueOf(prophylaxisPhaseTotal);
    tvProphylaxisPhaseTotal.setText(prophylaxisTotal);
    setValue(tableNameToTotalItemMap.get(KEY_PROPHYLAXIS_TABLE_TOTAL), prophylaxisTotal);
    String dispensationTotal = String.valueOf(dispensationTypeTotal);
    tvDispensationTypeTotal.setText(dispensationTotal);
    setValue(tableNameToTotalItemMap.get(KEY_FREQUENCY_TOTAL), dispensationTotal);
  }

  private void setValue(BaseInfoItem infoItem, String value) {
    if (infoItem == null) {
      return;
    }
    infoItem.setValue(value);
  }

  private Long valueOfString(String value) {
    try {
      return Long.valueOf(value);
    } catch (Exception e) {
      return null;
    }
  }
}
