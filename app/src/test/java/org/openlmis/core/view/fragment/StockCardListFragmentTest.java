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

package org.openlmis.core.view.fragment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import androidx.activity.result.ActivityResult;
import androidx.recyclerview.widget.RecyclerView;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.StockCardListActivity;
import org.openlmis.core.view.adapter.StockCardListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ProductsUpdateBanner;
import org.robolectric.Robolectric;

@RunWith(LMISTestRunner.class)
public class StockCardListFragmentTest {

  private StockCardListFragment fragment;
  private List<InventoryViewModel> inventoryViewModels;
  private ProductsUpdateBanner productUpdateBanner;
  private SharedPreferenceMgr sharedPreferenceMgr;

  @Before
  public void setUp() {
    fragment = buildFragment();
    productUpdateBanner = mock(ProductsUpdateBanner.class);
    sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    fragment.sharedPreferenceMgr = sharedPreferenceMgr;

    inventoryViewModels = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      StockCard stockCard = new StockCard();
      stockCard.setStockOnHand(10 - i);
      Product product = new Product();
      product.setPrimaryName((char) ('A' + i) + " Product");

      stockCard.setProduct(product);
      inventoryViewModels.add(new InventoryViewModel(stockCard));
    }
  }

  private StockCardListFragment buildFragment() {
    StockCardListActivity stockCardListActivity = Robolectric
        .buildActivity(StockCardListActivity.class).create().get();

    fragment = new StockCardListFragment();

    stockCardListActivity.getSupportFragmentManager().beginTransaction().add(fragment, null).commit();

    fragment.presenter = mock(StockCardPresenter.class);
    return fragment;
  }

  @Test
  public void shouldSortListByProductName() {
    when(fragment.presenter.getInventoryViewModels()).thenReturn(this.inventoryViewModels);
    List<InventoryViewModel> inventoryViewModels = fragment.presenter.getInventoryViewModels();
    StockCardListAdapter adapter = new StockCardListAdapter(new ArrayList<InventoryViewModel>(),
        null);
    adapter.refreshList(inventoryViewModels);
    adapter.sortByName(true);

    List<InventoryViewModel> sortedList = adapter.getFilteredList();
    assertThat(sortedList.get(0).getProduct().getPrimaryName(), is("A Product"));
    assertThat(sortedList.get(1).getProduct().getPrimaryName(), is("B Product"));
    assertThat(sortedList.get(2).getProduct().getPrimaryName(), is("C Product"));
  }

  @Test
  public void shouldSortListBySOH() {
    when(fragment.presenter.getInventoryViewModels()).thenReturn(inventoryViewModels);
    StockCardListAdapter adapter = new StockCardListAdapter(new ArrayList<InventoryViewModel>(),
        null);
    adapter.refreshList(inventoryViewModels);
    adapter.sortBySOH(true);

    List<InventoryViewModel> sortedList = adapter.getFilteredList();
    assertThat(sortedList.get(0).getStockOnHand(), is(1L));
    assertThat(sortedList.get(1).getStockOnHand(), is(2L));
    assertThat(sortedList.get(2).getStockOnHand(), is(3L));
  }

  @Test
  public void shouldRefreshStockcard() {
    // given
    when(sharedPreferenceMgr.isNeedShowProductsUpdateBanner()).thenReturn(true);
    final long[] stockcardIds = {0};
    final Intent data = new Intent();
    data.putExtra(Constants.PARAM_STOCK_CARD_ID_ARRAY, stockcardIds);

    ActivityResult mockResult = new ActivityResult(Activity.RESULT_OK, data);

    // when
    fragment.getStockListCallback().onActivityResult(mockResult);

    // then
    verify(fragment.presenter).refreshStockCardsObservable(stockcardIds);
  }

  @Test
  public void shouldRefreshAndShowBannerWhenNeedShowBanner() {
    // given
    fragment.productsUpdateBanner = productUpdateBanner;

    // when
    fragment.refreshBannerText();

    // then
    verify(productUpdateBanner,times(1)).refreshBannerText();
  }

  @Test
  public void shouldReturnFalseWhenIsCalled() {
    assertFalse(fragment.isFastScrollEnabled());
  }

  @Test
  public void shouldHideFastScrollerWhenSetUpFastScrollerAndDataIsEmpty() {
    // Given
    int gone = View.GONE;
    ArrayList<InventoryViewModel> data = newArrayList();

    RecyclerViewFastScroller mockedFastScroller = mock(RecyclerViewFastScroller.class);
    doNothing().when(mockedFastScroller).setVisibility(gone);
    fragment.fastScroller = mockedFastScroller;
    // when
    fragment.setUpFastScroller(data);
    // then
    verify(mockedFastScroller).setVisibility(gone);
  }

  @Test
  public void shouldInitializeFastScrollerWhenSetUpFastScrollerAndDataIsNotEmpty() {
    // Given
    int visibility = View.VISIBLE;

    InventoryViewModel mockedInventoryViewModel = mock(InventoryViewModel.class);
    when(mockedInventoryViewModel.getProductName()).thenReturn("A");
    ArrayList<InventoryViewModel> data = newArrayList(mockedInventoryViewModel);

    RecyclerViewFastScroller mockedFastScroller = mock(RecyclerViewFastScroller.class);
    doNothing().when(mockedFastScroller).setVisibility(visibility);

    RecyclerView mockedRecyclerView = mock(RecyclerView.class);
    fragment.stockCardRecycleView = mockedRecyclerView;
    doNothing().when(mockedFastScroller).setRecyclerView(mockedRecyclerView);

    doNothing().when(mockedFastScroller).setUpAlphabet(anyListOf(AlphabetItem.class));

    fragment.fastScroller = mockedFastScroller;
    // when
    fragment.setUpFastScroller(data);
    // then
    verify(mockedFastScroller).setRecyclerView(mockedRecyclerView);
    verify(mockedFastScroller).setUpAlphabet(anyListOf(AlphabetItem.class));
    verify(mockedFastScroller).setVisibility(visibility);
  }

  @Test
  public void shouldReturnMatchedLayoutIdWhenGetStockCardListLayoutIdIsCalled() {
    assertEquals(R.layout.fragment_stockcard_list, fragment.getStockCardListLayoutId());
  }
}
