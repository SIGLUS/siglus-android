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

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.StockCardListActivity;
import org.openlmis.core.view.adapter.StockCardListAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockCardListFragmentTest {

    private StockCardListFragment fragment;
    private List<StockCardViewModel> stockCardViewModels;

    @Before
    public void setUp() {
        fragment = buildFragment(false);

        stockCardViewModels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockCard stockCard = new StockCard();
            stockCard.setStockOnHand(10 - i);
            Product product = new Product();
            product.setPrimaryName((char) ('A' + i) + " Product");

            stockCard.setProduct(product);
            stockCardViewModels.add(new StockCardViewModel(stockCard));
        }
    }

    private StockCardListFragment buildFragment(boolean isKitStockList) {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_IS_KIT_STOCK_CARD, isKitStockList);
        StockCardListActivity stockCardListActivity = Robolectric.buildActivity(StockCardListActivity.class).withIntent(intent).create().get();

        fragment = new StockCardListFragment();

        stockCardListActivity.getFragmentManager().beginTransaction().add(fragment, null).commit();

        fragment.presenter = mock(StockCardPresenter.class);
        fragment.mAdapter = mock(StockCardListAdapter.class);
        return fragment;
    }

    @Test
    public void shouldSortListWhenSelectSortSpinner() {
        when(fragment.presenter.getStockCardViewModels()).thenReturn(stockCardViewModels);
        fragment.sortSpinner.setSelection(0);
        verify(fragment.mAdapter).sortByName(true);

        fragment.sortSpinner.setSelection(1);
        verify(fragment.mAdapter).sortByName(false);

        fragment.sortSpinner.setSelection(2);
        verify(fragment.mAdapter).sortByName(false);

        fragment.sortSpinner.setSelection(3);
        verify(fragment.mAdapter).sortBySOH(true);
    }

    @Test
    public void shouldSortListByProductName() {
        when(fragment.presenter.getStockCardViewModels()).thenReturn(stockCardViewModels);
        List<StockCardViewModel> stockCardViewModels = fragment.presenter.getStockCardViewModels();
        StockCardListAdapter adapter = new StockCardListAdapter(stockCardViewModels, null);
        adapter.sortByName(true);

        List<StockCardViewModel> sortedList = adapter.getCurrentList();
        assertThat(sortedList.get(0).getProduct().getPrimaryName(), is("A Product"));
        assertThat(sortedList.get(1).getProduct().getPrimaryName(), is("B Product"));
        assertThat(sortedList.get(2).getProduct().getPrimaryName(), is("C Product"));
    }

    @Test
    public void shouldSortListBySOH() {
        when(fragment.presenter.getStockCardViewModels()).thenReturn(stockCardViewModels);
        StockCardListAdapter adapter = new StockCardListAdapter(stockCardViewModels, null);
        adapter.sortBySOH(true);

        List<StockCardViewModel> sortedList = adapter.getCurrentList();
        assertThat(sortedList.get(0).getStockOnHand(), is(1L));
        assertThat(sortedList.get(1).getStockOnHand(), is(2L));
        assertThat(sortedList.get(2).getStockOnHand(), is(3L));
    }

    @Test
    public void shouldShowKitOverviewTitle() throws Exception {
        fragment = buildFragment(true);

        assertThat(fragment.getActivity().getTitle().toString(), is("Kit Overview"));
    }
}
