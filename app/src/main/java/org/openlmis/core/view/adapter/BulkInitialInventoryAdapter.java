package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.openlmis.core.R;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryWithLotViewHolder;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.List;


public class BulkInitialInventoryAdapter extends InventoryListAdapter<BaseViewHolder> {
    private static final String TAG = BulkInitialInventoryAdapter.class.getSimpleName();
    private final SingleClickButtonListener saveClickListener;
    private final SingleClickButtonListener completeClickListener;
    private final View.OnClickListener removeNonBasicProductListener;
    private BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener refreshCompleteCountListener;

    public BulkInitialInventoryAdapter(List<InventoryViewModel> data,
                                       View.OnClickListener removeNonBasicProductListener,
                                       SingleClickButtonListener saveClickListener,
                                       SingleClickButtonListener completeClickListener,
                                       BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener refreshCompleteCountListener) {
        super(data);
        Log.e(TAG,"BulkInitialInventoryAdapter data size = " + data.size());
        this.removeNonBasicProductListener =  removeNonBasicProductListener;
        this.saveClickListener = saveClickListener;
        this.completeClickListener = completeClickListener;
        this.refreshCompleteCountListener = refreshCompleteCountListener;
    }

    protected void populate(RecyclerView.ViewHolder viewHolder, int position) {
        final InventoryViewModel viewModel = filteredList.get(position);
        BulkInitialInventoryWithLotViewHolder holder = (BulkInitialInventoryWithLotViewHolder) viewHolder;
        holder.populate((BulkInitialInventoryViewModel) viewModel, queryKeyWord, refreshCompleteCountListener);
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

    public boolean isHasDataChanged() {
        List<InventoryViewModel> data = getData();
        for (InventoryViewModel model : data) {
            if (model.isDataChanged()) {
                return true;
            }
        }
        return false;
    }
    @Override
    public int validateAll() {
        int position = -1;
        for (int i = 0; i < data.size(); i++) {
            Log.e(TAG,"data.get(i).validate() = " + data.get(i).validate());
            if (!data.get(i).validate()) {
                if (position == -1 || i < position) {
                    position = i;
                }
            }
        }
        Log.e(TAG,"BulkInitialInventoryAdapter validateAll position = "+position);
        this.notifyDataSetChanged();
        return position;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG,"BulkInitialInventoryAdapter onCreateViewHolder ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bulk_item_initial_inventory, parent, false);
        return new BulkInitialInventoryWithLotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (position >= filteredList.size()) {
            return;
        }
        populate(holder, position);
    }
}
