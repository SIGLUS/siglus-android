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
import org.openlmis.core.model.builder.PodBuilder;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class PodRepositoryTest {

  private PodRepository podRepository;

  @Before
  public void setUp() throws Exception {
    podRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PodRepository.class);

    // given
    ArrayList<Pod> pods = new ArrayList<>();
    pods.add(PodBuilder.generatePod());
    podRepository.batchCreatePodsWithItems(pods);
  }

  @Test
  public void shouldCorrectListAllPods() throws Exception {
    // when
    List<Pod> allPods = podRepository.listAllPods();

    // then
    Assert.assertEquals(1, allPods.size());
  }

  @Test
  public void shouldCorrectQueryPodsByStatus() throws Exception {
    // when
    List<Pod> shippedPods = podRepository.queryPodsByStatus(OrderStatus.SHIPPED);
    List<Pod> receivedPods = podRepository.queryPodsByStatus(OrderStatus.RECEIVED);

    // then
    Assert.assertEquals(1, shippedPods.size());
    Assert.assertEquals(0, receivedPods.size());
  }

  @Test
  public void shouldCorrectDeleteByOrderCode() throws Exception {
    // when
    podRepository.deleteByOrderCode(FieldConstants.ORDER_CODE);

    // then
    Assert.assertEquals(0, podRepository.listAllPods().size());
  }
}