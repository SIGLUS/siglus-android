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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.view.activity.SelectDrugsActivity;

import java.util.ArrayList;
import java.util.List;

//TODO hide custom regime

public class MMIARegimeList extends LinearLayout {
    private Context context;
    private TextView totalView;
    private List<RegimenItem> dataList;
    private List<EditText> editTexts = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private boolean hasDataChanged = false;
    private ArrayList<RegimenItem> adults;
    private ArrayList<RegimenItem> paediatrics;

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

    public void initView(List<RegimenItem> regimenItems, TextView totalView) {
        initCategoryList(regimenItems);
        this.dataList = regimenItems;
        this.totalView = totalView;
        addHeaderView();

        for (int i = 0; i < adults.size(); i++) {
            addItemView(adults.get(i), i);
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_custom_regimen)) {
            addAdultBtnView();
        }

        for (int i = 0; i < paediatrics.size(); i++) {
            addItemView(paediatrics.get(i), adults.size() + i);
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_custom_regimen)) {
            addPaediatricsBtnView();
        }
        editTexts.get(editTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
        totalView.setText(String.valueOf(getTotal()));
    }

    private void addAdultBtnView() {
        final TextView view = (TextView) layoutInflater.inflate(R.layout.item_add_custom_regime, this, false);
        view.setText(R.string.label_add_adult_regime);
        view.setBackgroundResource(R.color.color_green_light);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.getContext().startActivity(SelectDrugsActivity.getIntentToMe(view.getContext(), Regimen.RegimeType.Adults));
            }
        });
        addView(view);
    }

    private void addPaediatricsBtnView() {
        final TextView view = (TextView) layoutInflater.inflate(R.layout.item_add_custom_regime, this, false);
        view.setText(R.string.label_add_child_regime);
        view.setBackgroundResource(R.color.color_regime_baby);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.getContext().startActivity(SelectDrugsActivity.getIntentToMe(view.getContext(), Regimen.RegimeType.Paediatrics));
            }
        });
        addView(view);
    }

    private void initCategoryList(List<RegimenItem> regimenItems) {
        adults = new ArrayList<>();
        paediatrics = new ArrayList<>();

        for (RegimenItem item : regimenItems) {
            if (item == null) {
                continue;
            }
            if (Regimen.RegimeType.Paediatrics.equals(item.getRegimen().getType())) {
                paediatrics.add(item);
            } else {
                adults.add(item);
            }
        }
    }

    public List<RegimenItem> getDataList() {
        return dataList;
    }

    private void addHeaderView() {
        addItemView(null, true, 0);
    }

    private void addItemView(final RegimenItem item, int position) {
        addItemView(item, false, position);
    }

    private void addItemView(final RegimenItem item, boolean isHeaderView, final int position) {
        View view = layoutInflater.inflate(R.layout.item_regime, this, false);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        EditText etTotal = (EditText) view.findViewById(R.id.et_total);

        if (isHeaderView) {
            tvName.setGravity(Gravity.CENTER);
            etTotal.setEnabled(false);
            view.setBackgroundResource(R.color.color_mmia_speed_list_header);

            tvName.setText(R.string.label_regime_header_name);
            etTotal.setText(getResources().getString(R.string.label_total_mmia).toUpperCase());
        } else {
            editTexts.add(etTotal);
            Regimen regimen = item.getRegimen();
            tvName.setText(regimen.getName());

            if (item.getAmount() != null) {
                etTotal.setText(String.valueOf(item.getAmount()));
            }

            if (Regimen.RegimeType.Paediatrics.equals(regimen.getType())) {
                view.setBackgroundResource(R.color.color_regime_baby);
            } else {
                view.setBackgroundResource(R.color.color_green_light);
            }

            etTotal.addTextChangedListener(new EditTextWatcher(item));

            etTotal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        if ((position + 1) < editTexts.size()) {
                            editTexts.get(position + 1).requestFocus();
                            return true;
                        }
                    }
                    return false;
                }
            });


        }
        addView(view);
    }

    public boolean hasDataChanged() {
        return hasDataChanged;
    }

    public void highLightTotal() {
        totalView.setBackground(getResources().getDrawable(R.drawable.border_bg_red));
    }

    public void deHighLightTotal() {
        totalView.setBackground(getResources().getDrawable(R.color.color_page_gray));
    }

    public boolean hasEmptyField() {
        for (RegimenItem item : dataList) {
            if (null == item.getAmount()) {
                return true;
            }
        }
        return false;
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
        return RnRForm.calculateTotalRegimenAmount(dataList);
    }

}
