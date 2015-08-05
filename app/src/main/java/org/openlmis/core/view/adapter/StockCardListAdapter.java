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


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;

import java.util.ArrayList;
import java.util.List;

public class StockCardListAdapter extends RecyclerView.Adapter<StockCardListAdapter.ViewHolder>{

    List<StockCard> stockCards;
    List<StockCard> currentStockCards;


    public StockCardListAdapter(List<StockCard> stockCardList){
        this.stockCards = stockCardList;
        currentStockCards = stockCardList;
    }

    @Override
    public StockCardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stockcard, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StockCardListAdapter.ViewHolder holder, int position) {
        holder.productName.setText(currentStockCards.get(position).getProduct().getName());
        holder.productUnit.setText(currentStockCards.get(position).getProduct().getUnit());
        holder.stockOnHand.setText(currentStockCards.get(position).getStockOnHand() + "");
    }

    @Override
    public int getItemCount() {
        return currentStockCards.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView productName;
        public TextView productUnit;
        public TextView stockOnHand;

        public ViewHolder(View itemView) {
            super(itemView);

            productName = (TextView)itemView.findViewById(R.id.product_name);
            productUnit = (TextView)itemView.findViewById(R.id.product_unit);
            stockOnHand = (TextView)itemView.findViewById(R.id.stockOnHand);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public void filterByName(String query){
        if (StringUtils.isEmpty(query)){
            this.currentStockCards = stockCards;
            this.notifyDataSetChanged();
        }

        this.currentStockCards = new ArrayList<>();
        for (StockCard stockCard : stockCards){
            if (stockCard.getProduct().getName().contains(query)){
                this.currentStockCards.add(stockCard);
            }
        }

        this.notifyDataSetChanged();
    }
}
