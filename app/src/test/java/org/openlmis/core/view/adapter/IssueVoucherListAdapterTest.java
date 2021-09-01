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
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.IssueVoucherListAdapter.IssueVoucherViewHolder;
import org.openlmis.core.view.listener.OrderOperationListener;
import org.openlmis.core.view.viewmodel.IssueVoucherListViewModel;

@RunWith(LMISTestRunner.class)
public class IssueVoucherListAdapterTest {

  private IssueVoucherListAdapter adapter;

  private Pod pod;
  private IssueVoucherViewHolder holder;

  @Before
  public void setup() throws Exception {
    adapter = new IssueVoucherListAdapter();
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    pod = PodBuilder.generatePod();
    // given
    holder = adapter.new IssueVoucherViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_issue_voucher_list, null));
  }

  @Test
  public void shouldCorrectConvert() {
    // given
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).programName("VC").build();

    // when
    adapter.convert(holder, viewModel);

    // then
    Assert.assertEquals(viewModel.getOrderNumber(), ((TextView) holder.getView(R.id.tv_order_number)).getText());
    Assert.assertEquals(viewModel.getProgramName(), ((TextView) holder.getView(R.id.tv_program)).getText());
    Assert.assertEquals(viewModel.getOrderSupplyFacilityName(),
        StringUtils.trimToNull(((TextView) holder.getView(R.id.tv_supplying_depot)).getText().toString()));
    Assert.assertEquals(viewModel.getReportingPeriod(),
        ((TextView) holder.getView(R.id.tv_reporting_period)).getText().toString());
    Assert.assertEquals(viewModel.getShippedDate(),
        ((TextView) holder.getView(R.id.tv_shipping_date)).getText().toString());
    Assert.assertEquals(viewModel.getErrorMsg(),
        StringUtils.trimToNull(((TextView) holder.getView(R.id.tv_error_tips)).getText().toString()));
  }

  @Test
  public void shouldShowOperation() {
    // given
    pod.setOrderStatus(OrderStatus.SHIPPED);
    pod.setLocal(true);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).programName("VC").build();

    // when
    holder.populate(viewModel);

    // then
    Assert.assertEquals(View.VISIBLE, holder.getView(R.id.rl_operation).getVisibility());
    Assert.assertEquals(View.GONE, holder.getView(R.id.iv_status).getVisibility());
  }

  @Test
  public void shouldCorrectCallback() {
    // given
    pod.setOrderStatus(OrderStatus.SHIPPED);
    pod.setLocal(true);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).programName("VC").build();
    holder.populate(viewModel);
    OrderOperationListener mockListener = Mockito.mock(OrderOperationListener.class);
    adapter.setListener(mockListener);
    RobolectricUtils.resetNextClickTime();

    // when
    holder.getView(R.id.rl_operation).performClick();

    // then
    Mockito.verify(mockListener, Mockito.times(1)).orderDeleteOrEditOperation(OrderStatus.SHIPPED, pod.getOrderCode());
  }

  @Test
  public void shouldDismissOperation() {
    // given
    pod.setOrderStatus(OrderStatus.SHIPPED);
    pod.setLocal(false);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).programName("VC").build();

    // when
    holder.populate(viewModel);

    // then
    Assert.assertEquals(View.GONE, holder.getView(R.id.rl_operation).getVisibility());
    Assert.assertEquals(View.GONE, holder.getView(R.id.iv_status).getVisibility());
  }
}