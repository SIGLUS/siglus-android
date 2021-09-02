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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.openlmis.core.R;

import org.openlmis.core.view.adapter.IssueVoucherReportAdapter.IssueVoucherReportViewHolder;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;

public class IssueVoucherReportAdapter extends BaseQuickAdapter<IssueVoucherReportProductViewModel,
    IssueVoucherReportViewHolder> {

  public IssueVoucherReportAdapter() {
    super(R.layout.item_issue_voucher_report_product);
  }

  @Override
  protected void convert(@NonNull IssueVoucherReportViewHolder holder,
      IssueVoucherReportProductViewModel viewModel) {
    holder.populate(viewModel);
  }

  public int validateAll() {
    int position = -1;
    for (int i = 0; i < getData().size(); i++) {
      if (getData().get(i).validate()) {
        continue;
      }
      if (position == -1) {
        position = i;
        break;
      }
    }
    notifyDataSetChanged();
    return position;
  }

  protected class IssueVoucherReportViewHolder extends BaseViewHolder {

    private TextView tvProductUnit;
    private TextView tvQuantityOrdered;
    private TextView tvPartialFulfilled;
    private RecyclerView rvLotList;
    private IssueVoucherReportLotAdapter lotAdapter;

    public IssueVoucherReportViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportProductViewModel productViewModel) {
      initView();
      lotAdapter.setList(productViewModel.getLotViewModelList());
      tvProductUnit.setText(productViewModel.getProductUnitName());
      tvPartialFulfilled.setText(productViewModel.getPartialFulfilledQuantity());
      tvQuantityOrdered.setText(productViewModel.getOrderedQuantity());
    }

    private void initView() {
      rvLotList = itemView.findViewById(R.id.rv_issue_voucher_lot_list);
      rvLotList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
      lotAdapter = new IssueVoucherReportLotAdapter();
      rvLotList.setAdapter(lotAdapter);
      tvProductUnit = itemView.findViewById(R.id.tv_product_unit);
      tvPartialFulfilled = itemView.findViewById(R.id.tv_partial_fulfilled);
      tvQuantityOrdered = itemView.findViewById(R.id.tv_quantity_ordered);
    }
  }
}
