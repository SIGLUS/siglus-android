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

import static org.openlmis.core.LMISApp.getContext;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.listener.OnUpdatePodListener;
import org.openlmis.core.view.viewmodel.IssueVoucherReportLotViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

public class IssueVoucherReportLotAdapter extends BaseAdapter {

  private final Context context;

  private final List<IssueVoucherReportLotViewModel> lotViewModelList;

  @Setter
  private OnUpdatePodListener onUpdatePodListener;


  public IssueVoucherReportLotAdapter(Context context, List<IssueVoucherReportLotViewModel> lotViewModelList) {
    this.context = context;
    this.lotViewModelList = lotViewModelList;
  }

  @Override
  public int getCount() {
    return lotViewModelList.size();
  }

  @Override
  public IssueVoucherReportLotViewModel getItem(int position) {
    return lotViewModelList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    IssueVoucherReportLotViewHolder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(context).inflate(R.layout.item_issue_voucher_report_lot, parent, false);
      viewHolder = new IssueVoucherReportLotViewHolder(convertView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (IssueVoucherReportLotViewHolder) convertView.getTag();
    }
    viewHolder.populate(lotViewModelList.get(position), position);
    return convertView;
  }

  protected class IssueVoucherReportLotViewHolder extends BaseViewHolder {

    private EditText etQuantityShipped;
    private TextView tvLotCode;
    private TextView tvLotExpireDate;
    private TextView tvPrice;
    private TextView tvTotalValue;
    private TextView tvQuantityReturned;
    private EditText etQuantityAccepted;
    private EditText etNote;
    private IssueVoucherReportLotViewModel lotViewModel;
    private View vRejectionReason;
    private TextView tvRejectionReason;
    private ImageView ivRejectionReason;
    private ImageView icLotClear;
    private Button btnRemoveLot;
    private int position;

    public IssueVoucherReportLotViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportLotViewModel lotViewModel, int position) {
      this.position = position;
      this.lotViewModel = lotViewModel;
      initView();
      if (lotViewModel.getLot() != null && !lotViewModel.getLot().getProduct().isKit()) {
        tvLotCode.setText(lotViewModel.getLot().getLotNumber());
        tvLotExpireDate.setText(DateUtil
            .formatDate(lotViewModel.getLot().getExpirationDate(), DateUtil.SIMPLE_DATE_FORMAT));
      }
      tvPrice.setText(getPrice(lotViewModel));
      updateTotalValue(lotViewModel);
      etQuantityShipped.setText(convertLongValueToString(lotViewModel.getShippedQuantity()));
      etQuantityAccepted.setText(convertLongValueToString(lotViewModel.getAcceptedQuantity()));
      tvQuantityReturned.setText(convertLongValueToString(lotViewModel.compareAcceptedAndShippedQuantity()));
      etNote.setText(lotViewModel.getNotes() == null ? "" : lotViewModel.getNotes());
      icLotClear.setOnClickListener(getOnClickListenerForDeleteIcon());
      updateClearButtonStatus(lotViewModel);
      if (lotViewModel.getOrderStatus() == OrderStatus.SHIPPED) {
        setViewForShipped();
      } else {
        setViewForReceived();
      }
      btnRemoveLot.setOnClickListener(getOnRemoveListenerForRemoveLotButton());
      updateBtnRemoveNewAddedLot();
    }

    private OnClickListener getOnRemoveListenerForRemoveLotButton() {
      return view -> {
        if (onUpdatePodListener != null) {
          onUpdatePodListener.onRemove(position);
        }
      };
    }

    private void updateBtnRemoveNewAddedLot() {
      if (lotViewModel.isAdded()) {
        btnRemoveLot.setVisibility(View.VISIBLE);
      } else {
        btnRemoveLot.setVisibility(View.GONE);
      }
    }

    private String getPrice(IssueVoucherReportLotViewModel lotViewModel) {
      String price = lotViewModel.getLot().getProduct().getPrice();
      return price == null ? "" : price;
    }

    private void updateTotalValue(IssueVoucherReportLotViewModel lotViewModel) {
      if (lotViewModel.getTotalValue() == null) {
        tvTotalValue.setText("");
        return;
      }
      tvTotalValue.setText(lotViewModel.getTotalValue().toString());
    }

    private void updateClearButtonStatus(IssueVoucherReportLotViewModel viewModel) {
      if (viewModel.shouldShowLotClear()) {
        icLotClear.setVisibility(View.VISIBLE);
      } else {
        icLotClear.setVisibility(View.GONE);
      }
    }

    private void setViewForReceived() {
      setRejectReasonText();
      setEditStatus(false);
      etQuantityShipped.setBackground(null);
      etQuantityShipped.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
      etQuantityAccepted.setBackground(null);
      etQuantityAccepted.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
      etNote.setBackground(null);
      etNote.setGravity(Gravity.CENTER);
      vRejectionReason.setBackground(null);
      ivRejectionReason.setVisibility(View.GONE);
    }

    private void setViewForShipped() {
      setEditStatus(true);
      setErrorStatusNull();
      setQuantityShipped();
      SingleTextWatcher quantityAcceptedTextWatcher = getQuantityAcceptedTextWatcher();
      etQuantityAccepted.removeTextChangedListener(quantityAcceptedTextWatcher);
      etQuantityAccepted.addTextChangedListener(quantityAcceptedTextWatcher);
      SingleTextWatcher noteTextWatcher = getNoteTextWatcher();
      etNote.removeTextChangedListener(noteTextWatcher);
      etNote.addTextChangedListener(noteTextWatcher);
      setRejectReason();
      checkValidate();
    }

    private void setQuantityShipped() {
      if (lotViewModel.isLocal()) {
        SingleTextWatcher quantityShippedTextWatcher = getQuantityShippedTextWatcher();
        etQuantityShipped.removeTextChangedListener(quantityShippedTextWatcher);
        etQuantityShipped.addTextChangedListener(quantityShippedTextWatcher);
      } else {
        etQuantityShipped.setFocusable(false);
        etQuantityShipped.setBackground(null);
        etQuantityShipped.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
      }
    }

    private void setRejectReason() {
      if (lotViewModel.isAdded()) {
        vRejectionReason.setOnClickListener(null);
        vRejectionReason.setBackgroundResource(R.drawable.border_bg_corner_gray);
        ivRejectionReason.setImageResource(R.drawable.ic_pulldown_unable);

        tvRejectionReason.setError(null);

        List<MovementReason> movementReasons = MovementReasonManager.getInstance()
            .buildReasonListForMovementType(MovementType.REJECTION);
        MovementReason rejectionReason = from(movementReasons)
            .firstMatch(movementReason -> movementReason != null && lotViewModel.getRejectedReason()
                .equals(movementReason.getCode()))
            .orNull();
        if (rejectionReason != null) {
          tvRejectionReason.setText(rejectionReason.getDescription());
        }

        return;
      }

      Long differenceQuality = lotViewModel.compareAcceptedAndShippedQuantity();
      if (differenceQuality != null && differenceQuality != 0) {
        setRejectReasonForCanSelectStatus();
        vRejectionReason.setOnClickListener(new SingleClickButtonListener() {
          @Override
          public void onSingleClick(View v) {
            Bundle bundle = new Bundle();

            List<MovementReason> movementReasons = MovementReasonManager.getInstance()
                .buildReasonListForRejection(differenceQuality > 0);
            String[] reasonArray = from(movementReasons).transform(MovementReason::getDescription)
                .toArray(String.class);
            bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, reasonArray);
            SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
            reasonsDialog.setArguments(bundle);
            reasonsDialog
                .setItemClickListener(new MovementTypeOnClickListener(reasonsDialog, lotViewModel, movementReasons));
            reasonsDialog.show(((BaseActivity) itemView.getContext()).getSupportFragmentManager(), "SELECT_REASONS");
          }
        });
        setRejectReasonText();
      } else {
        vRejectionReason.setOnClickListener(null);
        vRejectionReason.setBackgroundResource(R.drawable.border_bg_corner_gray);
        ivRejectionReason.setImageResource(R.drawable.ic_pulldown_unable);
        lotViewModel.setRejectedReason(null);
        tvRejectionReason.setError(null);
        tvRejectionReason.setText(itemView.getResources().getString(R.string.label_default_rejection_reason));
      }
    }

    private void setRejectReasonForCanSelectStatus() {
      vRejectionReason.setBackgroundResource(R.drawable.border_bg_corner);
      ivRejectionReason.setImageResource(R.drawable.icon_pulldown_enable);
      setRejectReasonText();
    }

    private void setRejectReasonText() {
      tvRejectionReason
          .setText(lotViewModel.getRejectionReasonDesc(lotViewModel.getOrderStatus() != OrderStatus.RECEIVED));
    }

    private void checkValidate() {
      if (lotViewModel.isValidate()) {
        return;
      }
      if (lotViewModel.isLocal() && lotViewModel.getShippedQuantity() == null) {
        etQuantityShipped.setError(getContext().getString(R.string.hint_error_field_required));
        etQuantityShipped.requestFocus();
      } else if (lotViewModel.getAcceptedQuantity() == null) {
        etQuantityAccepted.setError(getContext().getString(R.string.hint_error_field_required));
        etQuantityAccepted.requestFocus();
      } else if (lotViewModel.compareAcceptedAndShippedQuantity() != 0 && lotViewModel.getRejectedReason() == null) {
        tvRejectionReason.setError("");
        vRejectionReason.setBackgroundResource(R.drawable.border_bg_corner_red);
      }
    }

    private SingleTextWatcher getQuantityShippedTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable text) {
          try {
            String shippedQuantity = text.toString();
            Long quantityValue = StringUtils.isEmpty(shippedQuantity) ? null : Long.parseLong(shippedQuantity);
            if (!checkEqual(quantityValue, lotViewModel.getShippedQuantity())) {
              lotViewModel.setShippedQuantity(quantityValue);
              onUpdatePodListener.onUpdateTotalValue();
            }
            updateTotalValue(lotViewModel);
            etQuantityShipped.setError(null);
            tvQuantityReturned.setText(convertLongValueToString(lotViewModel.compareAcceptedAndShippedQuantity()));
            setRejectReason();
          } catch (NumberFormatException e) {
            new LMISException(e, "lotViewModel shippedQuantity").reportToFabric();
          }
        }
      };
    }

    private boolean checkEqual(Long quantityValue, Long shippedValue) {
      if (quantityValue == null) {
        return shippedValue == null;
      }
      return quantityValue.equals(shippedValue);
    }

    private SingleTextWatcher getQuantityAcceptedTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          try {
            String acceptedQuantity = s.toString();
            Long acceptedValue = StringUtils.isEmpty(acceptedQuantity) ? null : Long.parseLong(acceptedQuantity);
            lotViewModel.setAcceptedQuantity(acceptedValue);
            etQuantityAccepted.setError(null);
            tvQuantityReturned.setText(convertLongValueToString(lotViewModel.compareAcceptedAndShippedQuantity()));
            setRejectReason();
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
      tvLotExpireDate = itemView.findViewById(R.id.tv_expiring_date);
      etQuantityAccepted = itemView.findViewById(R.id.et_quantity_accepted);
      tvQuantityReturned = itemView.findViewById(R.id.tv_quantity_returned);
      vRejectionReason = itemView.findViewById(R.id.v_rejection_reason);
      tvRejectionReason = itemView.findViewById(R.id.tv_rejection_reason);
      ivRejectionReason = itemView.findViewById(R.id.iv_rejection_reason);
      tvPrice = itemView.findViewById(R.id.tv_price);
      tvTotalValue = itemView.findViewById(R.id.tv_value);
      etNote = itemView.findViewById(R.id.et_note);
      icLotClear = itemView.findViewById(R.id.iv_clear);
      btnRemoveLot = itemView.findViewById(R.id.btn_remove_new_added_lot);
    }

    private String convertLongValueToString(Long value) {
      if (value == null) {
        return "";
      }
      return value.toString();
    }

    private void setEditStatus(boolean isFocus) {
      etQuantityShipped.setFocusable(!lotViewModel.isAdded() && isFocus);
      etQuantityAccepted.setFocusable(isFocus);
      etNote.setFocusable(isFocus);
    }

    private void setErrorStatusNull() {
      etQuantityShipped.setError(null);
      etQuantityAccepted.setError(null);
      tvRejectionReason.setError(null);
      etNote.setError(null);
    }

    @NonNull
    private SingleClickButtonListener getOnClickListenerForDeleteIcon() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          final SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
              HtmlCompat.fromHtml(getString(R.string.msg_remove_lot_title),
                  HtmlCompat.FROM_HTML_MODE_LEGACY),
              HtmlCompat.fromHtml(getContext().getResources()
                      .getString(R.string.msg_remove_lot, lotViewModel.getLot().getLotNumber(),
                          lotViewModel.getLot().getExpirationDate(),
                          lotViewModel.getLot().getProduct().getPrimaryName()),
                  HtmlCompat.FROM_HTML_MODE_LEGACY),
              getString(R.string.btn_remove_lot),
              getString(R.string.btn_cancel), "confirm_dialog");
          dialogFragment.show(((BaseActivity) itemView.getContext()).getSupportFragmentManager(), "confirm_dialog");
          dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
            @Override
            public void positiveClick(String tag) {
              onUpdatePodListener.onRemove(position);
            }

            @Override
            public void negativeClick(String tag) {
              dialogFragment.dismiss();
            }
          });
        }
      };
    }

    private String getString(int id) {
      return getContext().getResources().getString(id);
    }

    class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {

      private final SimpleSelectDialogFragment reasonsDialog;
      private final IssueVoucherReportLotViewModel viewModel;
      private final List<MovementReason> rejectReasons;

      public MovementTypeOnClickListener(SimpleSelectDialogFragment reasonsDialog,
          IssueVoucherReportLotViewModel viewModel, List<MovementReason> reasons) {
        this.reasonsDialog = reasonsDialog;
        this.rejectReasons = reasons;
        this.viewModel = viewModel;
      }

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        viewModel.setRejectedReason(rejectReasons.get(position).getCode());
        setRejectReasonForCanSelectStatus();
        tvRejectionReason.setError(null);
        reasonsDialog.dismiss();
      }
    }
  }
}
