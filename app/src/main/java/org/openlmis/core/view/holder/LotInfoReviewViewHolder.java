package org.openlmis.core.view.holder;

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import roboguice.inject.InjectView;

public class LotInfoReviewViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_lot_info_review)
  TextView tvLotInfoReview;

  public LotInfoReviewViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(LotMovementViewModel viewModel) {
    long adjustmentQuantity = viewModel.getAdjustmentQuantity();
    if (adjustmentQuantity == 0) {
      tvLotInfoReview.setText(Html.fromHtml(context
          .getString(R.string.msg_physical_inventory_lot_review_no_adjustment,
              viewModel.getLotNumber())));
    } else if (adjustmentQuantity > 0) {
      tvLotInfoReview.setText(Html.fromHtml(context
          .getString(R.string.msg_physical_inventory_lot_review_positive_adjustment,
              viewModel.getLotNumber(), adjustmentQuantity)));
    } else {
      tvLotInfoReview.setText(Html.fromHtml(context
          .getString(R.string.msg_physical_inventory_lot_review_negative_adjustment,
              viewModel.getLotNumber(), Math.abs(adjustmentQuantity))));
    }
  }
}
