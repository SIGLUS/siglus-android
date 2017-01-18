package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.view.holder.StockHistoryMovementItemViewHolder;

import java.util.List;

public class MovementItemListAdapter extends RecyclerView.Adapter<StockHistoryMovementItemViewHolder> {
    private List<StockMovementItem> movementItemList;

    public MovementItemListAdapter(List<StockMovementItem> movementItemList) {
        this.movementItemList = movementItemList;
    }

    @Override
    public StockHistoryMovementItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StockHistoryMovementItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movement_history, parent, false));
    }

    @Override
    public void onBindViewHolder(StockHistoryMovementItemViewHolder holder, int position) {
        holder.populate(movementItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return movementItemList.size();
    }
}
