package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;


import org.openlmis.core.R;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryWithLotViewHolder;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;


public class BulkInitialInventoryAdapter extends InventoryListAdapter<BaseViewHolder> {
    private static final String TAG = BulkInitialInventoryAdapter.class.getSimpleName();
    private final RemoveNonBasicProduct removeNonBasicProductListener;
    private BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener refreshCompleteCountListener;

    public static final int ITEM_BASIC = 1;
    public static final int ITEM_NO_BASIC = ITEM_BASIC + 1;
    public static final int ITEM_BASIC_HEADER = ITEM_NO_BASIC + 1;
    public static final int ITEM_NON_BASIC_HEADER = ITEM_BASIC_HEADER + 1;

    public BulkInitialInventoryAdapter(List<InventoryViewModel> data,
                                       RemoveNonBasicProduct removeNonBasicProductListener,
                                       BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener refreshCompleteCountListener) {
        super(data);
        this.removeNonBasicProductListener = removeNonBasicProductListener;
        this.refreshCompleteCountListener = refreshCompleteCountListener;
    }

    protected void populate(RecyclerView.ViewHolder viewHolder, int position) {
        final InventoryViewModel viewModel = filteredList.get(position);
        BulkInitialInventoryWithLotViewHolder holder = (BulkInitialInventoryWithLotViewHolder) viewHolder;
        holder.populate((BulkInitialInventoryViewModel) viewModel, queryKeyWord, refreshCompleteCountListener, removeNonBasicProductListener);
    }

    @Override
    public String getTextToShowInBubble(int position) {

        if (position < 0 || position >= data.size())
            return null;

        String name = data.get(position).getProductName();
        if (name == null || name.length() < 1)
            return null;
        return data.get(position).getProductName().substring(0, 1);
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getViewType();
    }

    public boolean isHasDataChanged() {
        List<InventoryViewModel> data = getData();
        for (InventoryViewModel model : data) {
            if (model.isDataChanged()) {
                return true;
            }
        }
        return false;
    }

    public int validateAllForCompletedClick(String from) {
        int position = -1;
        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).validate()
                    && !(data.get(i).getViewType() == BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
                    || data.get(i).getViewType() == BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER)) {
                ((BulkInitialInventoryViewModel) data.get(i)).setFrom(from);
                if (position == -1 || i < position) {
                    position = i;
                }
            }
        }
        this.notifyDataSetChanged();
        return position;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_BASIC_HEADER) {
            return new BulkInitialInventoryWithLotViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_header, parent, false));
        } else if (viewType == ITEM_NON_BASIC_HEADER) {
            return new BulkInitialInventoryWithLotViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_non_basic_header, parent, false));
        } else {
            return new BulkInitialInventoryWithLotViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bulk_item_initial_inventory, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (position >= filteredList.size()) {
            return;
        }
        populate(holder, position);
    }

    public interface RemoveNonBasicProduct {
        void removeNoneBasicProduct(BulkInitialInventoryViewModel viewModel);
    }
}
