package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryHeaderViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

import lombok.Setter;

public class BulkInitialInventoryAdapter extends InventoryListAdapter<BaseViewHolder> {

    public static int ITEM_BASIC_HEADER = 0;
    public static int ITEM_LIST = 1;
    public static int ITEM_NON_BASIC_HEADER = 2;
    @Setter
    private View.OnClickListener removeNonBasicProductListener;

    public BulkInitialInventoryAdapter(List<InventoryViewModel> data) {
        super(data);
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getViewType();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_LIST) {
            return new BulkInitialInventoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory, parent, false));
        } else if (viewType == ITEM_BASIC_HEADER) {
            return new BulkInitialInventoryHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_header, parent, false), R.string.title_basic_products);
        } else {
            return new BulkInitialInventoryHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_header, parent, false), R.string.title_non_basic_products);
        }
    }

    @Override
    public int validateAll() {
        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).isChecked() && !data.get(i).isDummyModel()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {
        if (holder instanceof BulkInitialInventoryViewHolder) {
            final InventoryViewModel viewModel = data.get(position);
            ((BulkInitialInventoryViewHolder) holder).populate(viewModel, queryKeyWord);
            ((BulkInitialInventoryViewHolder) holder).btnRemoveProduct.setTag(viewModel);
            ((BulkInitialInventoryViewHolder) holder).btnRemoveProduct.setOnClickListener(removeNonBasicProductListener);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
