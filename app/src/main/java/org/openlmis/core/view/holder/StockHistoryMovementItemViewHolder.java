package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.StockMovementItem;

import roboguice.inject.InjectView;

public class StockHistoryMovementItemViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_movement_date)
    TextView tvMovementDate;

    @InjectView(R.id.tv_movement_reason)
    TextView tvMovementReason;

    @InjectView(R.id.tv_document_number)
    TextView tvMovementDocumentNumber;

    @InjectView(R.id.tv_entry_amount)
    TextView tvMovementEntryAmount;

    @InjectView(R.id.tv_negative_adjustment_amount)
    TextView tvNegativeAdjustmentAmount;

    @InjectView(R.id.tv_positive_adjustment_amount)
    TextView tvPositiveAdjustmentAmount;

    @InjectView(R.id.tv_issue_amount)
    TextView tvIssueAmount;

    @InjectView(R.id.tv_stock_existence)
    TextView tvStockExistence;


    public StockHistoryMovementItemViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(StockMovementItem stockMovementItem) {

    }
}
