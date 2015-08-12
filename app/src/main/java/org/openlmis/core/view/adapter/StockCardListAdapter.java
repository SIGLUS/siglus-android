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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

public class StockCardListAdapter extends RecyclerView.Adapter<StockCardListAdapter.ViewHolder> implements FilterableAdapter {

    List<StockCard> stockCards;

    @Getter
    List<StockCard> currentStockCards;


    public StockCardListAdapter(List<StockCard> stockCardList){
        this.stockCards = stockCardList;
        currentStockCards = new ArrayList<>(stockCardList);
    }

    @Override
    public StockCardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stockcard, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StockCardListAdapter.ViewHolder holder, int position) {

        Product product = currentStockCards.get(position).getProduct();
        String productName = product.getPrimaryName() + " [" + product.getCode() + "]";
        SpannableStringBuilder styledName=new SpannableStringBuilder(productName);
        styledName.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.secondary_text)),
                product.getPrimaryName().length(), productName.length(), Spannable.SPAN_POINT_MARK);

        String unit = product.getStrength() + " " + product.getType();
        SpannableStringBuilder styledUnit=new SpannableStringBuilder(unit);
        styledUnit.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.secondary_text)),
                product.getStrength().length(), unit.length(), Spannable.SPAN_POINT_MARK);

        holder.productName.setText(styledName);
        holder.productUnit.setText(styledUnit);
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

    @Override
    public void filter(String query){
        if (StringUtils.isEmpty(query)){
            this.currentStockCards = new ArrayList<>(stockCards);
            this.notifyDataSetChanged();
        }

        this.currentStockCards = new ArrayList<>();
        for (StockCard stockCard : stockCards){
            if (stockCard.getProduct().getPrimaryName().contains(query)
                    || stockCard.getProduct().getCode().contains(query)){
                this.currentStockCards.add(stockCard);
            }
        }

        this.notifyDataSetChanged();
    }

    public void sortBySOH(final boolean asc){
        Collections.sort(currentStockCards, new Comparator<StockCard>() {
            @Override
            public int compare(StockCard lhs, StockCard rhs) {
                if (asc){
                    return lhs.getStockOnHand() - rhs.getStockOnHand();
                }else {
                    return rhs.getStockOnHand() - lhs.getStockOnHand();
                }
            }
        });

        this.notifyDataSetChanged();
    }

    public void sortByName(final boolean asc){
        Collections.sort(currentStockCards, new Comparator<StockCard>() {
            @Override
            public int compare(StockCard lhs, StockCard rhs) {
                if (asc){
                    return lhs.getProduct().getPrimaryName().compareTo(rhs.getProduct().getPrimaryName());
                }else {
                    return rhs.getProduct().getPrimaryName().compareTo(lhs.getProduct().getPrimaryName());
                }
            }
        });

        this.notifyDataSetChanged();
    }

}
