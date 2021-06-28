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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.holder.StockCardViewHolder.OnItemViewClickListener;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModelBuilder;

@RunWith(LMISTestRunner.class)
public class StockCardListAdapterTest {

  private RecyclerView recyclerView;

  @Before
  public void setUp() throws Exception {
    recyclerView = new RecyclerView(LMISTestApp.getContext());
    recyclerView.setLayoutManager(new LinearLayoutManager(LMISTestApp.getContext()));
  }

  @Test
  public void testOnCreateViewHolder() {
    // given
    final ArrayList<InventoryViewModel> viewModels = new ArrayList<>();
    final OnItemViewClickListener mockListener = Mockito.mock(OnItemViewClickListener.class);
    final StockCardListAdapter adapter = new StockCardListAdapter(viewModels, mockListener);
    recyclerView.setAdapter(adapter);

    // when
    final StockCardViewHolder stockCardViewHolder = adapter.onCreateViewHolder(recyclerView, 0);

    // then
    Assertions.assertThat(stockCardViewHolder).isNotNull();
  }

  @Test
  public void testOnBindViewHolder() {
    // given
    final StockCardViewHolder mockHolder = Mockito.mock(StockCardViewHolder.class);
    final ArrayList<InventoryViewModel> viewModels = new ArrayList<>();
    final InventoryViewModel viewModel1 = generateInventoryViewModel("", 4);
    final InventoryViewModel viewModel2 = generateInventoryViewModel("", 1);
    final InventoryViewModel viewModel3 = generateInventoryViewModel("", 3);
    final InventoryViewModel viewModel4 = generateInventoryViewModel("", 2);
    viewModels.add(viewModel1);
    viewModels.add(viewModel2);
    viewModels.add(viewModel3);
    viewModels.add(viewModel4);
    final StockCardListAdapter adapter = new StockCardListAdapter(viewModels,
        Mockito.mock(OnItemViewClickListener.class));
    recyclerView.setAdapter(adapter);
    adapter.filter("");

    // when
    adapter.onBindViewHolder(mockHolder, 0);

    // then
    Mockito.verify(mockHolder, Mockito.times(1)).populate(viewModel1, "");
  }

  @Test
  public void testSortBySOH() {
    // given
    final ArrayList<InventoryViewModel> viewModels = new ArrayList<>();
    final InventoryViewModel viewModel1 = generateInventoryViewModel("", 4);
    final InventoryViewModel viewModel2 = generateInventoryViewModel("", 1);
    final InventoryViewModel viewModel3 = generateInventoryViewModel("", 3);
    final InventoryViewModel viewModel4 = generateInventoryViewModel("", 2);
    viewModels.add(viewModel1);
    viewModels.add(viewModel2);
    viewModels.add(viewModel3);
    viewModels.add(viewModel4);
    final StockCardListAdapter adapter = new StockCardListAdapter(viewModels,
        Mockito.mock(OnItemViewClickListener.class));
    recyclerView.setAdapter(adapter);

    // when
    adapter.sortBySOH(true);

    // then
    assertEquals(1, viewModels.get(0).getStockOnHand());
    assertEquals(2, viewModels.get(1).getStockOnHand());
    assertEquals(3, viewModels.get(2).getStockOnHand());
    assertEquals(4, viewModels.get(3).getStockOnHand());

    // when
    adapter.sortBySOH(false);

    // then
    assertEquals(4, viewModels.get(0).getStockOnHand());
    assertEquals(3, viewModels.get(1).getStockOnHand());
    assertEquals(2, viewModels.get(2).getStockOnHand());
    assertEquals(1, viewModels.get(3).getStockOnHand());
  }

  @Test
  public void testSortByName() {
    // given
    final ArrayList<InventoryViewModel> viewModels = new ArrayList<>();
    final InventoryViewModel viewModel1 = generateInventoryViewModel("b", 1);
    final InventoryViewModel viewModel2 = generateInventoryViewModel("c", 1);
    final InventoryViewModel viewModel3 = generateInventoryViewModel("d", 1);
    final InventoryViewModel viewModel4 = generateInventoryViewModel("q", 1);
    viewModels.add(viewModel1);
    viewModels.add(viewModel2);
    viewModels.add(viewModel3);
    viewModels.add(viewModel4);
    final StockCardListAdapter adapter = new StockCardListAdapter(viewModels,
        Mockito.mock(OnItemViewClickListener.class));
    recyclerView.setAdapter(adapter);

    // when
    adapter.sortByName(true);

    // then
    assertEquals("b", viewModels.get(0).getProductName());
    assertEquals("c", viewModels.get(1).getProductName());
    assertEquals("d", viewModels.get(2).getProductName());
    assertEquals("q", viewModels.get(3).getProductName());

    // when
    adapter.sortByName(false);

    // then
    assertEquals("q", viewModels.get(0).getProductName());
    assertEquals("d", viewModels.get(1).getProductName());
    assertEquals("c", viewModels.get(2).getProductName());
    assertEquals("b", viewModels.get(3).getProductName());
  }

  private InventoryViewModel generateInventoryViewModel(String productName, long stockOnHand) {
    final Product product = new Product();
    product.setPrimaryName(productName);
    return new InventoryViewModelBuilder(product).setSOH(stockOnHand).build();
  }
}