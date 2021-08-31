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
import android.widget.ImageView;
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

    private TextView tvQuantityShipped;
    private TextView tvLotCode;
    private EditText etQuantityAccepted;
    private TextView tvQuantityReturned;
    private View vRejectionReason;
    private TextView tvRejectionReason;
    private ImageView ivRejectionReason;
    private EditText etNote;

    public IssueVoucherReportLotViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportLotViewModel lotViewModel) {
      if (lotViewModel.getOrderStatus() == OrderStatus.SHIPPED) {

      }
    }
  }
}
