package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.MovementItemListAdapter;
import org.openlmis.core.view.viewmodel.StockHistoryViewModel;
import roboguice.inject.InjectView;

public class StockHistoryViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView tvProductName;

  @InjectView(R.id.tv_product_unit)
  TextView tvProductUnit;

  @InjectView(R.id.rv_stock_movement_item_list)
  RecyclerView movementHistoryListView;

  private StockHistoryViewModel viewModel;

  public StockHistoryViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(StockHistoryViewModel viewModel) {
    this.viewModel = viewModel;
    populateProductInfo();
    initRecyclerView();
  }

  private void initRecyclerView() {
    movementHistoryListView.setLayoutManager(new LinearLayoutManager(context));
    movementHistoryListView
        .setAdapter(new MovementItemListAdapter(viewModel.getFilteredMovementItemViewModelList()));
    movementHistoryListView.setNestedScrollingEnabled(false);
  }

  private void populateProductInfo() {
    tvProductName.setText(viewModel.getStyledProductName());
    tvProductUnit.setText(viewModel.getStyledProductUnit());
  }
}
