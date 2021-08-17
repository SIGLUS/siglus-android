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

import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.BulkIssueAdapter.BulkIssueProductViewHolder;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;

@RunWith(LMISTestRunner.class)
public class BulkIssueAdapterTest {

  private BulkIssueAdapter adapter;
  private BulkIssueProductViewModel mockProductViewModel;

  @Before
  public void setup() {
    adapter = new BulkIssueAdapter();
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    mockProductViewModel = Mockito.mock(BulkIssueProductViewModel.class);
    StockCard mockStockCard = Mockito.mock(StockCard.class);
    Mockito.when(mockStockCard.getProduct()).thenReturn(ProductBuilder.buildAdultProduct());
    Mockito.when(mockProductViewModel.getRequested()).thenReturn(1L);
    Mockito.when(mockProductViewModel.getStockCard()).thenReturn(mockStockCard);
    Mockito.when(mockProductViewModel.getItemType()).thenReturn(TYPE_EDIT);
  }

  @Test
  public void testConvert() {
    // given
    BulkIssueProductViewHolder holder = adapter.new BulkIssueProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_edit, null));

    // when
    adapter.convert(holder, mockProductViewModel);

    // when
    TextView tvProductTitle = holder.getView(R.id.tv_product_title);
    Assert.assertEquals("Primary product name [productCode]", tvProductTitle.getText().toString());
    EditText etRequested = holder.getView(R.id.et_requested);
    Assert.assertEquals("1", etRequested.getText().toString());
  }

  @Test
  public void shouldCorrectValidate() {
    // given
    BulkIssueProductViewModel mockProductViewModel = Mockito.mock(BulkIssueProductViewModel.class);
    Mockito.when(mockProductViewModel.validate()).thenReturn(true);
    adapter.setList(Collections.singletonList(mockProductViewModel));

    // then
    Assert.assertEquals(-1, adapter.validateAll());
  }

  @Test
  public void shouldReturnFailedPositionWhenValidateAll() {
    // given
    ArrayList<BulkIssueProductViewModel> viewModels = new ArrayList<>();
    BulkIssueProductViewModel viewModel1 = Mockito.mock(BulkIssueProductViewModel.class);
    BulkIssueProductViewModel viewModel2 = Mockito.mock(BulkIssueProductViewModel.class);
    Mockito.when(viewModel1.validate()).thenReturn(true);
    Mockito.when(viewModel2.validate()).thenReturn(false);
    viewModels.add(viewModel1);
    viewModels.add(viewModel2);
    adapter.setList(viewModels);

    // then
    Assert.assertEquals(1, adapter.validateAll());
  }

  @Test
  public void shouldCorrectRemoveItem() {
    // given
    BulkIssueAdapter mockAdapter = Mockito.mock(BulkIssueAdapter.class);
    BulkIssueProductViewHolder holder = mockAdapter.new BulkIssueProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_edit, null));
    holder.populate(mockProductViewModel);
    RobolectricUtils.resetNextClickTime();

    // when
    holder.getView(R.id.rl_trashcan).performClick();

    // then
    Mockito.verify(mockAdapter, Mockito.times(1)).removeAt(holder.getLayoutPosition());
  }

  @Test
  public void shouldBackToEditStatusWhenEditClicked() {
    // given
    BulkIssueAdapter mockAdapter = Mockito.mock(BulkIssueAdapter.class);
    BulkIssueProductViewHolder holder = mockAdapter.new BulkIssueProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_done, null));
    Mockito.when(mockProductViewModel.isDone()).thenReturn(true);
    holder.populate(mockProductViewModel);
    RobolectricUtils.resetNextClickTime();

    // when
    holder.getView(R.id.tv_edit).performClick();

    // then
    Mockito.verify(mockProductViewModel, Mockito.times(1)).setDone(false);
    Mockito.verify(mockAdapter, Mockito.times(1)).notifyItemChanged(holder.getLayoutPosition());
  }

  @Test
  public void shouldUpdateUiAfterVerifyClicked() {
    // given
    BulkIssueAdapter mockAdapter = Mockito.mock(BulkIssueAdapter.class);
    BulkIssueProductViewHolder holder = mockAdapter.new BulkIssueProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_edit, null));
    Mockito.when(mockProductViewModel.validate()).thenReturn(true);
    holder.populate(mockProductViewModel);
    RobolectricUtils.resetNextClickTime();

    // when
    holder.getView(R.id.tv_verified).performClick();

    // then
    Mockito.verify(mockAdapter, Mockito.times(1)).notifyItemChanged(holder.getLayoutPosition());
  }

  @Test
  public void shouldUpdateBannerResAfterAmountChange() {
    // given
    BulkIssueAdapter mockAdapter = Mockito.mock(BulkIssueAdapter.class);
    BulkIssueProductViewHolder holder = mockAdapter.new BulkIssueProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_edit, null));
    holder.populate(mockProductViewModel);

    // when
    holder.onAmountChange("");

    // then
    Mockito.verify(mockProductViewModel, Mockito.times(1)).updateBannerRes();
  }
}