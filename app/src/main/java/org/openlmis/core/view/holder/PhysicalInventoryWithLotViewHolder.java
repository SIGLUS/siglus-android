package org.openlmis.core.view.holder;

import android.view.View;

import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class PhysicalInventoryWithLotViewHolder extends InventoryWithLotViewHolder {
    public PhysicalInventoryWithLotViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel viewModel, String queryKeyWord) {
        super.populate(viewModel);
        highlightQueryKeyWord(viewModel, queryKeyWord);
    }

    private void highlightQueryKeyWord(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
    }
}
