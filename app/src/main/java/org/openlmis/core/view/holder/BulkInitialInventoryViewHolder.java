package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import roboguice.inject.InjectView;

public class BulkInitialInventoryViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_product_name)
    TextView productName;

    @InjectView(R.id.tv_product_unit)
    TextView productUnit;

    private InventoryViewModel viewModel;

    public BulkInitialInventoryViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord) {
        this.viewModel = inventoryViewModel;

        productName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
        productUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyleType()));
    }
}
