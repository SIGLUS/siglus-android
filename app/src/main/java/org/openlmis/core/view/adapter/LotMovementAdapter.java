package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.LotMovementViewHolder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.List;

import lombok.Getter;

public class LotMovementAdapter extends RecyclerView.Adapter<LotMovementViewHolder> {

    @Getter
    protected final List<LotMovementViewModel> lotList;

    @Getter
    private final String productName;
    private MovementChangedListener movementChangedListener;

    public LotMovementAdapter(List<LotMovementViewModel> data) {
        this.lotList = data;
        productName = null;
    }

    public LotMovementAdapter(List<LotMovementViewModel> data, String productName) {
        this.lotList = data;
        this.productName = productName;
    }

    @Override
    public LotMovementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lots_info, parent, false);
        return new LotMovementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LotMovementViewHolder holder, int position) {
        final LotMovementViewModel viewModel = lotList.get(position);
        holder.setMovementChangeListener(movementChangedListener);
        holder.populate(viewModel, this);
    }

    @Override
    public int getItemCount() {
        return lotList.size();
    }

    public void remove(LotMovementViewModel viewModel) {
        lotList.remove(viewModel);
        if (movementChangedListener != null) {
            movementChangedListener.movementChange();
        }
        this.notifyDataSetChanged();
    }


    public void setMovementChangeListener(MovementChangedListener movementChangedListener) {
        this.movementChangedListener = movementChangedListener;
    }

    public interface MovementChangedListener {
        void movementChange();
    }
}
