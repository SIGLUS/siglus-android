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
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class InventoryListAdapter extends RecyclerView.Adapter<InventoryListAdapter.ViewHolder> implements FilterableAdapter {

    LayoutInflater inflater;
    Context context;
    List<InventoryViewModel> inventoryList;
    List<InventoryViewModel> currentList;

    public InventoryListAdapter(Context context, List<Product> productList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        inventoryList = wrapByViewModel(productList);
        currentList = inventoryList;
    }

    private List<InventoryViewModel> wrapByViewModel(List<Product> productList) {
        List<InventoryViewModel> inventoryList = new ArrayList<>();

        for (Product product : productList) {
            inventoryList.add(new InventoryViewModel(product));
        }

        return inventoryList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_inventory, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final InventoryViewModel viewModel = currentList.get(position);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    holder.actionDivider.setVisibility(View.VISIBLE);
                    holder.actionPanel.setVisibility(View.VISIBLE);
                } else {
                    holder.actionDivider.setVisibility(View.GONE);
                    holder.actionPanel.setVisibility(View.GONE);
                    viewModel.reset();
                    holder.reset();
                }
                viewModel.setChecked(isChecked);
            }
        });

        holder.txQuantity.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                viewModel.setQuantity(holder.txQuantity.getText().toString());
                return false;
            }
        });

        holder.txExpireDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(holder, position);
            }
        });

        holder.checkBox.setChecked(viewModel.isChecked());
        holder.productName.setText(viewModel.getProduct().getPrimaryName());
        holder.productUnit.setText(viewModel.getProduct().getType());
        holder.txQuantity.setText(viewModel.getQuantity());
        holder.txExpireDate.setText(viewModel.getExpireDate());

        if (!viewModel.isValid()) {
            holder.lyQuantity.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
        } else {
            holder.lyQuantity.setErrorEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    public void showDatePicker(final ViewHolder holder, final int position) {

        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(context, DatePickerDialog.BUTTON_NEUTRAL, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                if (today.before(date)) {
                    String dateString = new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year).toString();
                    holder.txExpireDate.setText(dateString);
                    currentList.get(position).setExpireDate(dateString);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.msg_invalid_date), Toast.LENGTH_SHORT).show();
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    public List<InventoryViewModel> getInventoryList() {
        return this.inventoryList;
    }

    @Override
    public void filter(String key) {

        if (StringUtils.isEmpty(key)) {
            this.currentList = inventoryList;
            this.notifyDataSetChanged();
            return;
        }

        List<InventoryViewModel> filteredList = new ArrayList<>();

        for (InventoryViewModel viewModel : inventoryList) {
            if (viewModel.getProduct().getPrimaryName().contains(key)
                    ||viewModel.getProduct().getCode().contains(key)) {
                filteredList.add(viewModel);
            }
        }
        this.currentList = filteredList;
        this.notifyDataSetChanged();
    }

    @Override
    public int validateAll() {
        int position = -1;
        for (int i = 0; i < currentList.size(); i++) {
            InventoryViewModel model = currentList.get(i);
            if (model.isChecked() && StringUtils.isEmpty(model.getQuantity())) {
                model.setValid(false);
                if (position == -1) position = i;
            } else {
                model.setValid(true);
            }
        }
        this.notifyDataSetChanged();
        return position;
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
