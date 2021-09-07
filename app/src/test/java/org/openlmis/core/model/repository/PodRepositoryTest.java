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

import java.util.ArrayList;
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
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.utils.Constants.Program;
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
    Assert.assertEquals(1, allPods.size());
  }

  @Test
  public void shouldCorrectQueryPodsByStatus() throws Exception {
    // when
    List<Pod> shippedPods = podRepository.queryByStatus(OrderStatus.SHIPPED);
    List<Pod> receivedPods = podRepository.queryByStatus(OrderStatus.RECEIVED);

    // then
    Assert.assertEquals(1, shippedPods.size());
    Assert.assertEquals(0, receivedPods.size());
  }

  @Test
  public void shouldCorrectDeleteByOrderCode() throws Exception {
    // when
    podRepository.deleteByOrderCode(FieldConstants.ORDER_CODE);

    // then
    Assert.assertEquals(0, podRepository.list().size());
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
    Assert.assertEquals("remoteVCIssueVoucher", orderCodes.get(0));
  }

  @Test
  public void testHasUnmatchedPodByProgram() throws Exception {
    // given
    SyncError syncError = new SyncError(SyncErrorsMap.ERROR_POD_ORDER_DOSE_NOT_EXIST, SyncType.POD, 2);
    syncErrorRepository.save(syncError);
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

    SyncError syncError = new SyncError(SyncErrorsMap.ERROR_POD_ORDER_DOSE_NOT_EXIST, SyncType.POD, 3);
    syncErrorRepository.save(syncError);

    // then
    Assert.assertTrue(podRepository.hasUnmatchedPodByProgram(Program.VIA_PROGRAM.getCode()));

    // when
    podRepository.updateOrderCode(podOrderCode, issueVoucherOrderCode);

    // then
    Pod updatedPod = podRepository.queryByOrderCode(issueVoucherOrderCode);
    Assert.assertEquals(issueVoucherOrderCode, updatedPod.getOrderCode());
    Assert.assertEquals(podOrderCode, updatedPod.getOriginOrderCode());
    Assert.assertNull(podRepository.queryByOrderCode(podOrderCode));
    Assert.assertFalse(podRepository.hasUnmatchedPodByProgram(Program.VIA_PROGRAM.getCode()));
  }
}