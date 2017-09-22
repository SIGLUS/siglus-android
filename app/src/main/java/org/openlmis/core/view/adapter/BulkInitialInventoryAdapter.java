package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryHeaderViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

public class BulkInitialInventoryAdapter extends InventoryListAdapter<BaseViewHolder> {

    private static int HEADER_POSITION = 0;
    private static int ITEM_HEADER = 0;
    private static int ITEM_LIST = 1;

    public BulkInitialInventoryAdapter(List<InventoryViewModel> data) {
        super(data);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == HEADER_POSITION) {
            return ITEM_HEADER;
        } else {
            return ITEM_LIST;
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_LIST) {
            return new BulkInitialInventoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory, parent, false));
        } else {
            return new BulkInitialInventoryHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_header, parent, false));
        }
    }

    @Override
    public int validateAll() {
        for(int i = 0; i < data.size(); i++){
            if(!data.get(i).isChecked() && !data.get(i).isDummyModel()){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof BulkInitialInventoryViewHolder) {
            final InventoryViewModel viewModel = filteredList.get(position);
            ((BulkInitialInventoryViewHolder) holder).populate(viewModel, queryKeyWord);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}
