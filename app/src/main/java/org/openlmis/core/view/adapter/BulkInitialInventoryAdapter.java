package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.BulkInitialInventoryViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

public class BulkInitialInventoryAdapter extends InventoryListAdapter<BulkInitialInventoryViewHolder> {

    public BulkInitialInventoryAdapter(List<InventoryViewModel> data) {
        super(data);
    }

    @Override
    public void onBindViewHolder(final BulkInitialInventoryViewHolder holder, final int position) {
        final InventoryViewModel viewModel = filteredList.get(position);
        holder.populate(viewModel, queryKeyWord);
    }


    @Override
    public BulkInitialInventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BulkInitialInventoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory, parent, false));
    }
}
