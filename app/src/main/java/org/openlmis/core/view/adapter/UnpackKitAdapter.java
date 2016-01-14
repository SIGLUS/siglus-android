package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

public class UnpackKitAdapter extends InventoryListAdapter<RecyclerView.ViewHolder> implements FilterableAdapter {

    public UnpackKitAdapter(List<StockCardViewModel> data) {
        super(data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }
}
