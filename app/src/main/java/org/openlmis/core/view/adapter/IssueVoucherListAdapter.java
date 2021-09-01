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
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.view.adapter.IssueVoucherListAdapter.IssueVoucherViewHolder;
import org.openlmis.core.view.listener.OrderOperationListener;
import org.openlmis.core.view.viewmodel.IssueVoucherListViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

public class IssueVoucherListAdapter extends BaseQuickAdapter<IssueVoucherListViewModel, IssueVoucherViewHolder> {

  @Setter
  private OrderOperationListener listener;

  public IssueVoucherListAdapter() {
    super(R.layout.item_issue_voucher_list);
  }

  @Override
  protected void convert(@NonNull IssueVoucherViewHolder holder, IssueVoucherListViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherViewHolder extends BaseViewHolder {

    private IssueVoucherListViewModel viewModel;

    public IssueVoucherViewHolder(@NonNull View view) {
      super(view);
    }

    public void populate(IssueVoucherListViewModel viewModel) {
      this.viewModel = viewModel;
      setText(R.id.tv_order_number, viewModel.getOrderNumber());
      setText(R.id.tv_requesting_facility, UserInfoMgr.getInstance().getFacilityName());
      setText(R.id.tv_program, viewModel.getProgramName());
      setText(R.id.tv_supplying_depot, viewModel.getOrderSupplyFacilityName());
      setText(R.id.tv_reporting_period, viewModel.getReportingPeriod());
      setText(R.id.tv_shipping_date, viewModel.getShippedDate());
      setText(R.id.tv_edit, viewModel.isIssueVoucher() ? R.string.btn_edit : R.string.label_view);
      setTextColor(R.id.tv_order_number, ContextCompat.getColor(itemView.getContext(), R.color.color_black));
      setText(R.id.tv_error_tips, viewModel.getErrorMsg());
      setGone(R.id.tv_error_tips, StringUtils.isEmpty(viewModel.getErrorMsg()) || viewModel.isIssueVoucher());
      setVisible(R.id.v_top_divider_line, !viewModel.isIssueVoucher());
      getView(R.id.tv_edit).setOnClickListener(new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          if (listener == null) {
            return;
          }
          listener.orderEditOrViewOperation(viewModel.getPod());
        }
      });
      if (viewModel.shouldShowOperationIcon()) {
        setGone(R.id.rl_operation, false);
        getView(R.id.rl_operation).setOnClickListener(new SingleClickButtonListener() {
          @Override
          public void onSingleClick(View v) {
            if (listener == null) {
              return;
            }
            listener.orderDeleteOrEditOperation(viewModel.getPod().getOrderStatus(), viewModel.getPod().getOrderCode());
          }
        });
      } else {
        setGone(R.id.rl_operation, true);
      }
      if (viewModel.isIssueVoucher()) {
        setForIssueVoucher();
      } else {
        setForPod();
      }
    }

    private void setForIssueVoucher() {
      boolean isLocal = viewModel.getPod().isLocal();
      setImageResource(R.id.iv_operation, R.drawable.ic_trashcan);
      setGone(R.id.iv_status, true);
      int backgroundColor = isLocal ? R.color.color_D8D8D8 : R.color.color_ffd149;
      setBackgroundColor(R.id.ll_title, ContextCompat.getColor(itemView.getContext(), backgroundColor));
    }

    private void setForPod() {
      Pod pod = viewModel.getPod();
      setImageResource(R.id.iv_operation, R.drawable.ic_edit);
      setGone(R.id.iv_status, false);
      int iconRes;
      int titleBackGroundRes;
      int titleTextColorRes;
      if (pod.isSynced()) {
        iconRes = R.drawable.ic_done_green_pod;
        titleBackGroundRes = R.color.color_white;
        titleTextColorRes = R.color.color_00912e;
      } else if (!pod.isSynced() && !pod.isLocal()) {
        iconRes = R.drawable.ic_error;
        titleBackGroundRes = R.color.color_red;
        titleTextColorRes = R.color.color_white;
      } else {
        iconRes = R.drawable.ic_error_outline_red;
        titleBackGroundRes = R.color.color_D8D8D8;
        titleTextColorRes = R.color.color_box_text_red;
      }
      setImageResource(R.id.iv_status, iconRes);
      setBackgroundColor(R.id.ll_title, ContextCompat.getColor(itemView.getContext(), titleBackGroundRes));
      setTextColor(R.id.tv_order_number, ContextCompat.getColor(itemView.getContext(), titleTextColorRes));
    }
  }
}
