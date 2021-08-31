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

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.view.adapter.IssueVoucherReportLotAdapter.IssueVoucherReportLotViewHolder;
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
    private TextView etQuantityShipped;
    private TextView tvLotCode;
    private EditText etQuantityAccepted;
    private EditText etNote;

    public IssueVoucherReportLotViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportLotViewModel lotViewModel) {
      initView();
      tvLotCode.setText(lotViewModel.getLot().getLotNumber());
      etQuantityShipped.setText(lotViewModel.getShippedQuantity());
      etQuantityAccepted.setText(lotViewModel.getAcceptedQuantity());
      etNote.setText(lotViewModel.getNotes());
      if (lotViewModel.getOrderStatus() == OrderStatus.SHIPPED) {
        setFocusableStatus(true);
      } else {
        setFocusableStatus(false);
      }
    }

    private void setFocusableStatus(boolean isFocus) {
      etQuantityShipped.setFocusable(isFocus);
      etQuantityAccepted.setFocusable(isFocus);
      etNote.setFocusable(isFocus);
    }

    private void initView() {
      etQuantityShipped = itemView.findViewById(R.id.tv_quantity_shipped);
      tvLotCode = itemView.findViewById(R.id.tv_lot_code);
      etQuantityAccepted = itemView.findViewById(R.id.et_quantity_accepted);
      //      TextView tvQuantityReturned = itemView.findViewById(R.id.tv_quantity_returned);
      //      View vRejectionReason = itemView.findViewById(R.id.v_rejection_reason);
      //      TextView tvRejectionReason = itemView.findViewById(R.id.tv_rejection_reason);
      //      ImageView ivRejectionReason = itemView.findViewById(R.id.iv_rejection_reason);
      etNote = itemView.findViewById(R.id.et_note);
    }
  }
}
