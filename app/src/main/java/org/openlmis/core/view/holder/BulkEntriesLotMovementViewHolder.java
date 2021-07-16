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

import android.content.ContextWrapper;
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
import java.text.MessageFormat;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.adapter.BulkEntriesLotMovementAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
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
  @InjectView(R.id.ly_movement_reason)
  private TextInputLayout lyMovementReason;
  @InjectView(R.id.ly_document_number)
  private TextInputLayout lyDocumentNumber;
  @InjectView(R.id.tv_lot_soh)
  private TextView lotStockOnHand;
  @InjectView(R.id.vg_lot_soh)
  private ViewGroup vgLotSOH;

  private List<MovementReason> movementReasons;

  String [] reasonDescriptions;

  @Setter
  private AmountChangeListener amountChangeListener;

  private LotMovementViewModel lotMovementViewModel;

  public BulkEntriesLotMovementViewHolder(View itemView, List<MovementReason> movementReasons) {
    super(itemView);
    this.movementReasons = movementReasons;
    this.reasonDescriptions = FluentIterable.from(movementReasons)
        .transform(MovementReason::getDescription).toArray(String.class);
  }

  public void populate(final LotMovementViewModel viewModel,
      BulkEntriesLotMovementAdapter bulkEntriesLotMovementAdapter) {
    this.lotMovementViewModel = viewModel;
    lotNumber.setText(MessageFormat.format("{0} - {1}", viewModel.getLotNumber(), viewModel.getExpiryDate()));
    etLotAmount.setText(viewModel.getQuantity());
    lotStockOnHand.setText(viewModel.getLotSoh());
    movementReason.setText(viewModel.getMovementReason());
    documentNumber.setText(viewModel.getDocumentNumber());
    setErrorEnable();
    if (viewModel.isNewAdded()) {
      setLotSohTip();
      btnDelLot.setVisibility(View.VISIBLE);
    }
    if (!viewModel.isValid()) {
      setLotInfoError();
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
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, reasonDescriptions);
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog.setMovementTypeOnClickListener(
            new MovementTypeOnClickListener(reasonsDialog, viewModel));
        reasonsDialog.show(((BulkEntriesActivity) (((ContextWrapper) view.getContext()).getBaseContext()))
                .getSupportFragmentManager(), "SELECT_REASONS");
      }
    };
  }

  private String getString(int id) {
    return context.getResources().getString(id);
  }

  private void setErrorEnable() {
    lyLotAmount.setErrorEnabled(false);
    lyDocumentNumber.setErrorEnabled(false);
    lyMovementReason.setErrorEnabled(false);
  }

  private void setLotInfoError() {
    setLyLotAmountError();
    setLyMovementReason();
    setLyDocumentNumberError();
  }

  private void setLyLotAmountError() {
    if (StringUtils.isBlank(lotMovementViewModel.getQuantity()) && lotMovementViewModel.isNewAdded()) {
      lyLotAmount.setError(getString(R.string.msg_empty_quantity));
    }
  }

  private void setLyDocumentNumberError() {
    if (StringUtils.isBlank(lotMovementViewModel.getDocumentNumber())) {
      lyDocumentNumber.setError(getString(R.string.msg_empty_document_number));
    }
  }

  private void setLyMovementReason() {
    if (StringUtils.isBlank(lotMovementViewModel.getMovementReason())) {
      lyMovementReason.setError(getString(R.string.msg_empty_movement_reason));
    }
  }

  private void setLotSohTip() {
    if (StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
      lotSohTip.setText(getString(R.string.label_new_added_lot));
    } else {
      vgLotSOH.setVisibility(View.GONE);
    }
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

  public interface AmountChangeListener {

    void onAmountChange();
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
      movementReason.setText(reasonDescriptions[position]);
      viewModel.setMovementReason(movementReasons.get(position).getCode());
      lyMovementReason.setErrorEnabled(false);
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
          amountChangeListener.onAmountChange();
        }
      } else if (itemView.getId() == R.id.et_movement_document_number) {
        viewModel.setDocumentNumber(editable.toString());
        lyDocumentNumber.setErrorEnabled(false);
        updateVgDocumentNumberError();
      }
    }

    private void updateVgDocumentNumberError() {
      setLyDocumentNumberError();
    }

    private void updateVgLotSOHAndError() {
      if (viewModel.isNewAdded()) {
        if (!StringUtils.isBlank(viewModel.getQuantity()) && Long.parseLong(viewModel.getQuantity()) > 0) {
          vgLotSOH.setVisibility(View.GONE);
        } else {
          vgLotSOH.setVisibility(View.VISIBLE);
          setLyLotAmountError();
        }
      }
    }
  }
}
