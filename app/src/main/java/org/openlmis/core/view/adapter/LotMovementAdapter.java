package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.LotMovementViewHolder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.List;

public class LotMovementAdapter extends RecyclerView.Adapter<LotMovementViewHolder> {


    private final List<LotMovementViewModel> lotList;

    public LotMovementAdapter(List<LotMovementViewModel> data) {
        this.lotList = data;
    }

    @Override
    public LotMovementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lots_info, parent, false);
        return new LotMovementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LotMovementViewHolder holder, int position) {
        final LotMovementViewModel viewModel = lotList.get(position);
        holder.populate(viewModel);
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
