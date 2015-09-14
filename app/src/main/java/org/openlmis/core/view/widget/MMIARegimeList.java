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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;

import java.util.ArrayList;

public class MMIARegimeList extends LinearLayout {
    private Context context;
    private TextView totalView;
    private ArrayList<RegimenItem> dataList;
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private boolean hasDataChanged = false;

    public MMIARegimeList(Context context) {
        super(context);
        init(context);
    }

    public MMIARegimeList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = LayoutInflater.from(context);
    }

    public void initView(ArrayList<RegimenItem> regimenItems, TextView totalView) {
        this.dataList = regimenItems;
        this.totalView = totalView;
        addHeaderView();
        for (RegimenItem item : dataList) {
            if (item != null) {
                addItemView(item);
            }
        }
        totalView.setText(String.valueOf(getTotal()));
    }

    public ArrayList<RegimenItem> getDataList() {
        return dataList;
    }

    private void addHeaderView() {
        addItemView(null, true);
    }

    private void addItemView(final RegimenItem item) {
        addItemView(item, false);
    }

    private void addItemView(final RegimenItem item, boolean isHeaderView) {
        View view = layoutInflater.inflate(R.layout.item_regime, this, false);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        EditText etTotal = (EditText) view.findViewById(R.id.et_total);

        if (isHeaderView) {
            tvName.setGravity(Gravity.CENTER);
            etTotal.setEnabled(false);
            view.setBackgroundResource(R.color.color_mmia_speed_list_header);

            tvName.setText(R.string.label_regime_header_name);
            etTotal.setText(getResources().getString(R.string.label_total).toUpperCase());
        } else {
            editTexts.add(etTotal);
            Regimen regimen = item.getRegimen();
            tvName.setText(regimen.getName());

            if (item.getAmount() != null) {
                etTotal.setText(String.valueOf(item.getAmount()));
            }

            if (Regimen.RegimeType.BABY.equals(regimen.getType())) {
                view.setBackgroundResource(R.color.color_regime_baby);
            } else {
                view.setBackgroundResource(R.color.color_regime_adult);
            }
            etTotal.addTextChangedListener(new EditTextWatcher(item));
        }
        addView(view);
    }

    public boolean hasDataChanged() {
        return hasDataChanged;
    }


    class EditTextWatcher implements android.text.TextWatcher {

        private final RegimenItem item;

        public EditTextWatcher(RegimenItem item) {
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

            try {
                item.setAmount(Long.parseLong(editable.toString()));
            } catch (NumberFormatException e) {
                item.setAmount(null);
            }
            totalView.setText(String.valueOf(getTotal()));
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

    public long getTotal() {
        return RnRForm.getRegimenItemListAmount(dataList);
    }

}
