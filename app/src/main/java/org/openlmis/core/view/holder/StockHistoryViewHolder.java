package org.openlmis.core.view.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.adapter.MovementItemListAdapter;
import org.openlmis.core.view.viewmodel.StockMovementHistoryViewModel;

import roboguice.inject.InjectView;

public class StockHistoryViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_product_name)
    TextView tvProductName;

    @InjectView(R.id.tv_product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.rv_movement_history)
    RecyclerView movementHistoryListView;

    private StockMovementHistoryViewModel viewModel;

    public StockHistoryViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(StockMovementHistoryViewModel viewModel) {
        this.viewModel = viewModel;
        populateProductInfo();
        initRecyclerView();
    }

    private void initRecyclerView() {
        movementHistoryListView.setLayoutManager(new LinearLayoutManager(context));
        movementHistoryListView.setAdapter(new MovementItemListAdapter(viewModel.getFilteredMovementItemList()));
    }

    private void populateProductInfo() {
        tvProductName.setText(viewModel.getProductName());
        tvProductUnit.setText(viewModel.getProductUnit());
    }
}
