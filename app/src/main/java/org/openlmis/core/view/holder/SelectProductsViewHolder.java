package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import roboguice.inject.InjectView;


public class SelectProductsViewHolder extends BaseViewHolder {

    @InjectView(R.id.product_name)
    TextView productName;

    @InjectView(R.id.product_unit)
    TextView productUnit;

    @InjectView(R.id.touchArea_checkbox)
    LinearLayout taCheckbox;

    public SelectProductsViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(InventoryViewModel viewModel) {
        productName.setText(viewModel.getStyledName());
        productUnit.setText(viewModel.getStyleType());
    }
}
