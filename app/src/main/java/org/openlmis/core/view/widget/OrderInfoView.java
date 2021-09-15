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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.User;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class OrderInfoView extends LinearLayout {

  @InjectView(R.id.tv_supplier)
  private TextView tvSupplier;

  @InjectView(R.id.tv_district)
  private TextView tvDistrictName;

  @InjectView(R.id.tv_province)
  private TextView tvProvince;

  @InjectView(R.id.tv_client)
  private TextView tvClient;

  @InjectView(R.id.tv_requisition_number)
  private TextView tvRequisitionNumber;

  @InjectView(R.id.tv_issue_voucher_date)
  private TextView tvIssueVoucherDate;

  @Getter
  @InjectView(R.id.ll_pod_header_title)
  private LinearLayout llPodHeaderTitle;

  @InjectView(R.id.ll_pod_header_detail)
  private LinearLayout llPodHeaderDetail;

  @InjectView(R.id.tv_issue_voucher_number)
  private TextView tvIssueVoucherNumber;

  @InjectView(R.id.tv_pod_number)
  private TextView tvPodNumber;

  @InjectView(R.id.tv_reception_date_value)
  private TextView tvReceptionDate;

  @InjectView(R.id.bt_supply)
  private Button btSupply;


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
    if (reportViewModel.getPodStatus() == OrderStatus.SHIPPED) {
      llPodHeaderTitle.setVisibility(View.GONE);
      llPodHeaderDetail.setVisibility(View.GONE);
    }
    btSupply.setActivated(true);
    tvSupplier.setText(getValueNotNull(pod.getOrderSupplyFacilityName()));
    tvDistrictName.setText(getValueNotNull(pod.getOrderSupplyFacilityDistrict()));
    tvProvince.setText(getValueNotNull(pod.getOrderSupplyFacilityProvince()));
    User user = UserInfoMgr.getInstance().getUser();
    if (user != null) {
      tvClient.setText(user.getFacilityName());
    }
    tvRequisitionNumber.setText(getValueNotNull(pod.getRequisitionNumber()));
    if (pod.getShippedDate() != null) {
      tvIssueVoucherDate.setText(DateUtil.formatDate(pod.getShippedDate(), DateUtil.SIMPLE_DATE_FORMAT));
    }
    tvIssueVoucherNumber.setText(getValueNotNull(pod.getOrderCode()));
    tvPodNumber.setText(getValueNotNull(pod.getOrderCode()));
    if (pod.getReceivedDate() != null) {
      tvReceptionDate.setText(DateUtil.formatDate(pod.getReceivedDate(), DateUtil.DEFAULT_DATE_FORMAT));
    }

  }

  private String getValueNotNull(String value) {
    return value == null ? "" : value;
  }
}
