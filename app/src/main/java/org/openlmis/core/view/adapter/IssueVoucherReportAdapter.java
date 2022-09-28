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
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.List;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.IssueVoucherItemType;
import org.openlmis.core.view.adapter.IssueVoucherReportAdapter.IssueVoucherReportViewHolder;
import org.openlmis.core.view.listener.OnUpdatePodListener;
import org.openlmis.core.view.viewmodel.IssueVoucherReportLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportSummaryViewModel;

public class IssueVoucherReportAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity,
    IssueVoucherReportViewHolder> {
  @Setter
  private OnUpdatePodListener onUpdatePodListener;

  public IssueVoucherReportAdapter() {
    addItemType(IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TOTAL.getValue(),
        R.layout.item_issue_voucher_report_product_summary);
    addItemType(IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TYPE.getValue(),
        R.layout.item_issue_voucher_report_product);
  }

  @Override
  protected void convert(@NonNull IssueVoucherReportViewHolder holder, MultiItemEntity viewModel) {
    holder.populate(viewModel);
  }

  public int validateAll() {
    int position = -1;
    int dataSize = getData().size();
    for (int i = 0; i < dataSize - 1; i++) {
      IssueVoucherReportProductViewModel productViewModel = (IssueVoucherReportProductViewModel) getData().get(i);
      if (!productViewModel.validate()){
        position = i;
        break;
      }
    }
    notifyDataSetChanged();
    return position;
  }

  protected class IssueVoucherReportViewHolder extends BaseViewHolder implements OnUpdatePodListener {

    private TextView tvQuantityOrdered;
    private TextView tvPartialFulfilled;
    private TextView tvPreparedBy;
    private TextView tvConferredBy;
    private TextView tvReceivedBy;

    public IssueVoucherReportViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    public void populate(MultiItemEntity viewModel) {
      if (viewModel.getItemType() == IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TYPE.getValue()) {
        IssueVoucherReportProductViewModel productViewModel = (IssueVoucherReportProductViewModel) viewModel;
        initView(productViewModel);
        tvQuantityOrdered.setText(productViewModel.getOrderedQuantity());
        tvPartialFulfilled.setText(productViewModel.getPartialFulfilledQuantity());
      } else {
        IssueVoucherReportSummaryViewModel summaryViewModel = (IssueVoucherReportSummaryViewModel) viewModel;
        TextView totalValue = itemView.findViewById(R.id.tv_value);
        totalValue.setText(summaryViewModel.getTotal().toString());
        initSummaryView();
        tvReceivedBy.setText(summaryViewModel.getPod().getReceivedBy());
        tvPreparedBy.setText(summaryViewModel.getPod().getPreparedBy());
        tvConferredBy.setText(summaryViewModel.getPod().getConferredBy());
      }
    }

    private void initSummaryView() {
      tvPreparedBy = itemView.findViewById(R.id.tv_prepared_by);
      tvConferredBy = itemView.findViewById(R.id.tv_conferred_by);
      tvReceivedBy = itemView.findViewById(R.id.tv_received_by);
    }


    private void initView(IssueVoucherReportProductViewModel productViewModel) {
      ListView lvLotList = itemView.findViewById(R.id.lv_issue_voucher_lot_list);
      List<IssueVoucherReportLotViewModel> lotViewModels = productViewModel.getLotViewModelList();
      lvLotList.getLayoutParams().height =
          (int) itemView.getResources().getDimension(R.dimen.px_50) * lotViewModels.size();
      IssueVoucherReportLotAdapter lotAdapter = new IssueVoucherReportLotAdapter(itemView.getContext(), lotViewModels);
      lotAdapter.setOnUpdatePodListener(this);
      lvLotList.setAdapter(lotAdapter);
      tvQuantityOrdered = itemView.findViewById(R.id.tv_quantity_ordered);
      tvPartialFulfilled = itemView.findViewById(R.id.tv_partial_fulfilled);
    }

    @Override
    public void onRemove(int position) {
      notifyDataSetChanged();
      onUpdatePodListener.onRemove(getLayoutPosition(), position);
    }

    @Override
    public void onRemove(int productPosition, int lotPosition) {
      // do nothing
    }

    @Override
    public void onUpdateTotalValue() {
      onUpdatePodListener.onUpdateTotalValue();
    }

  }
}
