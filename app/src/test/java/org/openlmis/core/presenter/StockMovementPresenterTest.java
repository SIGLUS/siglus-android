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

package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockMovementPresenterTest extends LMISRepositoryUnitTest {

    private StockMovementPresenter stockMovementPresenter;

    StockRepository stockRepositoryMock;
    StockMovementPresenter.StockMovementView view;

    SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        view = mock(StockMovementPresenter.StockMovementView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        stockMovementPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockMovementPresenter.class);
        stockMovementPresenter.attachView(view);
        stockMovementPresenter.stockCard = StockCardBuilder.buildStockCard();
        stockMovementPresenter.sharedPreferenceMgr = sharedPreferenceMgr;

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @Test
    public void shouldValidateStockMovementViewModelBeforeSaveAndReturnErrorIfInvalid() {
        StockMovementViewModel stockMovementViewModelMock = mock(StockMovementViewModel.class);
        when(stockMovementViewModelMock.validateEmpty()).thenReturn(false);

        stockMovementPresenter.submitStockMovement(stockMovementViewModelMock);
        verify(stockMovementViewModelMock, times(2)).validateEmpty();
        verify(view).showErrorAlert(anyString());
    }

    @Test
    public void shouldSaveStockMovement() throws LMISException {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(new Product());
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);

        StockMovementItem item = new StockMovementItem();

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.validateInputValid()).thenReturn(true);
        when(viewModel.validateEmpty()).thenReturn(true);
        when(viewModel.convertViewToModel()).thenReturn(item);

        stockMovementPresenter.submitStockMovement(viewModel);

        verify(view).showSignDialog();
    }

    @Test
    public void shouldSaveAndRefresh() throws Exception {
        //given
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(1);
        Product product = new Product();
        product.setActive(true);
        stockCard.setProduct(product);

        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(0L);

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel()).thenReturn(item);
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);

        //when
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        assertThat(stockMovementPresenter.getStockCard().getStockOnHand()).isEqualTo(item.getStockOnHand());
        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(item);
        verify(view).updateArchiveMenus(true);
    }

    @Test
    public void shouldNotEnableArchiveMenuForKitsEvenIfSOHIsZero() throws Exception {
        //given
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(0);
        Product product = new Product();
        product.setActive(true);
        product.setKit(true);
        stockCard.setProduct(product);

        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(0L);

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel()).thenReturn(item);

        //when
        stockMovementPresenter.stockCard = stockCard;
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        verify(view).updateArchiveMenus(false);
    }

    @Test
    public void shouldLoadStockMovementViewModelsObserver() throws Exception {
        when(stockRepositoryMock.listLastFive(anyInt())).thenReturn(new ArrayList<StockMovementItem>());

        TestSubscriber<List<StockMovementViewModel>> subscriber = new TestSubscriber<>();
        stockMovementPresenter.loadStockMovementViewModelsObserver().subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValue(new ArrayList<StockMovementViewModel>());
    }

    @Test
    public void shouldAddStockMovementViewModelWhenSubscriberOnNext() throws ParseException {
        Product product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();
        StockCardViewModel viewModel = new StockCardViewModelBuilder(product).build();

        stockMovementPresenter.loadStockMovementViewModelSubscriber().onNext((List) newArrayList(viewModel, viewModel));

        assertThat(stockMovementPresenter.stockMovementModelList.size()).isEqualTo(3);
        verify(view).refreshStockMovement();
        verify(view).loaded();
    }

    @Test
    public void shouldArchiveStockCard() throws Exception {
        //given
        StockCard stockCard = stockMovementPresenter.stockCard;
        stockCard.getProduct().setArchived(false);

        //when
        stockMovementPresenter.archiveStockCard();

        //then
        assertThat(stockCard.getProduct().isArchived()).isTrue();
        verify(stockRepositoryMock).updateProductOfStockCard(stockCard);
    }

    @Test
    public void shouldGetCmm() {
        //given
        StockCard stockCard = stockMovementPresenter.stockCard;
        when(stockRepositoryMock.getCmm(stockCard)).thenReturn(10);

        //when
        String stockCardCmm = stockMovementPresenter.getStockCardCmm();

        //then
        assertThat(stockCardCmm).isEqualTo("10");
    }


    @Test
    public void shouldUpdateNotifyDeactivatedProductList() throws LMISException {
        //given
        StockCard stockCard = stockMovementPresenter.stockCard;
        stockCard.setStockOnHand(0);
        stockCard.getProduct().setActive(false);
        stockCard.getProduct().setPrimaryName("name");
        StockMovementItem item = new StockMovementItem();


        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel()).thenReturn(item);
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);

        //when
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        verify(sharedPreferenceMgr).setIsNeedShowProductsUpdateBanner(true, "name");
        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(item);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }
}
