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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter.IssueVoucherProductViewHolder;

import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;

public class IssueVoucherProductAdapter extends BaseQuickAdapter<IssueVoucherReportProductViewModel,
    IssueVoucherProductViewHolder> {

  public IssueVoucherProductAdapter() {
    super(R.layout.item_issue_voucher_report_product_info);
  }

  @Override
  protected void convert(@NonNull IssueVoucherProductViewHolder holder,
      IssueVoucherReportProductViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherProductViewHolder extends BaseViewHolder {

    private TextView productName;
    private LinearLayout productList;

    public IssueVoucherProductViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportProductViewModel issueVoucherReportProductViewModel) {
      productName = itemView.findViewById(R.id.products_name);
      productList = itemView.findViewById(R.id.products_list_item);
      productName.setText(issueVoucherReportProductViewModel.getPodProductItem()
          .getProduct().getPrimaryName());
      if (issueVoucherReportProductViewModel.getLotViewModelList().size() > 0) {
        ViewGroup inflate = (ViewGroup) LayoutInflater.from(itemView.getContext())
            .inflate(R.layout.item_issue_voucher_lot_name, productList, false);
        productList.addView(inflate);
      }
    }
  }
}
