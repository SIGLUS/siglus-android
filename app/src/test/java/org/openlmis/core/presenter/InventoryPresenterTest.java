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

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.model.Product.IsKit;

@RunWith(LMISTestRunner.class)
public class InventoryPresenterTest extends LMISRepositoryUnitTest {

    private InventoryPresenter inventoryPresenter;

    StockRepository stockRepositoryMock;
    ProductRepository productRepositoryMock;
    InventoryPresenter.InventoryView view;
    private Product product;
    private StockCard stockCard;
    private SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);
        productRepositoryMock = mock(ProductRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        view = mock(InventoryPresenter.InventoryView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        inventoryPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(InventoryPresenter.class);
        inventoryPresenter.attachView(view);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setCode("ABC");

        stockCard = new StockCard();
        stockCard.setStockOnHand(100);
        stockCard.setProduct(product);
        stockCard.setExpireDates(StringUtils.EMPTY);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @After
    public void tearDown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldLoadPhysicalStockCardList() throws LMISException {
        StockCard stockCardVIA = StockCardBuilder.buildStockCard();
        stockCardVIA.setProduct(new ProductBuilder().setPrimaryName("VIA Product").build());
        StockCard stockCardMMIA = StockCardBuilder.buildStockCard();
        stockCardMMIA.setProduct(new ProductBuilder().setPrimaryName("MMIA Product").build());
        List<StockCard> stockCards = Arrays.asList(stockCardMMIA, stockCardVIA);
        when(stockRepositoryMock.list()).thenReturn(stockCards);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = inventoryPresenter.loadPhysicalInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        verify(stockRepositoryMock).list();
        subscriber.assertNoErrors();

        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        assertEquals(receivedInventoryViewModels.size(), 2);
    }

    @Test
    public void shouldLoadPhysicalStockCardListWithoutDeactivatedOrKitProducts() throws LMISException {
        StockCard stockCardVIA = StockCardBuilder.buildStockCard();
        stockCardVIA.setProduct(new ProductBuilder().setPrimaryName("VIA Product").setIsArchived(false).setIsActive(true).build());

        StockCard stockCardMMIA = StockCardBuilder.buildStockCard();
        stockCardMMIA.setProduct(new ProductBuilder().setPrimaryName("MMIA Product").setIsArchived(false).setIsActive(false).build());

        StockCard kitStockCard = StockCardBuilder.buildStockCard();
        kitStockCard.setProduct(new ProductBuilder().setPrimaryName("VIA Kit Product").setIsArchived(false).setIsActive(true).setIsKit(true).build());

        List<StockCard> stockCards = Arrays.asList(stockCardMMIA, stockCardVIA, kitStockCard);
        when(stockRepositoryMock.list()).thenReturn(stockCards);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = inventoryPresenter.loadPhysicalInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        assertEquals(1, receivedInventoryViewModels.size());
        assertEquals("VIA Product", receivedInventoryViewModels.get(0).getProductName());
    }

    @Test
    public void shouldLoadMasterProductsList() throws LMISException {
        StockCard stockCardVIA = StockCardBuilder.buildStockCard();
        Product productVIA = new ProductBuilder().setPrimaryName("VIA Product").setCode("VIA Code").build();
        stockCardVIA.setProduct(productVIA);
        StockCard stockCardMMIA = StockCardBuilder.buildStockCard();
        Product productMMIA = new ProductBuilder().setProductId(10L).setPrimaryName("MMIA Product").setCode("MMIA Code").setIsArchived(true).build();
        stockCardMMIA.setProduct(productMMIA);

        StockCard unknownAStockCard = StockCardBuilder.buildStockCard();
        Product productUnknownA = new ProductBuilder().setPrimaryName("A Unknown Product").setCode("A Code").build();
        unknownAStockCard.setProduct(productUnknownA);
        StockCard unknownBStockCard = StockCardBuilder.buildStockCard();
        Product productUnknownB = new ProductBuilder().setPrimaryName("B Unknown Product").setCode("B Code").build();
        unknownBStockCard.setProduct(productUnknownB);

        when(stockRepositoryMock.list()).thenReturn(Arrays.asList(stockCardVIA, stockCardMMIA));
        when(productRepositoryMock.listActiveProducts(IsKit.No)).thenReturn(Arrays.asList(productMMIA, productVIA, productUnknownB, productUnknownA));
        when(stockRepositoryMock.queryStockCardByProductId(10L)).thenReturn(stockCardMMIA);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = inventoryPresenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        assertEquals(3, receivedInventoryViewModels.size());
    }

    @Test
    public void shouldOnlyActivatedProductsInInventoryList() throws LMISException {
        Product activeProduct1 = ProductBuilder.create().setPrimaryName("active product").setCode("P2").build();
        Product activeProduct2 = ProductBuilder.create().setPrimaryName("active product").setCode("P3").build();

        when(stockRepositoryMock.list()).thenReturn(new ArrayList<StockCard>());
        when(productRepositoryMock.listActiveProducts(IsKit.No)).thenReturn(Arrays.asList(activeProduct1, activeProduct2));

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = inventoryPresenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);

        assertEquals(2, receivedInventoryViewModels.size());
    }

    @Test
    public void shouldInitStockCardAndCreateAInitInventoryMovementItem() throws LMISException {
        InventoryViewModel model = new StockCardViewModelBuilder(product).setChecked(true)
                .setQuantity("100").build();
        InventoryViewModel model2 = new StockCardViewModelBuilder(product).setChecked(false)
                .setQuantity("200").build();

        inventoryPresenter.initStockCards(newArrayList(model, model2));

        verify(stockRepositoryMock, times(1)).initStockCard(any(StockCard.class));
    }

    @Test
    public void shouldReInventoryArchivedStockCard() throws LMISException {
        InventoryViewModel uncheckedModel = new StockCardViewModelBuilder(product)
                .setChecked(false)
                .setQuantity("100")
                .build();

        Product archivedProduct = new ProductBuilder().setPrimaryName("Archived product").setCode("BBC")
                .setIsArchived(true).build();
        StockCard archivedStockCard = new StockCardBuilder().setStockOnHand(0).setProduct(archivedProduct).build();
        InventoryViewModel archivedViewModel = new StockCardViewModelBuilder(archivedStockCard)
                .setChecked(true)
                .setQuantity("200")
                .build();

        List<InventoryViewModel> inventoryViewModelList = newArrayList(uncheckedModel, archivedViewModel);

        inventoryPresenter.initStockCards(inventoryViewModelList);

        assertFalse(archivedStockCard.getProduct().isArchived());
        verify(stockRepositoryMock, times(1)).reInventoryArchivedStockCard(archivedStockCard);
    }

    @Test
    public void shouldGoToMainPageWhenOnNextCalled() {
        inventoryPresenter.nextMainPageAction.call(null);

        verify(view).loaded();
        verify(view).goToParentPage();
    }

    @Test
    public void shouldShowErrorWhenOnErrorCalled() {
        String errorMessage = "This is throwable error";
        inventoryPresenter.errorAction.call(new Throwable(errorMessage));

        verify(view).loaded();
        verify(view).showErrorMessage(errorMessage);
    }

    @Test
    public void shouldMakePositiveAdjustment() throws LMISException {

        InventoryViewModel model = new InventoryViewModel(stockCard);
        model.setQuantity("120");

        StockMovementItem item = inventoryPresenter.calculateAdjustment(model, stockCard);

        assertThat(item.getMovementType(), is(StockMovementItem.MovementType.POSITIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
        assertThat(item.getStockOnHand(), is(Long.parseLong(model.getQuantity())));
        assertThat(item.getStockCard(), is(stockCard));
    }

    @Test
    public void shouldMakeNegativeAdjustment() throws LMISException {
        InventoryViewModel model = new InventoryViewModel(stockCard);
        model.setQuantity("80");

        StockMovementItem item = inventoryPresenter.calculateAdjustment(model, stockCard);

        assertThat(item.getMovementType(), is(StockMovementItem.MovementType.NEGATIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
        assertThat(item.getStockOnHand(), is(Long.parseLong(model.getQuantity())));
        assertThat(item.getStockCard(), is(stockCard));
    }


    @Test
    public void shouldCalculateStockAdjustment() throws LMISException {
        InventoryViewModel model = new InventoryViewModel(stockCard);
        model.setQuantity("100");

        StockMovementItem item = inventoryPresenter.calculateAdjustment(model, stockCard);

        assertThat(item.getMovementType(), is(StockMovementItem.MovementType.PHYSICAL_INVENTORY));
        assertThat(item.getMovementQuantity(), is(0L));
        assertThat(item.getStockOnHand(), is(Long.parseLong(model.getQuantity())));
        assertThat(item.getStockCard(), is(stockCard));
    }

    @Test
    public void shouldRestoreDraftInventory() throws Exception {

        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();

        ArrayList<DraftInventory> draftInventories = new ArrayList<>();
        DraftInventory draftInventory = new DraftInventory();
        stockCard.setId(9);
        draftInventory.setStockCard(stockCard);
        draftInventory.setQuantity(20L);
        draftInventory.setExpireDates("11/10/2015");
        draftInventories.add(draftInventory);
        when(stockRepositoryMock.listDraftInventory()).thenReturn(draftInventories);

        inventoryPresenter.restoreDraftInventory(inventoryViewModels);
        assertThat(inventoryViewModels.get(0).getQuantity(), is("20"));
        assertThat(inventoryViewModels.get(0).getExpiryDates().get(0), is("11/10/2015"));
        assertThat(inventoryViewModels.get(1).getQuantity(), is("15"));
        assertThat(inventoryViewModels.get(1).getExpiryDates().get(0), is("11/02/2015"));
    }

    private ArrayList<InventoryViewModel> getStockCardViewModels() {
        ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        inventoryViewModels.add(buildStockCardWithOutDraft(9, "11", null));
        InventoryViewModel inventoryViewModelWithOutDraft = buildStockCardWithOutDraft(3, "15", "11/02/2015");
        inventoryViewModels.add(inventoryViewModelWithOutDraft);
        return inventoryViewModels;
    }

    @NonNull
    private InventoryViewModel buildStockCardWithOutDraft(int stockCardId, String quantity, String expireDate) {
        InventoryViewModel inventoryViewModelWithOutDraft = new InventoryViewModel(stockCard);
        inventoryViewModelWithOutDraft.setStockCardId(stockCardId);
        inventoryViewModelWithOutDraft.setQuantity(quantity);
        ArrayList<String> expireDates = new ArrayList<>();
        expireDates.add(expireDate);
        inventoryViewModelWithOutDraft.setExpiryDates(expireDates);
        return inventoryViewModelWithOutDraft;
    }

    @Test
    public void shouldShowSignatureDialog() throws Exception {
        when(view.validateInventory()).thenReturn(true);
        inventoryPresenter.signPhysicalInventory();
        verify(view).showSignDialog();
    }

    @Test
    public void shouldSetSignatureToViewModel() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();
        String signature = "signature";
        inventoryPresenter.doPhysicalInventory(inventoryViewModels, signature);
        assertThat(inventoryViewModels.get(0).getSignature(), is(signature));
        assertThat(inventoryViewModels.get(1).getSignature(), is(signature));
    }

    @Test
    public void shouldUpdateLatestDoPhysicalInventoryTime() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable observable = inventoryPresenter.stockMovementObservable(inventoryViewModels);
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        verify(sharedPreferenceMgr).setLatestPhysicInventoryTime(anyString());
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
            bind(ProductRepository.class).toInstance(productRepositoryMock);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }
    }
}
