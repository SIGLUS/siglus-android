package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class PhysicalInventoryWithLotsViewHolder extends BaseViewHolder {
    TextView tvProductName;
    TextView tvProductUnit;

    public PhysicalInventoryWithLotsViewHolder(View itemView) {
        super(itemView);
        tvProductName = (TextView) itemView.findViewById(R.id.product_name);
        tvProductUnit = (TextView) itemView.findViewById(R.id.product_unit);
    }

    public void populate(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
    }
}
