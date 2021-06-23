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

  private String attrTableTrav;
  private String attrTableTravKey;
  private String attrTableTravNew;
  private String attrTableTravNewKey;
  private String attrTableTravMaintenance;
  private String attrTableTravMaintenanceKey;
  private String attrTableTravAlteration;
  private String attrTableTravAlterationKey;
  private String attrTableTravTransit;
  private String attrTableTravTransitKey;
  private String attrTableTravTransfer;
  private String attrTableTravTransferKey;
  private String attrTablePatients;
  private String attrTablePatientsKey;
  private String attrTablePatientsAdults;
  private String attrTablePatientsAdultsKey;
  private String attrTablePatients0To4;
  private String attrTablePatients0To4Key;
  private String attrTablePatients5To9;
  private String attrTablePatients5To9Key;
  private String attrTablePatients10To14;
  private String attrTablePatients10To14Key;
  private String attrTableProphylaxis;
  private String attrTableProphylaxisKey;
  private String attrTableProphylaxisPpe;
  private String attrTableProphylaxisPpeKey;
  private String attrTableProphylaxisPrep;
  private String attrTableProphylaxisPrepKey;
  private String attrTableProphylaxisChild;
  private String attrTableProphylaxisChildKey;
  private String attrTableProphylaxisTotal;
  private String attrTableProphylaxisTotalKey;
  private String attrTableOrigin;

  private Context context;
  private final List<EditText> editTexts = new ArrayList<>();
  private LayoutInflater layoutInflater;
  private List<BaseInfoItem> dataList;
  private final Map<String, List<BaseInfoItem>> tableMap = new HashMap<>();
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
    attrTableTrav = getString(R.string.table_trav);
    attrTablePatients = getString(R.string.table_patients);
    attrTableProphylaxis = getString(R.string.table_prophylaxis);
    attrTableOrigin = getString(R.string.label_mmia_speed_info_header);
  }

  private void initTableKey() {
    attrTableTravKey = getString(R.string.table_arvt_key);
    attrTablePatientsKey = getString(R.string.table_patients_key);
    attrTableProphylaxisKey = getString(R.string.table_prophylaxy_key);
  }

  private void initItem() {
    attrTableTravNew = getString(R.string.table_trav_label_new);
    attrTableTravNewKey = getString(R.string.table_trav_label_new_key);
    attrTableTravMaintenance = getString(R.string.table_trav_label_maintenance);
    attrTableTravMaintenanceKey = getString(R.string.table_trav_label_maintenance_key);
    attrTableTravAlteration = getString(R.string.table_trav_label_alteration);
    attrTableTravAlterationKey = getString(R.string.table_trav_label_alteration_key);
    attrTableTravTransit = getString(R.string.table_trav_label_transit);
    attrTableTravTransitKey = getString(R.string.table_trav_label_transit_key);
    attrTableTravTransfer = getString(R.string.table_trav_label_transfers);
    attrTableTravTransferKey = getString(R.string.table_trav_label_transfers_key);
    attrTablePatientsAdults = getString(R.string.table_patients_adults);
    attrTablePatientsAdultsKey = getString(R.string.table_patients_adults_key);
    attrTablePatients0To4 = getString(R.string.table_patients_0to4);
    attrTablePatients0To4Key = getString(R.string.table_patients_0to4_key);
    attrTablePatients5To9 = getString(R.string.table_patients_5to9);
    attrTablePatients5To9Key = getString(R.string.table_patients_5to9_key);
    attrTablePatients10To14 = getString(R.string.table_patients_10to14);
    attrTablePatients10To14Key = getString(R.string.table_patients_10to14_key);
    attrTableProphylaxisPpe = getString(R.string.table_prophylaxis_ppe);
    attrTableProphylaxisPpeKey = getString(R.string.table_prophylaxis_ppe_key);
    attrTableProphylaxisPrep = getString(R.string.table_prophylaxis_prep);
    attrTableProphylaxisPrepKey = getString(R.string.table_prophylaxis_prep_key);
    attrTableProphylaxisChild = getString(R.string.table_prophylaxis_child);
    attrTableProphylaxisChildKey = getString(R.string.table_prophylaxis_child_key);
    attrTableProphylaxisTotal = getString(R.string.table_prophylaxis_total);
    attrTableProphylaxisTotalKey = getString(R.string.table_prophylaxis_total_key);

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
      addTableView(tableMap.get(attrTableTravKey), attrTableTrav);
      addTableView(tableMap.get(attrTablePatientsKey), attrTablePatients);
      addTableView(tableMap.get(attrTableProphylaxisKey), attrTableProphylaxis);
    } else {
      this.dataWithOldFormat = true;
      addTableView(dataList, attrTableOrigin);
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
    if (attrTableTrav.equals(tableName)) {
      return i;
    } else if (attrTablePatients.equals(tableName)) {
      return i + tableMap.get(attrTableTravKey).size();
    } else if (attrTableProphylaxis.equals(tableName)) {
      return i + tableMap.get(attrTableTravKey).size() + tableMap.get(attrTablePatientsKey)
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
    nameMap.put(attrTableTravNewKey, attrTableTravNew);
    nameMap.put(attrTableTravMaintenanceKey, attrTableTravMaintenance);
    nameMap.put(attrTableTravAlterationKey, attrTableTravAlteration);
    nameMap.put(attrTableTravTransitKey, attrTableTravTransit);
    nameMap.put(attrTableTravTransferKey, attrTableTravTransfer);
    nameMap.put(attrTablePatientsAdultsKey, attrTablePatientsAdults);
    nameMap.put(attrTablePatients0To4Key, attrTablePatients0To4);
    nameMap.put(attrTablePatients5To9Key, attrTablePatients5To9);
    nameMap.put(attrTablePatients10To14Key, attrTablePatients10To14);
    nameMap.put(attrTableProphylaxisPpeKey, attrTableProphylaxisPpe);
    nameMap.put(attrTableProphylaxisPrepKey, attrTableProphylaxisPrep);
    nameMap.put(attrTableProphylaxisChildKey, attrTableProphylaxisChild);
    nameMap.put(attrTableProphylaxisTotalKey, attrTableProphylaxisTotal);
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
