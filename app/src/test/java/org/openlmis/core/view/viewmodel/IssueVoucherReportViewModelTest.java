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

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.builder.PodBuilder;

@RunWith(LMISTestRunner.class)
public class IssueVoucherReportViewModelTest {

  private Pod pod;

  @Before
  public void setup() throws Exception {
    pod = PodBuilder.generatePod();
  }

  @Test
  public void shouldGetCorrectStatus() {
    // when
    IssueVoucherReportViewModel issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);

    // then
    assertEquals(OrderStatus.SHIPPED, issueVoucherReportViewModel.getPodStatus());
  }

  @Test
  public void shouldGetCorrectProductItem() {
    // when
    IssueVoucherReportViewModel issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);

    // then
    List<IssueVoucherReportProductViewModel> productViewModels = issueVoucherReportViewModel.getProductViewModels();
    assertEquals(1, productViewModels.size());
    IssueVoucherReportProductViewModel productViewModel = productViewModels.get(0);
    assertEquals("productCode",productViewModel.getProduct().getCode());
    assertEquals("10",productViewModel.getOrderedQuantity());
    assertEquals("5",productViewModel.getPartialFulfilledQuantity());
  }

  @Test
  public void shouldGetCorrectLotItem() {
    // when
    IssueVoucherReportViewModel issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);

    // then
    List<IssueVoucherReportProductViewModel> productViewModels = issueVoucherReportViewModel.getProductViewModels();
    assertEquals(1, productViewModels.size());
    IssueVoucherReportLotViewModel lotViewModel = productViewModels.get(0).getLotViewModelList().get(0);
    assertEquals(FieldConstants.LOT_NUMBER, lotViewModel.getLot().getLotNumber());
    assertEquals(Long.valueOf(10),lotViewModel.getShippedQuantity());
  }

}
