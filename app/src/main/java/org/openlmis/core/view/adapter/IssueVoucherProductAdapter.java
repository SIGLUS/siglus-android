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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.List;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.IssueVoucherItemType;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter.IssueVoucherProductViewHolder;
import org.openlmis.core.view.listener.OnUpdatePodListener;
import org.openlmis.core.view.viewmodel.IssueVoucherReportLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

public class IssueVoucherProductAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity,
    IssueVoucherProductViewHolder> {

  @Setter
  private OnUpdatePodListener productRemoveListener;


  public IssueVoucherProductAdapter() {
    addItemType(IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TOTAL.getValue(),
        R.layout.item_issue_voucher_report_summary_info);
    addItemType(IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TYPE.getValue(),
        R.layout.item_issue_voucher_report_product_info);
  }

  @Override
  protected void convert(@NonNull IssueVoucherProductViewHolder holder,
      MultiItemEntity viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherProductViewHolder extends BaseViewHolder {

    private LinearLayout productList;
    private LinearLayout lotList;
    private ImageView btnProductClear;

    public IssueVoucherProductViewHolder(View itemView) {
      super(itemView);
    }

    public void populate(MultiItemEntity viewModel) {
      if (viewModel.getItemType() == IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TYPE.getValue()) {
        TextView productCode = itemView.findViewById(R.id.products_code);
        productCode.setText(productViewModel.getPodProductItem().getProduct().getCode());
        TextView productName = itemView.findViewById(R.id.products_name);
        productName.setText(productViewModel.getPodProductItem().getProduct().getPrimaryName());
        productList = itemView.findViewById(R.id.products_list_item);
        lotList = itemView.findViewById(R.id.ll_lot_list);
        btnProductClear = itemView.findViewById(R.id.iv_clear);
        IssueVoucherReportProductViewModel productViewModel = (IssueVoucherReportProductViewModel) viewModel;
        updateClearButtonStatus(productViewModel);
        lotList.removeAllViews();
        btnProductClear.setOnClickListener(getRemoveClickListener());
        List<IssueVoucherReportLotViewModel> lotViewModels = productViewModel.getLotViewModelList();
        if (lotViewModels.isEmpty()) {
          return;
        }
        addEmptyLotView(lotViewModels);
      }
    }

    private void updateClearButtonStatus(IssueVoucherReportProductViewModel viewModel) {
      if (viewModel.shouldShowProductClear()) {
        btnProductClear.setVisibility(View.VISIBLE);
      } else {
        btnProductClear.setVisibility(View.GONE);
      }
    }

    private void addEmptyLotView(List<IssueVoucherReportLotViewModel> lotViewModels) {
      for (int i = 0; i < lotViewModels.size(); i++) {
        ViewGroup inflate = (ViewGroup) LayoutInflater.from(itemView.getContext())
            .inflate(R.layout.item_issue_voucher_lot_name, productList, false);
        LinearLayout lotItem = inflate.findViewById(R.id.ll_lot_name);
        if (i != lotViewModels.size() - 1) {
          lotItem.setBackground(null);
        }
        lotList.addView(inflate);
      }
    }

    private SingleClickButtonListener getRemoveClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          if (productRemoveListener != null) {
            productRemoveListener.onRemove(getLayoutPosition());
          }
        }
      };
    }
  }
}
