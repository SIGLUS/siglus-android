package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import roboguice.inject.InjectView;

public class PhysicalInventoryWithLotsInventoryViewHolder extends LotInventoryViewHolder {
    @InjectView(R.id.product_name)
    TextView tvProductName;

    @InjectView(R.id.product_unit)
    TextView tvProductUnit;

    public PhysicalInventoryWithLotsInventoryViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel viewModel, String queryKeyWord) {
        super.populate(viewModel);
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledUnit()));
    }
}
