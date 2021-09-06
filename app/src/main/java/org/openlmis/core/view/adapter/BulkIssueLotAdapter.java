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

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.textfield.TextInputLayout;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.adapter.BulkIssueLotAdapter.BulkIssueLotViewHolder;
import org.openlmis.core.view.listener.AmountChangeListener;
import org.openlmis.core.view.viewmodel.BulkIssueLotViewModel;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;

public class BulkIssueLotAdapter extends BaseMultiItemQuickAdapter<BulkIssueLotViewModel, BulkIssueLotViewHolder> {

  @Setter
  private AmountChangeListener amountChangeListener;

  public BulkIssueLotAdapter() {
    addItemType(BulkIssueProductViewModel.TYPE_EDIT, R.layout.item_bulk_issue_lot_edit);
    addItemType(BulkIssueProductViewModel.TYPE_DONE, R.layout.item_bulk_issue_lot_done);
  }

  @Override
  protected void convert(@NonNull BulkIssueLotViewHolder holder, BulkIssueLotViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class BulkIssueLotViewHolder extends BaseViewHolder {

    private BulkIssueLotViewModel viewModel;

    private TextInputLayout tilAmount;

    public BulkIssueLotViewHolder(@NonNull View view) {
      super(view);
    }

    public void populate(BulkIssueLotViewModel viewModel) {
      this.viewModel = viewModel;
      setGone(R.id.tv_expired_tips, !viewModel.isExpired());
      LotOnHand lotOnHand = viewModel.getLotOnHand();
      Lot lot = lotOnHand.getLot();
      if (viewModel.isDone()) {
        setVisible(R.id.ll_issued_amount, !viewModel.isExpired());
        setText(R.id.tv_lot_number, lot.getLotNumber());
        setText(R.id.tv_issued_amount, viewModel.getAmount() == null ? "0" : viewModel.getAmount().toString());
      } else {
        tilAmount = getView(R.id.til_amount);
        setVisible(R.id.til_amount, !viewModel.isExpired());
        setText(R.id.tv_lot_number_and_date,
            lot.getLotNumber() + " - " + DateUtil.formatDateWithoutDay(lot.getExpirationDate()));
        setText(R.id.tv_existing_lot_on_hand,
            itemView.getContext().getString(R.string.label_existing_soh_of_lot)
                + "  "
                + lotOnHand
                .getQuantityOnHand());
        EditText etAmount = getView(R.id.et_amount);
        etAmount.setText(viewModel.getAmount() == null ? "" : viewModel.getAmount().toString());
        SingleTextWatcher amountTextWatcher = getAmountTextWatcher();
        etAmount.removeTextChangedListener(amountTextWatcher);
        etAmount.addTextChangedListener(amountTextWatcher);
        etAmount.setImeOptions(getLayoutPosition() == (getDefItemCount() - 1) ? IME_ACTION_DONE : IME_ACTION_NEXT);
        updateAmountTips();
      }
    }

    SingleTextWatcher getAmountTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          String issueAmountString = s.toString();
          Long issueAmount = StringUtils.isEmpty(issueAmountString) ? null : Long.parseLong(issueAmountString);
          viewModel.setAmount(issueAmount);
          updateAmountTips();
          if (amountChangeListener != null) {
            amountChangeListener.onAmountChange(issueAmountString);
          }
        }
      };
    }

    private void updateAmountTips() {
      if (viewModel.isBiggerThanSoh()) {
        tilAmount.setError(itemView.getContext().getString(R.string.msg_invalid_quantity));
      } else {
        tilAmount.setErrorEnabled(false);
        tilAmount.setError(null);
      }
    }
  }
}
