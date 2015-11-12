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

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.holder.StockCardViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

public class StockCardListAdapter extends RecyclerView.Adapter<StockCardViewHolder> {

    List<StockCard> stockCards;
    private StockCardViewHolder.OnItemViewClickListener listener;

    @Getter
    List<StockCard> currentStockCards;

    public StockCardListAdapter(List<StockCard> stockCards, StockCardViewHolder.OnItemViewClickListener listener) {
        this.stockCards = stockCards;
        this.listener = listener;
        this.currentStockCards = new ArrayList<>(stockCards);
    }

    @Override
    public StockCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stockcard, parent, false);

        return new StockCardViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(StockCardViewHolder holder, final int position) {

        final StockCard stockCard = currentStockCards.get(position);
        holder.populate(stockCard);
    }



    @Override
    public int getItemCount() {
        return currentStockCards.size();
    }

    public void filter(String query) {
        if (StringUtils.isEmpty(query)) {
            this.currentStockCards = new ArrayList<>(stockCards);
            this.notifyDataSetChanged();
        }

        this.currentStockCards = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            if (stockCard.getProduct().getPrimaryName().toLowerCase().contains(query.toLowerCase())
                    || stockCard.getProduct().getCode().toLowerCase().contains(query.toLowerCase())) {
                this.currentStockCards.add(stockCard);
            }
        }

        this.notifyDataSetChanged();
    }

    public void sortBySOH(final boolean asc) {

        Comparator<StockCard> stockCardComparator = new Comparator<StockCard>() {
            @Override
            public int compare(StockCard lhs, StockCard rhs) {
                if (asc) {
                    return (int) (lhs.getStockOnHand() - rhs.getStockOnHand());
                } else {
                    return (int) (rhs.getStockOnHand() - lhs.getStockOnHand());
                }
            }
        };

        Collections.sort(currentStockCards, stockCardComparator);
        Collections.sort(stockCards, stockCardComparator);

        this.notifyDataSetChanged();
    }

    public void sortByName(final boolean asc) {

        Comparator<StockCard> stockCardComparator = new Comparator<StockCard>() {
            @Override
            public int compare(StockCard lhs, StockCard rhs) {
                if (asc) {
                    return lhs.getProduct().getPrimaryName().compareTo(rhs.getProduct().getPrimaryName());
                } else {
                    return rhs.getProduct().getPrimaryName().compareTo(lhs.getProduct().getPrimaryName());
                }
            }
        };

        Collections.sort(currentStockCards, stockCardComparator);
        Collections.sort(stockCards, stockCardComparator);

        this.notifyDataSetChanged();
    }

}
