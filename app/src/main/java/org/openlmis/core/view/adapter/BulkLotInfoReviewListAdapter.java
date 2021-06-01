package org.openlmis.core.view.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.BulkLotInfoReviewViewHolder;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.ArrayList;
import java.util.List;

public class BulkLotInfoReviewListAdapter extends RecyclerView.Adapter<BulkLotInfoReviewViewHolder> {
    List<LotMovementViewModel> viewModels = new ArrayList<>();

    public BulkLotInfoReviewListAdapter(BaseStockMovementViewModel stockMovementViewModel) {
        viewModels.addAll(stockMovementViewModel.getExistingLotMovementViewModelList());
        viewModels.addAll(stockMovementViewModel.getNewLotMovementViewModelList());
    }

    @Override
    public BulkLotInfoReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BulkLotInfoReviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lot_info_review, parent, false));
    }

    @Override
    public void onBindViewHolder(BulkLotInfoReviewViewHolder holder, int position) {
        holder.populate(viewModels.get(position));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
