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

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMIAInfoList extends LinearLayout {
    private Context context;
    private EditText totalPatientsView = null;
    private List<EditText> editTexts = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private List<BaseInfoItem> dataList;
    private Map<String, List<BaseInfoItem>> tableMap = new HashMap<>();
    private String ATTR_TABLE_TRAV;
    private String ATTR_TABLE_DISPENSED;
    private String ATTR_TABLE_PATIENTS;
    private String ATTR_TABLE_PROPHYLAXIS;

    private String ATTR_TABLE_ORIGIN;

    private boolean hasDataChanged = false;

    public MMIAInfoList(Context context) {
        super(context);
        init(context);
    }

    public MMIAInfoList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditText getPatientTotalView() {
        return totalPatientsView;
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = LayoutInflater.from(context);
        initTableName();
    }

    private void initTableName() {
        ATTR_TABLE_TRAV = getString(R.string.table_trav);
        ATTR_TABLE_DISPENSED = getString(R.string.table_dispensed);
        ATTR_TABLE_PATIENTS = getString(R.string.table_patients);
        ATTR_TABLE_PROPHYLAXIS = getString(R.string.table_prophylaxis);
        ATTR_TABLE_ORIGIN = getString(R.string.label_mmia_speed_info_header);
    }

    private String getString(int id) {
        return getResources().getString(id);
    }

    public void initView(List<BaseInfoItem> list) {
        this.dataList = list;
        for (BaseInfoItem item : list) {
            List<BaseInfoItem> tableList = tableMap.get(item.getTableName()) == null ? new ArrayList<>() : tableMap.get(item.getTableName());
            tableList.add(item);
            tableMap.put(item.getTableName(), tableList);
        }

        addItemView();
        editTexts.get(editTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    private void addItemView() {
        if (tableMap.size() != 1) {
            addTableView(tableMap.get(ATTR_TABLE_TRAV), ATTR_TABLE_TRAV);
            addTableView(tableMap.get(ATTR_TABLE_DISPENSED), ATTR_TABLE_DISPENSED);
            addTableView(tableMap.get(ATTR_TABLE_PATIENTS), ATTR_TABLE_PATIENTS);
            addTableView(tableMap.get(ATTR_TABLE_PROPHYLAXIS), ATTR_TABLE_PROPHYLAXIS);
        } else {
            addTableView(dataList, ATTR_TABLE_ORIGIN);
        }
    }

    private void addTableView(List<BaseInfoItem> list, String tableName) {
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.item_mmia_requisitions_bottom, this, false);
        TextView tableHeader = (TextView) linearLayout.findViewById(R.id.header);
        tableHeader.setText(tableName);
        linearLayout.removeView(tableHeader);
        addView(tableHeader);
        sortedByDisplayOrder(list);
        for (int i = 0; i < list.size(); i++) {
            addTableViewItem(list.get(i), getPosition(i, tableName));
        }
    }

    private int getPosition(int i, String tableName) {
        if (ATTR_TABLE_TRAV.equals(tableName)) {
            return i;
        } else if (ATTR_TABLE_DISPENSED.equals(tableName)) {
            return i + tableMap.get(ATTR_TABLE_TRAV).size();
        } else if (ATTR_TABLE_PATIENTS.equals(tableName)) {
            return i + tableMap.get(ATTR_TABLE_TRAV).size() + tableMap.get(ATTR_TABLE_DISPENSED).size();
        } else if (ATTR_TABLE_PROPHYLAXIS.equals(tableName)) {
            return i + tableMap.get(ATTR_TABLE_TRAV).size() + tableMap.get(ATTR_TABLE_DISPENSED).size()
                    + tableMap.get(ATTR_TABLE_PATIENTS).size();
        } else {
            return i;
        }
    }

    private void sortedByDisplayOrder(List<BaseInfoItem> list) {
        Collections.sort(list, (o1, o2) -> o1.getDisplayOrder() - o2.getDisplayOrder());
    }


    private void addTableViewItem(BaseInfoItem item, int position) {
        View view = layoutInflater.inflate(R.layout.item_mmia_info, this, false);
        TextView textView = (TextView) view.findViewById(R.id.tv_name);
        EditText editText = (EditText) view.findViewById(R.id.et_value);

        textView.setText(item.getName());
        editTexts.add(editText);
        editText.setText(item.getValue());
        if (isTotalInfoView(item)) {
            totalPatientsView = editText;
        } else {
            editText.addTextChangedListener(new EditTextWatcher(item));
        }
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

    public boolean hasDataChanged() {
        return hasDataChanged;
    }

    public List<BaseInfoItem> getDataList() {
        return dataList;
    }

    public void deHighLightTotal() {
        if (totalPatientsView != null) {
            totalPatientsView.setBackground(getResources().getDrawable(R.color.color_page_gray));
        }
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
        return getString(R.string.table_prophylaxis_total).equals(item.getName());
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
