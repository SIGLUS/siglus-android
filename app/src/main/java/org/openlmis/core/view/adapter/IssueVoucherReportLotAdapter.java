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

package org.openlmis.core.view.adapter;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.IssueVoucherReportActivity;
import org.openlmis.core.view.adapter.IssueVoucherReportLotAdapter.IssueVoucherReportLotViewHolder;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.viewmodel.IssueVoucherReportLotViewModel;

public class IssueVoucherReportLotAdapter extends BaseQuickAdapter<IssueVoucherReportLotViewModel,
    IssueVoucherReportLotViewHolder> {

  public IssueVoucherReportLotAdapter() {
    super(R.layout.item_issue_voucher_report_lot);
  }

  @Override
  protected void convert(@NonNull IssueVoucherReportLotViewHolder holder,
      IssueVoucherReportLotViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherReportLotViewHolder extends BaseViewHolder {

    private EditText etQuantityShipped;
    private TextView tvLotCode;
    private TextView tvQuantityReturned;
    private EditText etQuantityAccepted;
    private EditText etNote;
    private IssueVoucherReportLotViewModel lotViewModel;
    private View vRejectionReason;
    private TextView tvRejectionReason;
    private ImageView ivRejectionReason;
    private String[] rejectReasons = new String[]{"damaged", "divided"};

    public IssueVoucherReportLotViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportLotViewModel lotViewModel) {
      this.lotViewModel = lotViewModel;
      initView();
      tvLotCode.setText(lotViewModel.getLot().getLotNumber());
      etQuantityShipped.setText(convertLongValueToString(lotViewModel.getShippedQuantity()));
      etQuantityAccepted.setText(convertLongValueToString(lotViewModel.getAcceptedQuantity()));
      etNote.setText(lotViewModel.getNotes());
      if (lotViewModel.getOrderStatus() == OrderStatus.SHIPPED) {
        setViewForShipped();
      } else {
        setViewForReceived();
      }
    }

    private void setViewForReceived() {
      setEditStatus(false);
      etQuantityShipped.setBackground(null);
      etQuantityAccepted.setBackground(null);
      etNote.setBackground(null);
      vRejectionReason.setBackground(null);
      ivRejectionReason.setVisibility(View.GONE);
    }

    private void setViewForShipped() {
      setEditStatus(true);
      if (lotViewModel.isLocal() == true) {
        SingleTextWatcher quantityShippedTextWatcher = getQuantityShippedTextWatcher();
        etQuantityShipped.removeTextChangedListener(quantityShippedTextWatcher);
        etQuantityShipped.addTextChangedListener(quantityShippedTextWatcher);
      } else {
        etQuantityShipped.setFocusable(false);
        etQuantityShipped.setBackground(null);
      }
      SingleTextWatcher quantityAcceptedTextWatcher = getQuantityAcceptedTextWatcher();
      etQuantityAccepted.removeTextChangedListener(quantityAcceptedTextWatcher);
      etQuantityAccepted.addTextChangedListener(quantityAcceptedTextWatcher);
      SingleTextWatcher noteTextWatcher = getNoteTextWatcher();
      etNote.removeTextChangedListener(noteTextWatcher);
      etNote.addTextChangedListener(noteTextWatcher);
      if (lotViewModel.getRejectedQuality() != null && lotViewModel.getRejectedQuality().longValue() > 0) {
        vRejectionReason.setBackgroundResource(R.drawable.border_bg_corner);
        ivRejectionReason.setImageResource(R.drawable.icon_pulldown_enable);
        tvRejectionReason.setText(itemView.getResources().getString(R.string.label_default_rejection_reason));
        vRejectionReason.setOnClickListener(v -> {
          Bundle bundle = new Bundle();
          bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, rejectReasons);
          SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
          reasonsDialog.setArguments(bundle);
          reasonsDialog.setMovementTypeOnClickListener(
              new IssueVoucherReportLotViewHolder.MovementTypeOnClickListener(reasonsDialog, lotViewModel));
          reasonsDialog.show(((IssueVoucherReportActivity) (((ContextWrapper) itemView.getContext()).getBaseContext()))
              .getSupportFragmentManager(), "SELECT_REASONS");
        });
      } else {
        vRejectionReason.setBackgroundResource(R.drawable.border_bg_bottom_gray);
        ivRejectionReason.setImageResource(R.drawable.ic_pulldown_unable);
        tvRejectionReason.setText(lotViewModel.getRejectedReason() == null
            ? itemView.getResources().getString(R.string.label_default_rejection_reason)
            : lotViewModel.getRejectedReason());
      }
    }


    private SingleTextWatcher getQuantityShippedTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable text) {
          try {
            lotViewModel.setShippedQuantity(Long.valueOf(text.toString()));
            tvQuantityReturned.setText(convertLongValueToString(lotViewModel.getRejectedQuality()));
          } catch (NumberFormatException e) {
            new LMISException(e, "lotViewModel shippedQuantity").reportToFabric();
          }
        }
      };
    }

    private SingleTextWatcher getQuantityAcceptedTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          try {
            lotViewModel.setAcceptedQuantity(Long.valueOf(s.toString()));
            tvQuantityReturned.setText(convertLongValueToString(lotViewModel.getRejectedQuality()));
          } catch (NumberFormatException e) {
            new LMISException(e, "issue voucher acceptedQuantity").reportToFabric();
          }
        }
      };
    }

    private SingleTextWatcher getNoteTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          lotViewModel.setNotes(s.toString());
        }
      };
    }

    private void initView() {
      etQuantityShipped = itemView.findViewById(R.id.et_quantity_shipped);
      tvLotCode = itemView.findViewById(R.id.tv_lot_code);
      etQuantityAccepted = itemView.findViewById(R.id.et_quantity_accepted);
      tvQuantityReturned = itemView.findViewById(R.id.tv_quantity_returned);
      vRejectionReason = itemView.findViewById(R.id.v_rejection_reason);
      tvRejectionReason = itemView.findViewById(R.id.tv_rejection_reason);
      ivRejectionReason = itemView.findViewById(R.id.iv_rejection_reason);
      etNote = itemView.findViewById(R.id.et_note);
    }

    private String convertLongValueToString(Long value) {
      if (value == null) {
        return "";
      }
      return value.toString();
    }

    private void setEditStatus(boolean isFocus) {
      etQuantityShipped.setFocusable(isFocus);
      etQuantityAccepted.setFocusable(isFocus);
      etNote.setFocusable(isFocus);
    }

    class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {

      private final SimpleSelectDialogFragment reasonsDialog;
      private IssueVoucherReportLotViewModel viewModel;

      public MovementTypeOnClickListener(SimpleSelectDialogFragment reasonsDialog,
          IssueVoucherReportLotViewModel viewModel) {
        this.reasonsDialog = reasonsDialog;
        this.viewModel = viewModel;
      }

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setRejectedReason(rejectReasons[position]);
        tvRejectionReason.setText(viewModel.getRejectedReason());
        reasonsDialog.dismiss();
      }
    }
  }
}
