package org.openlmis.core.view.holder;

import android.view.View;

import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class PhysicalInventoryWithLotsViewHolder extends AddLotViewHolder {
    public PhysicalInventoryWithLotsViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel viewModel, String queryKeyWord) {
        highlightQueryKeyWord(viewModel, queryKeyWord);
        super.populate(viewModel);
    }

    private void highlightQueryKeyWord(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
    }
}
