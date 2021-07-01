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
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.adapter.BulkEntriesLotMovementAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.InjectView;

public class BulkEntriesLotMovementViewHolder extends BaseViewHolder {

  @InjectView(R.id.et_movement_reason)
  private EditText movementReason;
  @InjectView(R.id.tv_lot_soh_tip)
  private TextView lotSohTip;
  @InjectView(R.id.et_movement_document_number)
  private EditText documentNumber;
  @InjectView(R.id.btn_delete_lot)
  private ImageView btnDelLot;
  @InjectView(R.id.tv_lot_number)
  private TextView lotNumber;
  @InjectView(R.id.et_lot_amount)
  private EditText etLotAmount;
  @InjectView(R.id.ly_lot_amount)
  private TextInputLayout lyLotAmount;
  @InjectView(R.id.tv_lot_soh)
  private TextView lotStockOnHand;
  @InjectView(R.id.vg_lot_soh)
  private ViewGroup vgLotSOH;
  private String[] movementReasons;

  private AmountChangeListener amountChangeListener;

  public BulkEntriesLotMovementViewHolder(View itemView, String[] movementReasons) {
    super(itemView);
    this.movementReasons = movementReasons;
  }

  public void populate(final LotMovementViewModel viewModel,
      BulkEntriesLotMovementAdapter bulkEntriesLotMovementAdapter) {
    lotNumber.setText(viewModel.getLotNumber() + " - " + viewModel.getExpiryDate());
    etLotAmount.setText(viewModel.getQuantity());
    lotStockOnHand.setText(viewModel.getLotSoh());
    movementReason.setText(viewModel.getMovementReason());
    documentNumber.setText(viewModel.getDocumentNumber());
    lyLotAmount.setErrorEnabled(false);
    if (viewModel.isNewAdded()) {
      lotSohTip.setText(getString(R.string.label_new_added_lot));
      btnDelLot.setVisibility(View.VISIBLE);
      if (!viewModel.isValid()) {
        lyLotAmount.setError(getString(R.string.msg_empty_quantity));
      }
    }
    setUpViewListener(viewModel, bulkEntriesLotMovementAdapter);
  }

  public void setMovementChangeListener(
      BulkEntriesLotMovementViewHolder.AmountChangeListener amountChangeListener) {
    this.amountChangeListener = amountChangeListener;
  }

  private void setUpViewListener(LotMovementViewModel viewModel,
      BulkEntriesLotMovementAdapter adapter) {
    movementReason.setOnClickListener(getMovementReasonOnClickListener(viewModel));
    etLotAmount.addTextChangedListener(new EditTextWatcher(etLotAmount, viewModel));
    documentNumber.addTextChangedListener(new EditTextWatcher(documentNumber, viewModel));
    btnDelLot.setOnClickListener(getOnClickListenerForDeleteIcon(viewModel, adapter));
  }

  @NonNull
  private View.OnClickListener getMovementReasonOnClickListener(LotMovementViewModel viewModel) {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, movementReasons);
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog.setMovementTypeOnClickListener(
            new MovementTypeOnClickListener(reasonsDialog, viewModel));
        reasonsDialog.show(((BulkEntriesActivity) view.getContext()).getSupportFragmentManager(),
            "SELECT_REASONS");
      }
    };
  }

  private String getString(int id) {
    return context.getResources().getString(id);
  }

  private void setQuantityError(String string) {
    etLotAmount.requestFocus();
    lyLotAmount.setError(string);
  }

  @NonNull
  private View.OnClickListener getOnClickListenerForDeleteIcon(final LotMovementViewModel viewModel,
      final BulkEntriesLotMovementAdapter bulkEntriesLotMovementAdapter) {
    return v -> {
      final SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
          Html.fromHtml(getString(R.string.msg_remove_new_lot_title)),
          Html.fromHtml(context.getResources()
              .getString(R.string.msg_remove_new_lot, viewModel.getLotNumber(),
                  viewModel.getExpiryDate(), bulkEntriesLotMovementAdapter.getProductName())),
          getString(R.string.btn_remove_lot),
          getString(R.string.btn_cancel), "confirm_dialog");
      dialogFragment.show(((BaseActivity) context).getSupportFragmentManager(), "confirm_dialog");
      dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
        @Override
        public void positiveClick(String tag) {
          bulkEntriesLotMovementAdapter.remove(viewModel);
        }

        @Override
        public void negativeClick(String tag) {
          dialogFragment.dismiss();
        }
      });
    };
  }

  class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {

    private final SimpleSelectDialogFragment reasonsDialog;
    private LotMovementViewModel viewModel;

    public MovementTypeOnClickListener(SimpleSelectDialogFragment reasonsDialog,
        LotMovementViewModel viewModel) {
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
      if (itemView.getId() == R.id.et_lot_amount) {
        viewModel.setQuantity(editable.toString());
        lyLotAmount.setErrorEnabled(false);
        updateVgLotSOHAndError();
        if (!viewModel.isNewAdded()) {
          amountChangeListener.amountChange();
        }

      } else {
        viewModel.setDocumentNumber(editable.toString());
      }
    }

    private void updateVgLotSOHAndError() {
      if (viewModel.isNewAdded()) {
        if (viewModel.validateLotWithPositiveQuantity()) {
          vgLotSOH.setVisibility(View.GONE);
        } else {
          vgLotSOH.setVisibility(View.VISIBLE);
          setQuantityError(getString(R.string.msg_empty_quantity));
        }
      }
    }
  }

  public interface AmountChangeListener {

    void amountChange();
  }
}
