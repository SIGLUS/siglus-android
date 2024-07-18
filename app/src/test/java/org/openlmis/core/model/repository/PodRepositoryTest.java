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

package org.openlmis.core.model.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.Constants.Program;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class PodRepositoryTest {

  private PodRepository podRepository;

  private SyncErrorsRepository syncErrorRepository;

  @Before
  public void setUp() throws Exception {
    podRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PodRepository.class);
    syncErrorRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncErrorsRepository.class);

    // given
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(PodBuilder.generatePod());
    podRepository.batchCreatePodsWithItems(pods);
  }

  @Test
  public void shouldCorrectQueryPodById() throws Exception {
    // given
    Pod VCPod = PodBuilder.generatePod();
    VCPod.setOrderCode("VCPodOrderCode");
    VCPod.setId(2);
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(VCPod);
    podRepository.batchCreatePodsWithItems(pods);

    // then
    Assert.assertNotNull(podRepository.queryById(2));
  }

  @Test
  public void shouldCorrectListAllPods() throws Exception {
    // when
    List<Pod> allPods = podRepository.list();

    // then
    assertEquals(1, allPods.size());
  }

  @Test
  public void shouldCorrectQueryPodsByStatus() throws Exception {
    // when
    List<Pod> shippedPods = podRepository.queryByStatus(OrderStatus.SHIPPED);
    List<Pod> receivedPods = podRepository.queryByStatus(OrderStatus.RECEIVED);

    // then
    assertEquals(1, shippedPods.size());
    assertEquals(0, receivedPods.size());
  }

  @Test
  public void shouldCorrectDeleteByOrderCode() throws Exception {
    // when
    podRepository.deleteByOrderCode(FieldConstants.ORDER_CODE);

    // then
    assertEquals(0, podRepository.list().size());
  }

  @Test
  public void shouldCorrectQueryIssueVoucherOrderCodesBelongProgram() throws Exception {
    // given
    ArrayList<Pod> pods = new ArrayList<>();
    Pod remoteVCIssueVoucher = PodBuilder.generatePod();
    remoteVCIssueVoucher.setOrderCode("remoteVCIssueVoucher");
    remoteVCIssueVoucher.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());
    remoteVCIssueVoucher.setLocal(false);

    Pod VCPod = PodBuilder.generatePod();
    VCPod.setOrderCode("VCPod");
    VCPod.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());
    VCPod.setLocal(false);
    VCPod.setOrderStatus(OrderStatus.RECEIVED);
    pods.add(remoteVCIssueVoucher);
    pods.add(VCPod);
    podRepository.batchCreatePodsWithItems(pods);

    // when
    List<String> orderCodes = podRepository.querySameProgramIssueVoucherByOrderCode("VCPod");

    // then
    assertEquals("remoteVCIssueVoucher", orderCodes.get(0));
  }

  @Test
  public void testHasUnmatchedPodByProgram() throws Exception {
    // given
    SyncError syncError = new SyncError(Constants.SIGLUS_API_ORDER_NUMBER_NOT_EXIST, SyncType.POD, 2);
    syncErrorRepository.createOrUpdate(syncError);
    Pod VCPod = PodBuilder.generatePod();
    VCPod.setId(2);
    VCPod.setOrderCode("VCPod");
    VCPod.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());
    VCPod.setSynced(false);
    VCPod.setOrderStatus(OrderStatus.RECEIVED);
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(VCPod);
    podRepository.batchCreatePodsWithItems(pods);

    // then
    Assert.assertTrue(podRepository.hasUnmatchedPodByProgram(Program.VIA_PROGRAM.getCode()));
  }

  @Test
  public void shouldCorrectUpdateOrderCode() throws Exception {
    // given
    String issueVoucherOrderCode = "issueVoucherOrderCode";
    String podOrderCode = "podOrderCode";

    Pod issueVoucher = PodBuilder.generatePod();
    issueVoucher.setId(2);
    issueVoucher.setOrderCode(issueVoucherOrderCode);
    issueVoucher.setOrderStatus(OrderStatus.SHIPPED);
    issueVoucher.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());

    Pod pod = PodBuilder.generatePod();
    pod.setId(3);
    pod.setOrderCode(podOrderCode);
    pod.setOrderStatus(OrderStatus.RECEIVED);
    pod.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(issueVoucher);
    pods.add(pod);
    podRepository.batchCreatePodsWithItems(pods);

    SyncError syncError = new SyncError(Constants.SIGLUS_API_ORDER_NUMBER_NOT_EXIST, SyncType.POD, 3);
    syncErrorRepository.createOrUpdate(syncError);

    // then
    Assert.assertTrue(podRepository.hasUnmatchedPodByProgram(Program.VIA_PROGRAM.getCode()));

    // when
    podRepository.updateOrderCode(podOrderCode, issueVoucherOrderCode);

    // then
    Pod updatedPod = podRepository.queryByOrderCode(issueVoucherOrderCode);
    assertEquals(issueVoucherOrderCode, updatedPod.getOrderCode());
    assertEquals(podOrderCode, updatedPod.getOriginOrderCode());
    Assert.assertNull(podRepository.queryByOrderCode(podOrderCode));
    Assert.assertFalse(podRepository.hasUnmatchedPodByProgram(Program.VIA_PROGRAM.getCode()));
  }

  @Test
  public void shouldCorrectCheckOldData() throws Exception {
    // given
    String issueVoucherOrderCode = "issueVoucherOrderCode";
    Pod issueVoucher = PodBuilder.generatePod();
    issueVoucher.setId(2);
    issueVoucher.setOrderCode(issueVoucherOrderCode);
    issueVoucher.setOrderStatus(OrderStatus.SHIPPED);
    issueVoucher.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());
    issueVoucher.setRequisitionEndDate(DateUtil.parseString("2020-01-01", DateUtil.DB_DATE_FORMAT));
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(issueVoucher);
    podRepository.batchCreatePodsWithItems(pods);

    // then
    Assert.assertTrue(podRepository.hasOldData());
  }

  @Test
  public void shouldCorrectDeleteOldData() throws Exception {
    // given
    String issueVoucherOrderCode = "issueVoucherOrderCode";
    Pod issueVoucher = PodBuilder.generatePod();
    issueVoucher.setId(2);
    issueVoucher.setOrderCode(issueVoucherOrderCode);
    issueVoucher.setOrderStatus(OrderStatus.SHIPPED);
    issueVoucher.setRequisitionProgramCode(Program.VIA_PROGRAM.getCode());
    issueVoucher.setRequisitionEndDate(DateUtil.parseString("2020-01-01", DateUtil.DB_DATE_FORMAT));
    issueVoucher.setSynced(true);
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(issueVoucher);
    podRepository.batchCreatePodsWithItems(pods);

    // then
    assertEquals(2, podRepository.list().size());

    // when
    podRepository.deleteOldData();

    // then
    assertEquals(1, podRepository.list().size());
  }

  @Test
  public void shouldCorrectQueryUnsyncedPodsAndMarkSynced() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setSynced(false);
    pod.setOrderStatus(OrderStatus.RECEIVED);
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(pod);
    podRepository.batchCreatePodsWithItems(pods);

    // when
    List<Pod> unsyncedPods = podRepository.queryUnsyncedPods();

    // then
    assertEquals(1, unsyncedPods.size());
    assertEquals(pod.getId(), unsyncedPods.get(0).getId());

    // when
    boolean result = podRepository.markSynced(pod);

    // then
    Assert.assertTrue(result);
    Assert.assertTrue(pod.isSynced());
  }

  @Test
  public void test_queryRemotePodsByProgramCodeAndPeriod() throws Exception {
    // given
    Date periodBegin = DateUtil.getCurrentDate();
    String programCode = "VC";

    Pod regularPod = generatePod("regularPod", programCode, periodBegin, false, false);
    Pod emergencyPod = generatePod("emergencyPod", programCode, periodBegin, false, true);
    Pod localPod = generatePod("localPod", programCode, periodBegin, true, false);
    Pod otherPeriodSubmittedPod = generatePod(
        "otherPeriodSubmittedPod",
        programCode, DateUtil.dateMinusMonth(periodBegin, 5), true, false
    );

    podRepository.batchCreatePodsWithItems(
        newArrayList(regularPod, emergencyPod, localPod, otherPeriodSubmittedPod)
    );
    // when
    List<Pod> actualPods = podRepository.queryRegularRemotePodsByProgramCodeAndPeriod(
        programCode, DateUtil.getFirstDayForCurrentMonthByDate(periodBegin)
    );
    // then
    assertEquals(1, actualPods.size());
    Pod pod = actualPods.get(0);
    assertEquals(programCode, pod.getRequisitionProgramCode());
    assertEquals(
        DateUtil.formatDate(periodBegin, DateUtil.DB_DATE_FORMAT),
        DateUtil.formatDate(pod.getRequisitionStartDate(), DateUtil.DB_DATE_FORMAT)
    );
    assertFalse(pod.isLocal());
  }

  @NonNull
  private Pod generatePod(
      String orderCode, String programCode, Date periodBegin, boolean isLocal,
      boolean requisitionIsEmergency
  ) throws Exception {
    Pod pod = PodBuilder.generatePod();

    pod.setOrderCode(orderCode);
    pod.setLocal(isLocal);
    pod.setRequisitionProgramCode(programCode);
    pod.setRequisitionStartDate(periodBegin);
    pod.setRequisitionIsEmergency(requisitionIsEmergency);

    return pod;
  }
}