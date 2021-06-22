package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.BulkEntriesLotMovementViewHolder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class BulkEntriesLotMovementAdapter extends RecyclerView.Adapter<BulkEntriesLotMovementViewHolder> {

  @Getter
  protected final List<LotMovementViewModel> lotList;


  public BulkEntriesLotMovementAdapter(
      List<LotMovementViewModel> lotList) {
    this.lotList = lotList;
  }

  @NonNull
  @Override
  public BulkEntriesLotMovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    return new BulkEntriesLotMovementViewHolder(LayoutInflater.from(parent.getContext()).inflate(
        R.layout.item_bulk_entries_lots_info,parent,false));
  }

  @Override
  public void onBindViewHolder(@NonNull BulkEntriesLotMovementViewHolder holder, int position) {
    holder.populate(lotList.get(position));
  }

  @Override
  public int getItemCount() {
    return lotList.size();
  }
}
