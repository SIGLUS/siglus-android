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

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.List;

import lombok.Getter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


public class PhysicalInventoryAdapter extends RecyclerView.Adapter<PhysicalInventoryAdapter.ViewHolder> implements FilterableAdapter {

    LayoutInflater inflater;
    Context context;

    @Getter
    List<StockCardViewModel> data;
    List<StockCardViewModel> currentList;

    public PhysicalInventoryAdapter(Context context, List<StockCardViewModel> stockCardViewModelList){
        this.context = context;
        this.data = stockCardViewModelList;
        currentList =  newArrayList(data);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.item_physical_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        StockCardViewModel viewModel = currentList.get(position);
        holder.tvProductName.setText(viewModel.getStyledName());
        holder.tvProductUnit.setText(viewModel.getStyledUnit());

        holder.etQuantity.setText(viewModel.getQuantity());

        holder.etQuantity.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                currentList.get(position).setQuantity(((TextView)v).getText().toString());
                return false;
            }
        });

        if (!viewModel.isValidate()) {
            holder.lyQuantity.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
        } else {
            holder.lyQuantity.setErrorEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    @Override
    public void filter(final String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            this.currentList = data;
            this.notifyDataSetChanged();
            return;
        }

        this.currentList = from(data).filter(new Predicate<StockCardViewModel>() {
            @Override
            public boolean apply(StockCardViewModel stockCardViewModel) {
                return stockCardViewModel.getProductName().contains(keyword)
                        || stockCardViewModel.getFnm().contains(keyword);
            }
        }).toList();

        this.notifyDataSetChanged();
    }

    public void refreshList(List<StockCardViewModel> data){
        this.data = data;
        this.currentList.clear();
        this.currentList.addAll(data);
        notifyDataSetChanged();
    }


    @Override
    public int validateAll() {
        int position = -1;
        for (int i=0;i<data.size();i++){
            if (!data.get(i).validate()){
                position = i;
                break;
            }
        }

        this.notifyDataSetChanged();
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductUnit;
        EditText etQuantity;
        TextView tvAddExpiryDate;
        TextInputLayout lyQuantity;

        public ViewHolder(View itemView) {
            super(itemView);

            tvProductName = (TextView)itemView.findViewById(R.id.product_name);
            tvProductUnit = (TextView)itemView.findViewById(R.id.product_unit);
            etQuantity = (EditText)itemView.findViewById(R.id.tx_quantity);
            lyQuantity = (TextInputLayout)itemView.findViewById(R.id.ly_quantity);
            tvAddExpiryDate = (TextView)itemView.findViewById(R.id.tx_expire_date);

            etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        }
    }
}
