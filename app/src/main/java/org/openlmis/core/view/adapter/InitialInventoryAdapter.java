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

package org.openlmis.core.view.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class InitialInventoryAdapter extends InventoryListAdapter<InitialInventoryAdapter.ViewHolder> {

    public InitialInventoryAdapter(Context context, List<Product> productList) {
        super(context, wrapByViewModel(productList));
    }

    public static List<StockCardViewModel> wrapByViewModel(List<Product> productList) {
        List<StockCardViewModel> inventoryList = new ArrayList<>();

        for (Product product : productList) {
            inventoryList.add(new StockCardViewModel(product));
        }

        return inventoryList;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final StockCardViewModel viewModel = currentList.get(position);
        final EditTextWatcher textWatcher = new EditTextWatcher(viewModel);
        holder.txQuantity.removeTextChangedListener(textWatcher);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    holder.actionDivider.setVisibility(View.VISIBLE);
                    holder.actionPanel.setVisibility(View.VISIBLE);
                } else {
                    holder.actionDivider.setVisibility(View.GONE);
                    holder.actionPanel.setVisibility(View.GONE);
                    viewModel.setQuantity(StringUtils.EMPTY);
                    viewModel.setExpiryDates(null);
                    holder.reset();
                }
                viewModel.setChecked(isChecked);
            }
        });


        holder.txExpireDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(holder, position);
            }
        });

        holder.checkBox.setChecked(viewModel.isChecked());
        holder.productName.setText(viewModel.getStyledName());
        holder.productUnit.setText(viewModel.getType());
        holder.txExpireDate.setText(viewModel.optFirstExpiryDate());
        holder.txQuantity.setText(viewModel.getQuantity());
        holder.txQuantity.setHint(R.string.hint_quantity_in_stock);
        holder.txQuantity.addTextChangedListener(textWatcher);

        if (!viewModel.isValidate()) {
            holder.lyQuantity.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
        } else {
            holder.lyQuantity.setErrorEnabled(false);
        }
    }

    class EditTextWatcher extends SimpleTextWatcher {

        private final StockCardViewModel viewModel;

        public EditTextWatcher(StockCardViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return true;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            viewModel.setQuantity(editable.toString());
        }
    }


    public void showDatePicker(final ViewHolder holder, final int position) {

        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(context, DatePickerDialog.BUTTON_NEUTRAL, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                if (today.before(date)) {
                    String dateString = new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year).toString();
                    try {
                        holder.txExpireDate.setText(DateUtil.convertDate(dateString, "dd/MM/yyyy", "MMM yyyy"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    currentList.get(position).addExpiryDate(dateString, false);
                } else {
                    ToastUtil.show(R.string.msg_invalid_date);
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_inventory, parent, false));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView productName;
        public TextView productUnit;
        public TextInputLayout lyQuantity;
        public EditText txQuantity;
        public TextView txExpireDate;
        public View actionDivider;
        public CheckBox checkBox;
        public View actionPanel;

        public ViewHolder(View itemView) {
            super(itemView);

            productName = (TextView) itemView.findViewById(R.id.product_name);
            productUnit = (TextView) itemView.findViewById(R.id.product_unit);
            lyQuantity = (TextInputLayout) itemView.findViewById(R.id.ly_quantity);
            txQuantity = (EditText) itemView.findViewById(R.id.tx_quantity);
            txExpireDate = (TextView) itemView.findViewById(R.id.tx_expire_date);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            actionDivider = itemView.findViewById(R.id.action_divider);
            actionPanel = itemView.findViewById(R.id.action_panel);

            txQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                    } else {
                        checkBox.setChecked(true);
                    }
                }
            });
        }

        public void reset() {
            txQuantity.setText(StringUtils.EMPTY);
            txExpireDate.setText(StringUtils.EMPTY);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

}
