package org.openlmis.core.view.holder;

import android.view.View;

import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class ExpiredStockCardListViewHolder extends StockCardViewHolder {

    public ExpiredStockCardListViewHolder(View itemView) {
        super(itemView, null);
    }

    @Override
    protected void inflateData(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvStockOnHand.setText(String.valueOf(inventoryViewModel.getStockOnHand()));
        tvProductName.setText(
                TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getProductStyledName())
        );
        initStockOnHandWarning(inventoryViewModel);
    }
}