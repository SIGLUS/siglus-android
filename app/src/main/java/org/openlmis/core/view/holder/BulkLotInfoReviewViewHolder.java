package org.openlmis.core.view.holder;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import roboguice.inject.InjectView;

public class BulkLotInfoReviewViewHolder extends BaseViewHolder {
    public BulkLotInfoReviewViewHolder(View itemView) {
        super(itemView);
    }
    @InjectView(R.id.tv_lot_info_review)
    TextView tvLotInfoReview;

    public void populate(LotMovementViewModel viewModel) {
        long adjustmentQuantity = viewModel.getAdjustmentQuantity();
        tvLotInfoReview.setText(Html.fromHtml(context.getString(R.string.msg_initial_inventory_lot_review_add, viewModel.getLotNumber(), adjustmentQuantity)));
    }
}
