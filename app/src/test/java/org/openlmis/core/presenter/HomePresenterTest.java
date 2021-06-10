/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */

package org.openlmis.core.presenter;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enums.StockOnHandStatus;
import org.openlmis.core.model.repository.StockRepository;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.eq;

@RunWith(LMISTestRunner.class)
public class HomePresenterTest {

    private HomePresenter.HomeView view;
    StockRepository mockStockRepository;
    HomePresenter presenter;

    @Before
    public void setUp() throws Exception {
        // given
        view = Mockito.mock(HomePresenter.HomeView.class);
        mockStockRepository = Mockito.mock(StockRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, binder -> binder.bind(StockRepository.class).toInstance(mockStockRepository));
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(HomePresenter.class);
        presenter.attachView(view);

        final HashMap<String, Integer> stockOnHandStatusMap = new HashMap<>();
        stockOnHandStatusMap.put(StockOnHandStatus.REGULAR_STOCK.name(), 1);
        stockOnHandStatusMap.put(StockOnHandStatus.LOW_STOCK.name(), 1);
        stockOnHandStatusMap.put(StockOnHandStatus.STOCK_OUT.name(), 1);
        stockOnHandStatusMap.put(StockOnHandStatus.OVER_STOCK.name(), 1);
        Mockito.when(mockStockRepository.queryStockCountGroupByStockOnHandStatus()).thenReturn(stockOnHandStatusMap);
    }

    @Test
    public void getDashboardDataTest() {
        // given
        final TestSubscriber<Map<String, Integer>> testSubscriber = new TestSubscriber<>(presenter.queryStockCountObserver);
        presenter.queryStockCountObserver = testSubscriber;

        // when
        presenter.getDashboardData();
        testSubscriber.awaitTerminalEvent();

        // then
        testSubscriber.assertNoErrors();
        Mockito.verify(view, Mockito.times(1)).updateDashboard(eq(1), eq(1), eq(1), eq(1));
    }

    @Test
    public void shouldAutoUnsubscribe(){
        // given
        Subscription mockSubscribe = Mockito.mock(Subscription.class);
        final TestSubscriber<Map<String, Integer>> testSubscriber = new TestSubscriber<>(presenter.queryStockCountObserver);
        presenter.queryStockCountObserver = testSubscriber;
        presenter.previousSubscribe = mockSubscribe;

        // when
        presenter.getDashboardData();
        testSubscriber.awaitTerminalEvent();

        // then
        Mockito.verify(mockSubscribe,Mockito.times(1)).unsubscribe();
        MatcherAssert.assertThat(presenter.subscriptions.size(), Matchers.is(1));
    }
}