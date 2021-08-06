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

import static org.openlmis.core.view.viewmodel.BulkIssueProductViewModel.TYPE_EDIT;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;

@RunWith(LMISTestRunner.class)
public class BulkIssueAdapterTest {


  @Test
  public void testConvertEditType() {
    // given
    BulkIssueAdapter adapter = new BulkIssueAdapter();
    Context context = LMISTestApp.getContext();
    context.setTheme(R.style.AppTheme);
    BaseViewHolder holder = new BaseViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_bulk_issue_edit, null));
    BulkIssueProductViewModel mockProductViewModel = Mockito.mock(BulkIssueProductViewModel.class);
    Mockito.when(mockProductViewModel.getRequested()).thenReturn(1L);
    Mockito.when(mockProductViewModel.getProduct()).thenReturn(ProductBuilder.buildAdultProduct());
    Mockito.when(mockProductViewModel.getItemType()).thenReturn(TYPE_EDIT);

    // when
    adapter.convert(holder, mockProductViewModel);

    // when
    TextView tvProductTitle = holder.getView(R.id.tv_product_title);
    Assert.assertEquals("Primary product name [productCode]", tvProductTitle.getText().toString());
    EditText etRequested = holder.getView(R.id.et_requested);
    Assert.assertEquals("1", etRequested.getText().toString());
  }
}