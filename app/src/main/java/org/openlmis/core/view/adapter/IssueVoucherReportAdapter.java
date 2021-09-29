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
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.List;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.IssueVoucherReportAdapter.IssueVoucherReportViewHolder;
import org.openlmis.core.view.listener.OnRemoveListener;
import org.openlmis.core.view.viewmodel.IssueVoucherReportLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;

public class IssueVoucherReportAdapter extends BaseQuickAdapter<IssueVoucherReportProductViewModel,
    IssueVoucherReportViewHolder> {

  public IssueVoucherReportAdapter() {
    super(R.layout.item_issue_voucher_report_product);
  }

  @Setter
  private OnRemoveListener onRemoveListener;

  @Override
  protected void convert(@NonNull IssueVoucherReportViewHolder holder,
      IssueVoucherReportProductViewModel viewModel) {
    holder.populate(viewModel);
  }

  public boolean isThisProductNoLot(int position) {
    return getData().get(position).getLotViewModelList().isEmpty();
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

  protected class IssueVoucherReportViewHolder extends BaseViewHolder implements OnRemoveListener {

    private TextView tvProductUnit;
    private TextView tvQuantityOrdered;
    private TextView tvPartialFulfilled;
    private ListView lvLotList;
    private IssueVoucherReportLotAdapter lotAdapter;

    public IssueVoucherReportViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    public void populate(IssueVoucherReportProductViewModel productViewModel) {
      initView(productViewModel);
      tvProductUnit.setText(productViewModel.getProductUnitName());
      tvQuantityOrdered.setText(productViewModel.getOrderedQuantity());
      tvPartialFulfilled.setText(productViewModel.getPartialFulfilledQuantity());
    }

    private void initView(IssueVoucherReportProductViewModel productViewModel) {
      lvLotList = itemView.findViewById(R.id.lv_issue_voucher_lot_list);
      List<IssueVoucherReportLotViewModel> lotViewModels = productViewModel.getLotViewModelList();
      lvLotList.getLayoutParams().height = (int) itemView.getResources().getDimension(R.dimen.px_50) * lotViewModels.size();
      lotAdapter = new IssueVoucherReportLotAdapter(itemView.getContext(), lotViewModels);
      lotAdapter.setOnRemoveListener(this);
      lvLotList.setAdapter(lotAdapter);
      tvProductUnit = itemView.findViewById(R.id.tv_product_dosage);
      tvQuantityOrdered = itemView.findViewById(R.id.tv_quantity_ordered);
      tvPartialFulfilled = itemView.findViewById(R.id.tv_partial_fulfilled);
    }

    @Override
    public void onRemove(int position) {
      notifyDataSetChanged();
      onRemoveListener.onRemove(getLayoutPosition(), position);
    }

    @Override
    public void onRemove(int productPosition, int lotPosition) {
      // do nothing
    }
  }
}
