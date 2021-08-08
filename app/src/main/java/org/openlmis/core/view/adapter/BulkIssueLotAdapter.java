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
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.BulkIssueLotViewModel;

public class BulkIssueLotAdapter extends BaseQuickAdapter<BulkIssueLotViewModel, BaseViewHolder> {

  public BulkIssueLotAdapter() {
    super(R.layout.item_bulk_issue_lot);
  }

  @Override
  protected void convert(@NonNull BaseViewHolder holder, BulkIssueLotViewModel viewModel) {
    holder.setVisible(R.id.tv_expired_tips, viewModel.isExpired());
    holder.setVisible(R.id.til_amount, !viewModel.isExpired());
    EditText etAmount = holder.getView(R.id.et_amount);
    etAmount.setText(viewModel.getAmount() == null ? "" : viewModel.getAmount().toString());
    LotOnHand lotOnHand = viewModel.getLotOnHand();
    Lot lot = lotOnHand.getLot();
    holder.setText(R.id.tv_lot_number_and_date,
        lot.getLotNumber() + " - " + DateUtil.formatDateWithLongMonthAndYear(lot.getExpirationDate()));
    holder.setText(R.id.tv_existing_lot_on_hand,
        holder.itemView.getContext().getString(R.string.label_existing_soh_of_lot)
            + "  "
            + lotOnHand
            .getQuantityOnHand());
    SingleTextWatcher amountTextWatcher = new SingleTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        viewModel.setAmount(StringUtils.isBlank(s) ? null : Long.parseLong(s.toString()));
      }
    };
    etAmount.removeTextChangedListener(amountTextWatcher);
    etAmount.addTextChangedListener(amountTextWatcher);
  }
}
