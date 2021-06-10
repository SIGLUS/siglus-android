package org.openlmis.core.view.adapter;

import android.view.View;
import java.util.List;
import org.openlmis.core.view.holder.KitStockCardViewHolder;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class KitStockCardListAdapter extends StockCardListAdapter {

  public KitStockCardListAdapter(List<InventoryViewModel> inventoryViewModel,
      StockCardViewHolder.OnItemViewClickListener listener) {
    super(inventoryViewModel, listener);
  }

  @Override
  protected StockCardViewHolder createViewHolder(View view) {
    return new KitStockCardViewHolder(view, listener);
  }
}
