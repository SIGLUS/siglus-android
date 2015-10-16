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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class PhysicalInventoryAdapter extends InventoryListAdapter<RecyclerView.ViewHolder> implements FilterableAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private View footView;

    public PhysicalInventoryAdapter(Context context, List<StockCardViewModel> data) {
        super(context, data);
    }

    public PhysicalInventoryAdapter(Context context, List<StockCardViewModel> data, View footView) {
        this(context, data);
        this.footView = footView;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = inflater.inflate(R.layout.item_physical_inventory, parent, false);
            return new ViewHolder(view);
        } else {
            return new VHFooter(footView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position >= currentList.size()) {
            return;
        }
        final ViewHolder holder = (ViewHolder) viewHolder;
        final StockCardViewModel viewModel = currentList.get(position);
        EditTextWatcher textWatcher = new EditTextWatcher(viewModel);
        holder.etQuantity.removeTextChangedListener(textWatcher);

        holder.tvProductName.setText(viewModel.getStyledName());
        holder.tvProductUnit.setText(viewModel.getStyledUnit());

        holder.etQuantity.setHint(R.string.hint_quantity_in_stock);
        holder.etQuantity.setText(viewModel.getQuantity());
        holder.etQuantity.addTextChangedListener(textWatcher);

        initDate(holder.expireDateContainer, viewModel);

        holder.tvAddExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(holder.expireDateContainer, viewModel);
            }
        });

        if (!viewModel.isValidate()) {
            holder.lyQuantity.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
        } else {
            holder.lyQuantity.setErrorEnabled(false);
        }
    }

    private void showDatePicker(final ViewGroup expireDateContainer, final StockCardViewModel viewModel) {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(context, DatePickerDialog.BUTTON_NEUTRAL, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                if (today.before(date)) {
                    String dateString = new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year).toString();
                    addDate(dateString, expireDateContainer, viewModel);
                } else {
                    ToastUtil.show(R.string.msg_invalid_date);
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        dialog.show();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductUnit;
        EditText etQuantity;
        TextView tvAddExpiryDate;
        TextInputLayout lyQuantity;
        ViewGroup expireDateContainer;

        public ViewHolder(View itemView) {
            super(itemView);

            tvProductName = (TextView) itemView.findViewById(R.id.product_name);
            tvProductUnit = (TextView) itemView.findViewById(R.id.product_unit);
            etQuantity = (EditText) itemView.findViewById(R.id.tx_quantity);
            lyQuantity = (TextInputLayout) itemView.findViewById(R.id.ly_quantity);
            tvAddExpiryDate = (TextView) itemView.findViewById(R.id.tx_expire_date);
            expireDateContainer = (ViewGroup) itemView.findViewById(R.id.vg_expire_date_container);

            etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        }
    }

    public static class VHFooter extends RecyclerView.ViewHolder {
        public VHFooter(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return itemCount == 0 ? itemCount : itemCount + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }

    }

    private boolean isPositionFooter(int position) {
        return position == currentList.size();
    }

    private void initDate(ViewGroup expireDateContainer, StockCardViewModel viewModel) {
        View addView = expireDateContainer.getChildAt(expireDateContainer.getChildCount() - 1);
        expireDateContainer.removeAllViews();
        expireDateContainer.addView(addView);
        for (String date : viewModel.getExpiryDates()) {
            initExpireDateView(date, viewModel, expireDateContainer);
        }
    }

    private void addDate(String date, ViewGroup expireDateContainer, StockCardViewModel viewModel) {
        if (viewModel.addExpiryDate(date)) {
            initExpireDateView(date, viewModel, expireDateContainer);
        }
    }

    private void initExpireDateView(String date, final StockCardViewModel viewModel, final ViewGroup expireDateContainer) {
        try {
            final String expireDate = DateUtil.convertDate(date, DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
            final View expireDateView = addExpireDateView(expireDate, expireDateContainer);
            View ivClear = expireDateView.findViewById(R.id.iv_clear);
            ivClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expireDateContainer.removeView(expireDateView);
                    viewModel.removeExpiryDate(expireDate);
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private ViewGroup addExpireDateView(final String expireDate, final ViewGroup expireDateContainer) {
        ViewGroup expireDateView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.item_expire_date, null);
        TextView tvExpireDate = (TextView) expireDateView.findViewById(R.id.tx_expire_data);
        tvExpireDate.setText(expireDate);
        expireDateContainer.addView(expireDateView, expireDateContainer.getChildCount() - 1);
        return expireDateView;
    }

}
