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

package org.openlmis.core.view.viewmodel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

@RunWith(LMISTestRunner.class)
public class IssueVoucherListViewModelTest {

  private Pod pod;

  @Before
  public void setup() throws Exception {
    pod = PodBuilder.generatePod();
  }

  @Test
  public void shouldCorrectGetOrderNumberWhenNotEmergency() {
    // given
    pod.setRequisitionIsEmergency(false);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals(pod.getOrderCode(), viewModel.getOrderNumber());
  }

  @Test
  public void shouldCorrectGetOrderNumberWhenEmergency() {
    // given
    pod.setRequisitionIsEmergency(true);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals("EMERGENCY " + pod.getOrderCode(), viewModel.getOrderNumber());
  }

  @Test
  public void shouldCorrectGetOrderSupplyFacilityName() {
    // given
    pod.setOrderSupplyFacilityName("Name");
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals("Name", viewModel.getOrderSupplyFacilityName());
  }

  @Test
  public void shouldCorrectGetReportingPeriod() {
    // given
    String startDate = "01/01/2021";
    String endDate = "02/02/2021";
    pod.setRequisitionActualStartDate(DateUtil.parseString(startDate, DateUtil.SIMPLE_DATE_FORMAT));
    pod.setRequisitionActualEndDate(DateUtil.parseString(endDate, DateUtil.SIMPLE_DATE_FORMAT));
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals(startDate + " - " + endDate, viewModel.getReportingPeriod());
  }

  @Test
  public void shouldGetNullWhenNoPeriod() {
    // given
    pod.setRequisitionActualStartDate(null);
    pod.setRequisitionActualEndDate(null);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals("", viewModel.getReportingPeriod());
  }

  @Test
  public void shouldCorrectGetShippedDate() {
    // given
    String shippedDate = "01/01/2021";
    pod.setShippedDate(DateUtil.parseString(shippedDate, DateUtil.SIMPLE_DATE_FORMAT));
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals(shippedDate, viewModel.getShippedDate());
  }

  @Test
  public void shouldGetNullWhenNoShippedDate() {
    // given
    pod.setShippedDate(null);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertEquals("", viewModel.getShippedDate());
  }

  @Test
  public void shouldCorrectGetIsIssueVoucher() {
    // given
    pod.setOrderStatus(OrderStatus.SHIPPED);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertTrue(viewModel.isIssueVoucher());
  }

  @Test
  public void shouldCorrectGetShouldShowOperationIcon() {
    // given
    pod.setOrderStatus(OrderStatus.SHIPPED);
    pod.setLocal(true);
    IssueVoucherListViewModel viewModel = IssueVoucherListViewModel.builder().pod(pod).build();

    // then
    Assert.assertTrue(viewModel.shouldShowOperationIcon());
  }

  @Test
  public void shouldCorrectGetErrorMsg() throws Exception {
    // given
    Pod unsyncPod = PodBuilder.generatePod();
    unsyncPod.setSynced(false);
    IssueVoucherListViewModel unsyncViewModel = IssueVoucherListViewModel.builder().pod(unsyncPod).build();

    Pod syncErrorPod = PodBuilder.generatePod();
    SyncError syncError = new SyncError("test message", SyncType.POD, syncErrorPod.getId());
    IssueVoucherListViewModel syncErrorViewModel = IssueVoucherListViewModel.builder()
        .pod(syncErrorPod)
        .syncError(syncError)
        .build();

    Pod orderNotMatchPod = PodBuilder.generatePod();
    SyncError orderNotMatchError = new SyncError(Constants.SIGLUS_API_ORDER_NUMBER_NOT_EXIST, SyncType.POD,
        syncErrorPod.getId());
    IssueVoucherListViewModel orderNotMatchViewModel = IssueVoucherListViewModel.builder()
        .pod(orderNotMatchPod)
        .syncError(orderNotMatchError)
        .build();

    // then
    Assert.assertEquals(LMISApp.getContext().getResources().getString(R.string.error_pod_not_sync),
        unsyncViewModel.getErrorMsg());
    Assert.assertEquals("test message", syncErrorViewModel.getErrorMsg());
    Assert.assertEquals(Constants.SIGLUS_API_ORDER_NUMBER_NOT_EXIST,
        orderNotMatchViewModel.getErrorMsg());
  }
}