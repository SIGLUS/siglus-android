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

import android.text.Editable;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.MessageFormat;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.IssueVoucherLotAdapter.IssueVoucherLotViewHolder;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.listener.AmountChangeListener;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;

public class IssueVoucherLotAdapter extends BaseMultiItemQuickAdapter<IssueVoucherLotViewModel,
    IssueVoucherLotViewHolder> {

  @Setter
  @Getter
  private AmountChangeListener amountChangeListener;

  public IssueVoucherLotAdapter() {
    addItemType(IssueVoucherLotViewModel.TYPE_EDIT, R.layout.item_issue_voucher_lot_edit);
    addItemType(IssueVoucherLotViewModel.TYPE_DONE, R.layout.item_issue_voucher_lot_done);
  }


  @Override
  protected void convert(@NotNull IssueVoucherLotViewHolder holder, IssueVoucherLotViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherLotViewHolder extends BaseViewHolder {

    private TextInputLayout tilShippedQuantity;
    private TextInputLayout tilAcceptedQuantity;
    private IssueVoucherLotViewModel viewModel;

    public IssueVoucherLotViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherLotViewModel viewModel) {
      this.viewModel = viewModel;
      if (viewModel.isDone()) {
        setText(R.id.tv_lot_number_and_date, viewModel.getLotNumber());
        setText(R.id.tv_quantity_shipped, "Quantity shipped: " + viewModel.getShippedQuantity());
        setText(R.id.tv_quantity_accepted, "Quantity accepted: " + viewModel.getAcceptedQuantity());
      } else {
        setText(R.id.tv_lot_number_and_date, MessageFormat.format("{0} - {1}",
            viewModel.getLotNumber(), viewModel.getExpiryDate()));
        tilShippedQuantity = getView(R.id.til_quantity_shipped);
        tilAcceptedQuantity = getView(R.id.til_quantity_accepted);
        setText(R.id.et_quantity_shipped,
            viewModel.getShippedQuantity() == null ? "" : viewModel.getShippedQuantity().toString());
        setText(R.id.et_quantity_accepted,
            viewModel.getAcceptedQuantity() == null ? "" : viewModel.getAcceptedQuantity().toString());
        TextInputEditText etShippedQuantity = getView(R.id.et_quantity_shipped);
        TextInputEditText etAcceptedQuantity = getView(R.id.et_quantity_accepted);
        etShippedQuantity.setSelection(Objects.requireNonNull(etShippedQuantity.getText()).toString().length());
        etAcceptedQuantity.setSelection(Objects.requireNonNull(etAcceptedQuantity.getText()).toString().length());
        etShippedQuantity.removeTextChangedListener(getShippedQuantityTextWatcher());
        etAcceptedQuantity.removeTextChangedListener(getAcceptedQuantityTextWatcher());
        etShippedQuantity.addTextChangedListener(getShippedQuantityTextWatcher());
        etAcceptedQuantity.addTextChangedListener(getAcceptedQuantityTextWatcher());
        ImageView ivDel = getView(R.id.iv_del);
        ivDel.setOnClickListener(getOnClickListenerForDeleteIcon());
        if (viewModel.isNewAdd()) {
          ivDel.setVisibility(View.VISIBLE);
        } else {
          ivDel.setVisibility(View.GONE);
        }
        updateQuantityTips();
      }
    }

    private SingleTextWatcher getShippedQuantityTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          String shippedQuantity = s.toString();
          Long quantityValue = StringUtils.isEmpty(shippedQuantity) ? null : Long.parseLong(shippedQuantity);
          viewModel.setShippedQuantity(quantityValue);
          updateQuantityTipsAfterTextChange();
          if (amountChangeListener != null) {
            amountChangeListener.onAmountChange(shippedQuantity);
          }
        }
      };
    }

    private SingleTextWatcher getAcceptedQuantityTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          String acceptedQuantity = s.toString();
          Long quantityValue = StringUtils.isEmpty(acceptedQuantity) ? null : Long.parseLong(acceptedQuantity);
          viewModel.setAcceptedQuantity(quantityValue);
          updateQuantityTipsAfterTextChange();
          if (amountChangeListener != null) {
            amountChangeListener.onAmountChange(acceptedQuantity);
          }
        }
      };
    }

    public void updateQuantityTips() {
      if (!viewModel.isValid() && viewModel.isShouldShowError()) {
        updateShippedQuantityTips();
        updateAcceptedQuantityTips();
      }
    }

    private void updateQuantityTipsAfterTextChange() {
      if (!viewModel.validateLot()) {
        updateShippedQuantityTips();
        updateAcceptedQuantityTips();
      } else {
        tilShippedQuantity.setErrorEnabled(false);
        tilShippedQuantity.setError(null);
        tilAcceptedQuantity.setErrorEnabled(false);
        tilAcceptedQuantity.setError(null);
      }
    }

    private void updateShippedQuantityTips() {
      if (viewModel.getShippedQuantity() == null) {
        tilShippedQuantity.setError(getString(R.string.msg_shipped_quantity_can_not_be_blank));
      } else if (viewModel.isShippedQuantityZero()) {
        tilShippedQuantity.setError(getString(R.string.msg_shipped_quantity_can_not_be_zero));
      } else if (viewModel.getShippedQuantity() > 0) {
        tilShippedQuantity.setError(null);
      }
    }

    private void updateAcceptedQuantityTips() {
      if (viewModel.getAcceptedQuantity() == null) {
        tilAcceptedQuantity.setError(getString(R.string.msg_accepted_quantity_can_not_be_blank));
      } else if (viewModel.getAcceptedQuantity() > 0) {
        tilAcceptedQuantity.setError(null);
      }
      if (viewModel.isAcceptedQuantityMoreThanShippedQuantity()) {
        tilAcceptedQuantity.setError(getString(R.string.msg_shipped_quantity_less_than_accepted_quantity));
      }
    }

    @NonNull
    private View.OnClickListener getOnClickListenerForDeleteIcon() {
      return v -> {
        final SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
            HtmlCompat.fromHtml(getString(R.string.msg_remove_new_lot_title), HtmlCompat.FROM_HTML_MODE_LEGACY),
            HtmlCompat.fromHtml(getContext().getResources()
                .getString(R.string.msg_remove_new_lot, viewModel.getLotNumber(),
                    viewModel.getExpiryDate(), viewModel.getProduct().getPrimaryName()),
                HtmlCompat.FROM_HTML_MODE_LEGACY),
            getString(R.string.btn_remove_lot),
            getString(R.string.btn_cancel), "confirm_dialog");
        dialogFragment.show(((BaseActivity) getContext()).getSupportFragmentManager(), "confirm_dialog");
        dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
          @Override
          public void positiveClick(String tag) {
            IssueVoucherLotAdapter.this.removeAt(getLayoutPosition());
          }

          @Override
          public void negativeClick(String tag) {
            dialogFragment.dismiss();
          }
        });
      };
    }

    private String getString(int id) {
      return getContext().getResources().getString(id);
    }
  }

}
