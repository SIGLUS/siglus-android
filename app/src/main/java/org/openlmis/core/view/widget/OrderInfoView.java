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

package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Date;
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class OrderInfoView extends LinearLayout {

  @InjectView(R.id.tv_order_name)
  private TextView orderName;

  @InjectView(R.id.tv_order_facility)
  private TextView tvOrderFacility;

  @InjectView(R.id.tv_period_info)
  private TextView tvPeriodInfo;

  @InjectView(R.id.tv_program)
  private TextView tvProgram;

  @InjectView(R.id.tv_supplying_depot)
  private TextView tvSupplyingDepot;

  @InjectView(R.id.tv_shipping_date)
  private TextView tvShippingDate;

  @Getter
  @InjectView(R.id.ll_pod_receive_info)
  private LinearLayout linearLayout;

  @InjectView(R.id.tv_delivered_by)
  private TextView tvDeliveredBy;

  @InjectView(R.id.tv_received_by)
  private TextView tvReceivedBy;


  public OrderInfoView(Context context) {
    super(context);
    init(context);
  }

  public OrderInfoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    LayoutInflater.from(context).inflate(R.layout.view_order_info, this);
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  public void refresh(Pod pod, IssueVoucherReportViewModel reportViewModel) {
    orderName.setText(reportViewModel.getPod().getOrderCode());
    tvOrderFacility.setText(SharedPreferenceMgr.getInstance().getCurrentUserFacility());
    tvProgram.setText(reportViewModel.getProgram().getProgramName());
    String supplyingName = pod.getOrderSupplyFacilityName();
    tvSupplyingDepot.setText(supplyingName == null ? "" : supplyingName);
    if (pod.getShippedDate() != null) {
      tvShippingDate.setText(DateUtil.formatDate(pod.getShippedDate(), DateUtil.SIMPLE_DATE_FORMAT));
    }
    setPeriodInfo(pod);
    if (pod.getOrderStatus() == OrderStatus.SHIPPED) {
      linearLayout.setVisibility(GONE);
    } else {
      tvDeliveredBy.setText(pod.getDeliveredBy());
      tvReceivedBy.setText(pod.getReceivedBy());
    }
  }

  private void setPeriodInfo(Pod pod) {
    String periodInfo = getReportingPeriod(pod);
    if (periodInfo.isEmpty()) {
      tvPeriodInfo.setVisibility(GONE);
    } else {
      tvPeriodInfo.setText(periodInfo);
    }
  }

  private String getReportingPeriod(Pod pod) {
    Date requisitionActualStartDate = pod.getRequisitionActualStartDate();
    Date requisitionActualEndDate = pod.getRequisitionActualEndDate();
    if (requisitionActualStartDate == null || requisitionActualEndDate == null) {
      return "";
    }
    String startDate = DateUtil.formatDate(requisitionActualStartDate, DateUtil.SIMPLE_DATE_FORMAT);
    String endDate = DateUtil.formatDate(requisitionActualEndDate, DateUtil.SIMPLE_DATE_FORMAT);
    return startDate + " - " + endDate;
  }

}
