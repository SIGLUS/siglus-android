package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.BaseLotListView;

import roboguice.inject.InjectView;

public class PhysicalInventoryWithLotViewHolder extends BaseViewHolder {
    @InjectView(R.id.product_name)
    TextView tvProductName;

    @InjectView(R.id.product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.view_lot_list)
    BaseLotListView lotListView;

    protected InventoryViewModel viewModel;

    public PhysicalInventoryWithLotViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel viewModel, String queryKeyWord) {
        this.viewModel = viewModel;
        lotListView.initLotListView(viewModel);
        highlightQueryKeyWord(viewModel, queryKeyWord);
    }

    private void highlightQueryKeyWord(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
    }
}
