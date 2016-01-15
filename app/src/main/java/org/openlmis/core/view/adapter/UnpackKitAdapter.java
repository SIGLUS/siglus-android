package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.UnpackKitViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

public class UnpackKitAdapter extends InventoryListAdapter<UnpackKitViewHolder> implements FilterableAdapter {

    public UnpackKitAdapter(List<StockCardViewModel> data) {
        super(data);
    }

    @Override
    public UnpackKitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_physical_inventory, parent, false);
        return new UnpackKitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UnpackKitViewHolder viewHolder, int position) {
        final StockCardViewModel viewModel = currentList.get(position);
        viewHolder.populate(viewModel);
    }
}
