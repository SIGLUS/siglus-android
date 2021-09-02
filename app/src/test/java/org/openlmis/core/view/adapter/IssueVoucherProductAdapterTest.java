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
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter.IssueVoucherProductViewHolder;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;

@RunWith(LMISTestRunner.class)
public class IssueVoucherProductAdapterTest {

  private IssueVoucherProductAdapter adapter;
  private IssueVoucherReportProductViewModel productViewModel;
  private IssueVoucherProductViewHolder holder;

  @Before
  public void setup() {
    adapter = new IssueVoucherProductAdapter();
    holder = adapter.new IssueVoucherProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_issue_voucher_report_product_info, null));

    PodProductItem podProductItem = PodProductItem.builder()
        .pod(null)
        .product(ProductBuilder.buildAdultProduct())
        .orderedQuantity(10L)
        .partialFulfilledQuantity(5L)
        .build();
    productViewModel = new IssueVoucherReportProductViewModel(podProductItem, OrderStatus.SHIPPED, true);
  }

  @Test
  public void testCorrectUIFocusableForLocalDraft()  {
    // given
    productViewModel.setLocal(true);

    // when
    adapter.convert(holder, productViewModel);

    // then
    TextView name = holder.getView(R.id.products_name);
    assertEquals("Primary product name", name.getText().toString());
    assertEquals(View.VISIBLE, holder.getView(R.id.products_list_item).getVisibility());
  }

}
