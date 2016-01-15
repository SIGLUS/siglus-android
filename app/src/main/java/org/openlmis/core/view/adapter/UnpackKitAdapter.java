package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PhysicalInventoryViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

public class UnpackKitAdapter extends InventoryListAdapter<RecyclerView.ViewHolder> implements FilterableAdapter {

    public UnpackKitAdapter(List<StockCardViewModel> data) {
        super(data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_physical_inventory, parent, false);
        return new PhysicalInventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position >= currentList.size()) {
            return;
        }
        PhysicalInventoryViewHolder holder = (PhysicalInventoryViewHolder) viewHolder;
        final StockCardViewModel viewModel = currentList.get(position);

        holder.populate(viewModel, queryKeyWord);
    }
}
