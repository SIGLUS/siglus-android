package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

import roboguice.inject.InjectView;

public class RequisitionProductViewHolder extends BaseViewHolder {

    @InjectView(R.id.tx_FNM)
    TextView productCode;

    @InjectView(R.id.tx_product_name)
    TextView productName;

    public RequisitionProductViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final RequisitionFormItemViewModel entry) {
        productCode.setText(entry.getFmn());
        productName.setText(entry.getProductName());
    }
}
