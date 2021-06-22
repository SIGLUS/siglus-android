package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import roboguice.inject.InjectView;

public class BulkEntriesLotMovementViewHolder extends BaseViewHolder{

  @InjectView(R.id.tv_lot_number)
  private TextView lotNumber;

  @InjectView(R.id.et_amount)
  private EditText lotAmount;

  @InjectView(R.id.tv_lot_soh)
  private TextView lotStockOnHand;

  @InjectView(R.id.et_movement_reason)
  private EditText movementReason;

  @InjectView(R.id.et_movement_document_number)
  private EditText documentNumber;

  @InjectView(R.id.ic_required)
  private ImageView icRequired;

  public BulkEntriesLotMovementViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final LotMovementViewModel viewModel) {
    lotNumber.setText(viewModel.getLotNumber());
    lotAmount.setText(viewModel.getQuantity());
    lotStockOnHand.setText(viewModel.getLotSoh());
  }
}
