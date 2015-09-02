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
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

import java.util.List;


public class PhysicalInventoryAdapater extends RecyclerView.Adapter<PhysicalInventoryAdapater.ViewHolder> implements FilterableAdapter {

    LayoutInflater inflater;
    Context context;
    List<StockCardViewModel> data;

    public PhysicalInventoryAdapater(Context context, List<StockCardViewModel> stockCardViewModelList){
        this.context = context;
        this.data = stockCardViewModelList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.item_physical_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StockCardViewModel viewModel = data.get(position);
        holder.tvProductName.setText(viewModel.getStyledName());
        holder.tvProductUnit.setText(viewModel.getStyledUnit());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void filter(String keyword) {

    }

    @Override
    public int validateAll() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductUnit;
        EditText etQuantity;
        TextView tvAddExpiryDate;

        public ViewHolder(View itemView) {
            super(itemView);

            tvProductName = (TextView)itemView.findViewById(R.id.product_name);
            tvProductUnit = (TextView)itemView.findViewById(R.id.product_unit);
            etQuantity = (EditText)itemView.findViewById(R.id.tx_quantity);
            tvAddExpiryDate = (TextView)itemView.findViewById(R.id.tx_expire_date);

            etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        }
    }
}
