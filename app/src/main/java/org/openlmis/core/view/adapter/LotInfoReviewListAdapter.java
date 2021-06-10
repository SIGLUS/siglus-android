package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.LotInfoReviewViewHolder;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class LotInfoReviewListAdapter extends RecyclerView.Adapter<LotInfoReviewViewHolder> {

  List<LotMovementViewModel> viewModels = new ArrayList<>();

  public LotInfoReviewListAdapter(BaseStockMovementViewModel stockMovementViewModel) {
    viewModels.addAll(stockMovementViewModel.getExistingLotMovementViewModelList());
    viewModels.addAll(stockMovementViewModel.getNewLotMovementViewModelList());
  }

  @Override
  public LotInfoReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new LotInfoReviewViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_lot_info_review, parent, false));
  }

  @Override
  public void onBindViewHolder(LotInfoReviewViewHolder holder, int position) {
    holder.populate(viewModels.get(position));
  }

  @Override
  public int getItemCount() {
    return viewModels.size();
  }
}
