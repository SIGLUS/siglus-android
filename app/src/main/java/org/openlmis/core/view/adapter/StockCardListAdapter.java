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
