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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;

public class MMIAPatientInfoList extends LinearLayout {

  private static String ATTR_TABLE_TRAV;
  private static String ATTR_TABLE_TRAV_KEY;
  private static String ATTR_TABLE_TRAV_NEW;
  private static String ATTR_TABLE_TRAV_NEW_KEY;
  private static String ATTR_TABLE_TRAV_MAINTENANCE;
  private static String ATTR_TABLE_TRAV_MAINTENANCE_KEY;
  private static String ATTR_TABLE_TRAV_ALTERATION;
  private static String ATTR_TABLE_TRAV_ALTERATION_KEY;
  private static String ATTR_TABLE_TRAV_TRANSIT;
  private static String ATTR_TABLE_TRAV_TRANSIT_KEY;
  private static String ATTR_TABLE_TRAV_TRANSFER;
  private static String ATTR_TABLE_TRAV_TRANSFER_KEY;
  private static String ATTR_TABLE_PATIENTS;
  private static String ATTR_TABLE_PATIENTS_KEY;
  private static String ATTR_TABLE_PATIENTS_ADULTS;
  private static String ATTR_TABLE_PATIENTS_ADULTS_KEY;
  private static String ATTR_TABLE_PATIENTS_0TO4;
  private static String ATTR_TABLE_PATIENTS_0TO4_KEY;
  private static String ATTR_TABLE_PATIENTS_5TO9;
  private static String ATTR_TABLE_PATIENTS_5TO9_KEY;
  private static String ATTR_TABLE_PATIENTS_10TO14;
  private static String ATTR_TABLE_PATIENTS_10TO14_KEY;
  private static String ATTR_TABLE_PROPHYLAXIS;
  private static String ATTR_TABLE_PROPHYLAXIS_KEY;
  private static String ATTR_TABLE_PROPHYLAXIS_PPE;
  private static String ATTR_TABLE_PROPHYLAXIS_PPE_KEY;
  private static String ATTR_TABLE_PROPHYLAXIS_PREP;
  private static String ATTR_TABLE_PROPHYLAXIS_PREP_KEY;
  private static String ATTR_TABLE_PROPHYLAXIS_CHILD;
  private static String ATTR_TABLE_PROPHYLAXIS_CHILD_KEY;
  private static String ATTR_TABLE_PROPHYLAXIS_TOTAL;
  private static String ATTR_TABLE_PROPHYLAXIS_TOTAL_KEY;
  private static String ATTR_TABLE_ORIGIN;

  private Context context;
  private final List<EditText> editTexts = new ArrayList<>();
  private LayoutInflater layoutInflater;
  private List<BaseInfoItem> dataList;
  private final Map<String, List<BaseInfoItem>> tableMap = new HashMap<>();


  private boolean hasDataChanged = false;
  private boolean dataWithOldFormat = false;

  public MMIAPatientInfoList(Context context) {
    super(context);
    init(context);
  }

  public MMIAPatientInfoList(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    setOrientation(LinearLayout.VERTICAL);
    layoutInflater = LayoutInflater.from(context);
    initTableName();
    initTableKey();
    initItem();
  }

  private void initTableName() {
    ATTR_TABLE_TRAV = getString(R.string.table_trav);
    ATTR_TABLE_PATIENTS = getString(R.string.table_patients);
    ATTR_TABLE_PROPHYLAXIS = getString(R.string.table_prophylaxis);
    ATTR_TABLE_ORIGIN = getString(R.string.label_mmia_speed_info_header);
  }

  private void initTableKey() {
    ATTR_TABLE_TRAV_KEY = getString(R.string.table_arvt_key);
    ATTR_TABLE_PATIENTS_KEY = getString(R.string.table_patients_key);
    ATTR_TABLE_PROPHYLAXIS_KEY = getString(R.string.table_prophylaxy_key);
  }

  private void initItem() {
    ATTR_TABLE_TRAV_NEW = getString(R.string.table_trav_label_new);
    ATTR_TABLE_TRAV_NEW_KEY = getString(R.string.table_trav_label_new_key);
    ATTR_TABLE_TRAV_MAINTENANCE = getString(R.string.table_trav_label_maintenance);
    ATTR_TABLE_TRAV_MAINTENANCE_KEY = getString(R.string.table_trav_label_maintenance_key);
    ATTR_TABLE_TRAV_ALTERATION = getString(R.string.table_trav_label_alteration);
    ATTR_TABLE_TRAV_ALTERATION_KEY = getString(R.string.table_trav_label_alteration_key);
    ATTR_TABLE_TRAV_TRANSIT = getString(R.string.table_trav_label_transit);
    ATTR_TABLE_TRAV_TRANSIT_KEY = getString(R.string.table_trav_label_transit_key);
    ATTR_TABLE_TRAV_TRANSFER = getString(R.string.table_trav_label_transfers);
    ATTR_TABLE_TRAV_TRANSFER_KEY = getString(R.string.table_trav_label_transfers_key);
    ATTR_TABLE_PATIENTS_ADULTS = getString(R.string.table_patients_adults);
    ATTR_TABLE_PATIENTS_ADULTS_KEY = getString(R.string.table_patients_adults_key);
    ATTR_TABLE_PATIENTS_0TO4 = getString(R.string.table_patients_0to4);
    ATTR_TABLE_PATIENTS_0TO4_KEY = getString(R.string.table_patients_0to4_key);
    ATTR_TABLE_PATIENTS_5TO9 = getString(R.string.table_patients_5to9);
    ATTR_TABLE_PATIENTS_5TO9_KEY = getString(R.string.table_patients_5to9_key);
    ATTR_TABLE_PATIENTS_10TO14 = getString(R.string.table_patients_10to14);
    ATTR_TABLE_PATIENTS_10TO14_KEY = getString(R.string.table_patients_10to14_key);
    ATTR_TABLE_PROPHYLAXIS_PPE = getString(R.string.table_prophylaxis_ppe);
    ATTR_TABLE_PROPHYLAXIS_PPE_KEY = getString(R.string.table_prophylaxis_ppe_key);
    ATTR_TABLE_PROPHYLAXIS_PREP = getString(R.string.table_prophylaxis_prep);
    ATTR_TABLE_PROPHYLAXIS_PREP_KEY = getString(R.string.table_prophylaxis_prep_key);
    ATTR_TABLE_PROPHYLAXIS_CHILD = getString(R.string.table_prophylaxis_child);
    ATTR_TABLE_PROPHYLAXIS_CHILD_KEY = getString(R.string.table_prophylaxis_child_key);
    ATTR_TABLE_PROPHYLAXIS_TOTAL = getString(R.string.table_prophylaxis_total);
    ATTR_TABLE_PROPHYLAXIS_TOTAL_KEY = getString(R.string.table_prophylaxis_total_key);

  }

  private String getString(int id) {
    return getResources().getString(id);
  }

  public void initView(List<BaseInfoItem> list) {
    this.dataList = list;
    for (BaseInfoItem item : list) {
      List<BaseInfoItem> tableList = tableMap.get(item.getTableName()) == null ? new ArrayList<>()
          : tableMap.get(item.getTableName());
      tableList.add(item);
      tableMap.put(item.getTableName(), tableList);
    }

    addItemView();
    editTexts.get(editTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
  }

  private void addItemView() {
    if (tableMap.size() != 1) {
      addTableView(tableMap.get(ATTR_TABLE_TRAV_KEY), ATTR_TABLE_TRAV);
      addTableView(tableMap.get(ATTR_TABLE_PATIENTS_KEY), ATTR_TABLE_PATIENTS);
      addTableView(tableMap.get(ATTR_TABLE_PROPHYLAXIS_KEY), ATTR_TABLE_PROPHYLAXIS);
    } else {
      this.dataWithOldFormat = true;
      addTableView(dataList, ATTR_TABLE_ORIGIN);
    }
  }

  private void addTableView(List<BaseInfoItem> list, String tableName) {
    TextView tableHeader = (TextView) layoutInflater
        .inflate(R.layout.item_mmia_requisitions_bottom, this, false);
    tableHeader.setText(tableName);
    addView(tableHeader);
    if (dataWithOldFormat) {
      View view = layoutInflater.inflate(R.layout.item_mmia_info, this, false);
      TextView textView = view.findViewById(R.id.tv_name);
      EditText editText = view.findViewById(R.id.et_value);
      editText.setBackgroundResource(R.color.color_mmia_info_name);
      editText.setTextColor(getResources().getColor(R.color.color_text_secondary));
      textView.setText(R.string.patient_column_name_left);
      editText.setText(R.string.patient_column_name_right);
      addView(view);
    }
    sortedByDisplayOrder(list);
    for (int i = 0; i < list.size(); i++) {
      addTableViewItem(list.get(i), getPosition(i, tableName));
    }
  }

  private int getPosition(int i, String tableName) {
    if (ATTR_TABLE_TRAV.equals(tableName)) {
      return i;
    } else if (ATTR_TABLE_PATIENTS.equals(tableName)) {
      return i + tableMap.get(ATTR_TABLE_TRAV_KEY).size();
    } else if (ATTR_TABLE_PROPHYLAXIS.equals(tableName)) {
      return i + tableMap.get(ATTR_TABLE_TRAV_KEY).size() + tableMap.get(ATTR_TABLE_PATIENTS_KEY)
          .size();
    } else {
      return i;
    }
  }

  private void sortedByDisplayOrder(List<BaseInfoItem> list) {
    Collections.sort(list, (o1, o2) -> o1.getDisplayOrder() - o2.getDisplayOrder());
  }

  private Map<String, String> nameMap() {
    Map<String, String> nameMap = new HashMap<>();
    nameMap.put(ATTR_TABLE_TRAV_NEW_KEY, ATTR_TABLE_TRAV_NEW);
    nameMap.put(ATTR_TABLE_TRAV_MAINTENANCE_KEY, ATTR_TABLE_TRAV_MAINTENANCE);
    nameMap.put(ATTR_TABLE_TRAV_ALTERATION_KEY, ATTR_TABLE_TRAV_ALTERATION);
    nameMap.put(ATTR_TABLE_TRAV_TRANSIT_KEY, ATTR_TABLE_TRAV_TRANSIT);
    nameMap.put(ATTR_TABLE_TRAV_TRANSFER_KEY, ATTR_TABLE_TRAV_TRANSFER);
    nameMap.put(ATTR_TABLE_PATIENTS_ADULTS_KEY, ATTR_TABLE_PATIENTS_ADULTS);
    nameMap.put(ATTR_TABLE_PATIENTS_0TO4_KEY, ATTR_TABLE_PATIENTS_0TO4);
    nameMap.put(ATTR_TABLE_PATIENTS_5TO9_KEY, ATTR_TABLE_PATIENTS_5TO9);
    nameMap.put(ATTR_TABLE_PATIENTS_10TO14_KEY, ATTR_TABLE_PATIENTS_10TO14);
    nameMap.put(ATTR_TABLE_PROPHYLAXIS_PPE_KEY, ATTR_TABLE_PROPHYLAXIS_PPE);
    nameMap.put(ATTR_TABLE_PROPHYLAXIS_PREP_KEY, ATTR_TABLE_PROPHYLAXIS_PREP);
    nameMap.put(ATTR_TABLE_PROPHYLAXIS_CHILD_KEY, ATTR_TABLE_PROPHYLAXIS_CHILD);
    nameMap.put(ATTR_TABLE_PROPHYLAXIS_TOTAL_KEY, ATTR_TABLE_PROPHYLAXIS_TOTAL);
    return nameMap;
  }

  private void addTableViewItem(BaseInfoItem item, int position) {
    View view = layoutInflater.inflate(R.layout.item_mmia_info, this, false);
    TextView textView = view.findViewById(R.id.tv_name);
    EditText editText = view.findViewById(R.id.et_value);

    Map<String, String> nameMap = nameMap();
    textView.setText(dataWithOldFormat ? item.getName() : nameMap.get(item.getName()));
    editTexts.add(editText);
    editText.setText(item.getValue());
    editText.addTextChangedListener(new EditTextWatcher(item));
    setTotalViewBackground(item, editText);
    addView(view);
    editText.setOnEditorActionListener(getOnEditorActionListener(position));
  }

  private TextView.OnEditorActionListener getOnEditorActionListener(int position) {
    return (v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_NEXT) {
        if ((position + 1) < editTexts.size()) {
          editTexts.get(position + 1).requestFocus();
          return true;
        }
      } else {
        return false;
      }
      return false;
    };
  }

  private void setTotalViewBackground(BaseInfoItem item, EditText etValue) {
    if (isTotalInfoView(item)) {
      etValue.setBackgroundResource(R.color.color_mmia_speed_list_header);
    }
  }

  public List<BaseInfoItem> getDataList() {
    return dataList;
  }

  public boolean hasEmptyField() {
    for (BaseInfoItem item : dataList) {
      if (StringUtils.isEmpty(item.getValue())) {
        return true;
      }
    }
    return false;
  }

  class EditTextWatcher implements android.text.TextWatcher {

    private final BaseInfoItem item;

    public EditTextWatcher(BaseInfoItem item) {
      this.item = item;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
      hasDataChanged = true;
      item.setValue(editable.toString());
    }
  }

  public long getTotal() {
    long totalRegimenNumber = 0;
    for (BaseInfoItem item : dataList) {
      if (isTotalInfoView(item) || TextUtils.isEmpty(item.getValue())) {
        continue;
      }
      try {
        totalRegimenNumber += Long.parseLong(item.getValue());
      } catch (NumberFormatException e) {
        new LMISException(e, "MMIAInfoList.getTotal").reportToFabric();
      }
    }
    return totalRegimenNumber;
  }

  private boolean isTotalInfoView(BaseInfoItem item) {
    if (dataWithOldFormat) {
      return getString(R.string.label_total_month_dispense).equals(item.getName());
    } else {
      return (getString(R.string.table_prophylaxis_total_key).equals(item.getName())
          || getString(R.string.table_patients_key).equals(item.getName()));
    }
  }

  public boolean isCompleted() {
    for (EditText editText : editTexts) {
      if (TextUtils.isEmpty(editText.getText().toString())) {
        editText.setError(context.getString(R.string.hint_error_input));
        editText.requestFocus();
        return false;
      }
    }
    return true;
  }

}
