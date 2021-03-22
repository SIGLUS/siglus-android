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
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class RapidTestRnrForm extends LinearLayout {
    private Context context;
    private ViewGroup viewGroup;
    private List<Pair<EditText, SimpleTextWatcher>> editTextConfigures = new ArrayList<>();
    public List<ProgramDataFormBasicItem> itemFormList;
    private LayoutInflater layoutInflater;

    @Getter
    private ViewGroup leftHeaderScrollView;
    private ViewGroup leftHeaderLinearlayout;
    @Getter
    private ViewGroup topRightScrollView;

    public RapidTestRnrForm(Context context) {
        super(context);
        init(context);
    }

    public RapidTestRnrForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void init(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        View container = layoutInflater.inflate(R.layout.view_rapid_test_rnr_form, this, true);
        viewGroup = (ViewGroup) container.findViewById(R.id.program_from_list);
        leftHeaderLinearlayout = (ViewGroup) container.findViewById(R.id.rapid_test_top_left_layout);
        leftHeaderScrollView = (ViewGroup) container.findViewById(R.id.rapid_test_top_left_scrollview);
        topRightScrollView = (ViewGroup) container.findViewById(R.id.rapid_test_top_scrollview);
    }

    public void initView(List<ProgramDataFormBasicItem> itemFormList) {
        this.itemFormList = itemFormList;
        addHeaderView();
        addItemView(itemFormList);
    }

    private void addHeaderView() {
        addView(null, true);
    }

    private void addItemView(List<ProgramDataFormBasicItem> itemFormList) {
        this.itemFormList = itemFormList;
        for (ProgramDataFormBasicItem basicItem : itemFormList) {
            addView(basicItem, false);
        }
    }

    private ViewGroup inflateView() {
        return (ViewGroup) layoutInflater.inflate(R.layout.item_rapid_test_from, this, false);
    }

    private ViewGroup getTopLeftInflateView() {
        return (ViewGroup) layoutInflater.inflate(R.layout.item_rapid_test_top_left, this, false);
    }

    private ViewGroup addView(ProgramDataFormBasicItem item, boolean isHeaderView) {
        ViewGroup inflate = inflateView();
        ViewGroup topLeftInflate = getTopLeftInflateView();
        TextView tvName = (TextView) inflate.findViewById(R.id.tv_name);
        EditText etStock = (EditText) inflate.findViewById(R.id.et_stock);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_received);
        TextView tvIssue = (TextView) inflate.findViewById(R.id.tv_issue);
        TextView tvAdjustment = (TextView) inflate.findViewById(R.id.tv_adjustment);
        TextView tvValidate = (TextView) inflate.findViewById(R.id.tv_expire);
        EditText etInventory = (EditText) inflate.findViewById(R.id.et_inventory);
        TextView leftHeaderText = topLeftInflate.findViewById(R.id.left_tv_code);

        if (isHeaderView) {
            setHeaderView(inflate, tvName, etStock, tvReceived, tvIssue, tvAdjustment, tvValidate, etInventory, leftHeaderText);

        } else {
            leftHeaderText.setText(item.getProduct().getCode());
            tvName.setText(item.getProduct().getPrimaryName());
            tvReceived.setText(String.valueOf(item.getReceived()));
            tvIssue.setText(String.valueOf(item.getIssued()));
            tvAdjustment.setText(String.valueOf(item.getAdjustment()));
            if (item.getIsCustomAmount()) {
                configEditText(item, etStock, getValue(item.getInitialAmount()));
            } else {
                etStock.setText(getValue(item.getInitialAmount()));
                etStock.setEnabled(false);
            }
            configEditText(item, etInventory, getValue(item.getInventory()));

            try {
                if (!(TextUtils.isEmpty(item.getValidate()))) {
                    tvValidate.setText(DateUtil.convertDate(item.getValidate(), "dd/MM/yyyy", "MMM yyyy"));
                }
            } catch (ParseException e) {
                new LMISException(e,"RapidTestRnrForm.addView").reportToFabric();
            }
        }
        leftHeaderLinearlayout.addView(topLeftInflate);
        viewGroup.addView(inflate);
        return inflate;
    }

    public boolean isCompleted() {
        for (Pair<EditText, SimpleTextWatcher> editTextConfigure : editTextConfigures) {
            EditText editText = editTextConfigure.first;
            if (TextUtils.isEmpty(editText.getText().toString())) {
                editText.setError(context.getString(R.string.hint_error_input));
                editText.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void configEditText(ProgramDataFormBasicItem item, EditText editText, String value) {
        editText.setText(value);
        editText.setEnabled(item.getForm().getStatus() == null
                || item.getForm().getStatus() == ProgramDataForm.STATUS.DRAFT);
        RapidTestRnrForm.EditTextWatcher textWatcher = new RapidTestRnrForm.EditTextWatcher(item, editText);
        editText.addTextChangedListener(textWatcher);
        editTextConfigures.add(new Pair<>(editText, textWatcher));
    }

    private String getValue(Long vaule) {
        return vaule == null ? "" : String.valueOf(vaule.longValue());

    }

    private void setHeaderView(ViewGroup inflate,
                               TextView tvName,
                               EditText etStock,
                               TextView tvReceived,
                               TextView tvIssue,
                               TextView tvAdjustment,
                               TextView tvValidate,
                               EditText etInventory,
                               TextView leftHeaderText) {
        leftHeaderText.setText(R.string.label_product_codes);
        tvName.setText(R.string.label_product_name);
        etStock.setText(R.string.initial_stock);
        tvReceived.setText(R.string.entries);
        tvIssue.setText(R.string.ISSUE);
        tvAdjustment.setText(R.string.loss_and_adjustment);
        tvValidate.setText(R.string.label_validate);
        etInventory.setText(R.string.label_inventory);

        etStock.setEnabled(false);
        etInventory.setEnabled(false);

    }

    public void removeListenerOnDestroyView() {
        for (Pair<EditText, SimpleTextWatcher> editTextConfigure : editTextConfigures) {
            if (editTextConfigure.first != null) {
                editTextConfigure.first.clearFocus();
                if (editTextConfigure.second != null) {
                    editTextConfigure.first.removeTextChangedListener(editTextConfigure.second);
                }
            }
        }
    }

    class EditTextWatcher extends SimpleTextWatcher {
        private final ProgramDataFormBasicItem item;
        private final EditText editText;

        public EditTextWatcher(ProgramDataFormBasicItem item, EditText editText) {
            this.item = item;
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(Editable etText) {
            switch (editText.getId()) {
                case R.id.et_inventory:
                    item.setInventory(getEditValue(etText));
                    break;
                case R.id.et_stock:
                    item.setInitialAmount(getEditValue(etText));
                    break;
            }
        }

        private Long getEditValue(Editable etText) {
            Long editText;
            try {
                editText = Long.valueOf(etText.toString());
            } catch (NumberFormatException e) {
                editText = null;
            }
            return editText;
        }
    }

}
