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

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.view.adapter.IssueVoucherReportAdapter.IssueVoucherReportViewHolder;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;

@RunWith(LMISTestRunner.class)
public class IssueVoucherReportAdapterTest {

  private IssueVoucherReportAdapter adapter;

  @Before
  public void setup() {
    adapter = new IssueVoucherReportAdapter();
  }

  @Test
  public void testConvertEditType() throws Exception  {
    // given
    IssueVoucherReportViewHolder holder = adapter.new IssueVoucherReportViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_issue_voucher_report_product, null));
    Pod VCPod = PodBuilder.generatePod();
    IssueVoucherReportProductViewModel model = new IssueVoucherReportProductViewModel(VCPod.getPodProductItemsWrapper()
        .get(0), OrderStatus.SHIPPED, true, true);

    // when
    adapter.convert(holder, model);

    // then
    assertEquals(View.VISIBLE, holder.getView(R.id.tv_product_unit).getVisibility());
    assertEquals(View.VISIBLE, holder.getView(R.id.tv_partial_fulfilled).getVisibility());
  }

}
