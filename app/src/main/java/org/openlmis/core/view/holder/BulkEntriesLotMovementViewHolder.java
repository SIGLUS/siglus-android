/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.holder;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.InjectView;

public class BulkEntriesLotMovementViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_lot_number)
  private TextView lotNumber;

  @InjectView(R.id.et_amount)
  private EditText lotAmount;

  @InjectView(R.id.tv_lot_soh)
  private TextView lotStockOnHand;

  @InjectView(R.id.et_movement_reason)
  EditText movementReason;

  @InjectView(R.id.et_movement_document_number)
  EditText documentNumber;

  @InjectView(R.id.ic_required)
  ImageView icRequired;

  private String[] movementReasons;

  public BulkEntriesLotMovementViewHolder(View itemView, String[] movementReasons) {
    super(itemView);
    this.movementReasons = movementReasons;
  }

  public void populate(final LotMovementViewModel viewModel) {
    lotNumber.setText(viewModel.getLotNumber());
    lotAmount.setText(viewModel.getQuantity());
    lotStockOnHand.setText(viewModel.getLotSoh());
    setUpViewListener(viewModel);
  }

  private void setUpViewListener(LotMovementViewModel viewModel) {
    movementReason.setOnClickListener(getMovementReasonOnClickListener(viewModel));
    lotAmount.addTextChangedListener(new EditTextWatcher(lotAmount,viewModel));
    documentNumber.addTextChangedListener(new EditTextWatcher(documentNumber,viewModel));
  }




  @NonNull
  private View.OnClickListener getMovementReasonOnClickListener(LotMovementViewModel viewModel) {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS,
            movementReasons);
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog
            .setMovementTypeOnClickListener(new MovementTypeOnClickListener(reasonsDialog,viewModel));
        reasonsDialog.show(((BulkEntriesActivity) view.getContext()).getSupportFragmentManager(), "SELECT_REASONS");
      }
    };
  }

  class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {

    private final SimpleSelectDialogFragment reasonsDialog;
    private LotMovementViewModel viewModel;

    public MovementTypeOnClickListener(SimpleSelectDialogFragment reasonsDialog,LotMovementViewModel viewModel) {
      this.reasonsDialog = reasonsDialog;
      this.viewModel = viewModel;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      movementReason.setText(movementReasons[position]);
      viewModel.setMovementReason(movementReasons[position]);

      reasonsDialog.dismiss();
    }
  }

  class EditTextWatcher extends SingleTextWatcher {

    private final LotMovementViewModel viewModel;
    private final View itemView;

    public EditTextWatcher(View itemView, LotMovementViewModel viewModel) {
      this.viewModel = viewModel;
      this.itemView = itemView;
    }

    @Override
    public void afterTextChanged(Editable editable) {
      if (itemView.getId() == R.id.et_amount) {
        viewModel.setQuantity(editable.toString());
      } else {
        viewModel.setDocumentNumber(editable.toString());
      }
    }

  }
}
