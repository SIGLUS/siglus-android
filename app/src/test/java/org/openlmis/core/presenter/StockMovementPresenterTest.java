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

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockMovementPresenterTest extends LMISRepositoryUnitTest {

    private StockMovementPresenter stockMovementPresenter;

    StockRepository stockRepositoryMock;
    ProductRepository productRepository;
    StockMovementPresenter.StockMovementView view;
    StockService stockServiceMock;

    SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);
        productRepository = mock(ProductRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        stockServiceMock = mock(StockService.class);

        view = mock(StockMovementPresenter.StockMovementView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        stockMovementPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockMovementPresenter.class);
        stockMovementPresenter.attachView(view);
        stockMovementPresenter.stockCard = StockCardBuilder.buildStockCard();
        stockMovementPresenter.sharedPreferenceMgr = sharedPreferenceMgr;
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
        when(viewModel.validateQuantitiesNotZero()).thenReturn(true);
        when(viewModel.convertViewToModel(stockCard)).thenReturn(item);

        stockMovementPresenter.submitStockMovement(viewModel);

        verify(view).showSignDialog();
    }

    @Test
    public void shouldSaveAndRefresh() throws Exception {
        //given
        StockCard stockCard = createStockCard(1, false);
        stockCard.setExpireDates("2016-01-01,2017-02-01");

        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(0L);

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel(stockCard)).thenReturn(item);
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);
        reset(view);

        //when
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        assertThat(stockMovementPresenter.getStockCard().getStockOnHand()).isEqualTo(item.getStockOnHand());
        assertThat(stockCard.getExpireDates()).isEqualTo("");
        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(item);
        verify(view).updateArchiveMenus(true);
        verify(view).updateExpiryDateViewGroup();
    }

    @Test
    public void shouldNotEnableArchiveMenuForKitsEvenIfSOHIsZero() throws Exception {
        //given
        StockCard stockCard = createStockCard(0, true);
        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(0L);

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel(stockCard)).thenReturn(item);

        //when
        stockMovementPresenter.stockCard = stockCard;
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        verify(view).updateArchiveMenus(false);
    }

    @Test
    public void shouldNotEnableUnpackMenuForKitsIfMovementItemSOHIsZero() throws Exception {
        //given
        StockCard stockCard = createStockCard(100, true);
        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(0L);

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel(stockCard)).thenReturn(item);

        //when
        stockMovementPresenter.stockCard = stockCard;
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        verify(view).updateUnpackKitMenu(false);
    }

    @Test
    public void shouldNotEnableUnpackMenuForKitsIfKitsProductSizeIsZero() throws Exception {
        //given
        Product kit = ProductBuilder.buildAdultProduct();

        StockCard stockCard = createStockCard(100, true);
        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(1L);
        stockCard.getProduct().setKitProductList(new ArrayList<KitProduct>());

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel(stockCard)).thenReturn(item);

        when(productRepository.queryKitProductByKitCode(kit.getCode())).thenReturn(new ArrayList<KitProduct>());

        //when
        stockMovementPresenter.stockCard = stockCard;
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        verify(view).updateUnpackKitMenu(false);
    }

    @Test
    public void shouldEnableUnpackMenuAndUpdateExpiryDateGroupWhenStockCardSOHIsNotZeroAndKitHasProducts() throws Exception {
        //given
        StockCard stockCard = createStockCard(100, true);
        Product kit = ProductBuilder.buildAdultProduct();
        kit.setKit(true);
        stockCard.setProduct(kit);
        when(stockRepositoryMock.queryStockCardById(200L)).thenReturn(stockCard);
        when(productRepository.queryKitProductByKitCode(kit.getCode())).thenReturn(Arrays.asList(new KitProduct()));

        //when
        stockMovementPresenter.setStockCard(200L);

        //then
        verify(view).updateUnpackKitMenu(true);
        verify(view).updateExpiryDateViewGroup();
    }

    @Test
    public void shouldDisableUnpackMenuWhenStockCardSOHIsNotZeroAndKitHasNoProduct() throws Exception {
        //given
        StockCard stockCard = createStockCard(100, true);
        Product kit = ProductBuilder.buildAdultProduct();
        kit.setKit(true);
        stockCard.setProduct(kit);
        when(stockRepositoryMock.queryStockCardById(200L)).thenReturn(stockCard);
        when(productRepository.queryKitProductByKitCode(kit.getCode())).thenReturn(new ArrayList<KitProduct>());

        //when
        stockMovementPresenter.setStockCard(200L);

        //then
        verify(view).updateUnpackKitMenu(false);
    }

    @NonNull
    private StockCard createStockCard(int stockOnHand, boolean isKit) {
        StockCard stockCard = new StockCard();
        stockCard.setId(200L);
        stockCard.setStockOnHand(stockOnHand);
        Product product = new Product();
        product.setActive(true);
        product.setKit(isKit);
        product.setKitProductList(Arrays.asList(new KitProduct()));
        stockCard.setProduct(product);
        return stockCard;
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
        InventoryViewModel viewModel = new StockCardViewModelBuilder(product).build();

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
        verify(stockRepositoryMock).updateProductOfStockCard(stockCard.getProduct());
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
        when(viewModel.convertViewToModel(stockCard)).thenReturn(item);
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);

        //when
        stockMovementPresenter.saveAndRefresh(viewModel);

        //then
        verify(sharedPreferenceMgr).setIsNeedShowProductsUpdateBanner(true, "name");
        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(item);
    }

    @Test
    public void shouldGetLotOnHandForStockCard() throws LMISException {
        StockCard stockCard = new StockCardBuilder().setStockCardId(1L).setProduct(new ProductBuilder().setProductId(1L).build()).build();

        Lot lot = new LotBuilder().setLotNumber("abc").build();
        LotOnHand lotOnHand = new LotOnHand();
        lotOnHand.setStockCard(stockCard);
        lotOnHand.setLot(lot);

        when(stockRepositoryMock.getNonEmptyLotOnHandByStockCard(1L)).thenReturn(newArrayList(lotOnHand));

        assertEquals(1, stockMovementPresenter.getLotOnHandByStockCard(stockCard).size());
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
            bind(ProductRepository.class).toInstance(productRepository);
            bind(StockService.class).toInstance(stockServiceMock);
        }
    }
}
