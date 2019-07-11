package org.openlmis.core.presenter;

import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModelBuilder;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class InitialInventoryPresenterTest extends LMISRepositoryUnitTest {

    private InitialInventoryPresenter initialInventoryPresenter;

    StockRepository stockRepositoryMock;
    ProductRepository productRepositoryMock;
    InventoryPresenter.InventoryView view;
    private Product product;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private InventoryRepository mockInventoryRepository;
    private LongSparseArray<InventoryViewModel> models;
    private int DEFAULT_ID = 0;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);
        productRepositoryMock = mock(ProductRepository.class);
        mockInventoryRepository = mock(InventoryRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        view = mock(InventoryPresenter.InventoryView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        initialInventoryPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(InitialInventoryPresenter.class);
        initialInventoryPresenter.attachView(view);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setCode("ABC");

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
        models = new LongSparseArray<>();
        getInventoryViewModelList();
    }

    @NonNull
    private void getInventoryViewModelList() {
        product.setBasic(true);
        product.setPrimaryName("Product 1");
        product.setId(1L);
        InventoryViewModel inventoryViewModel = new InventoryViewModel(product);
        Product product2 = new Product();
        product2.setPrimaryName("Product 2");
        product2.setCode("BCD");
        product2.setBasic(true);
        product2.setId(2L);
        InventoryViewModel inventoryViewModel2 = new InventoryViewModel(product2);
        Product product3 = new Product();
        product3.setPrimaryName("Product 3");
        product3.setCode("BCDE");
        product3.setBasic(true);
        product3.setId(3L);
        InventoryViewModel inventoryViewModel3 = new InventoryViewModel(product3);
        Product product4 = new Product();
        product4.setPrimaryName("Product 4");
        product4.setCode("BCDEF");
        product4.setBasic(false);
        product4.setId(4L);
        InventoryViewModel inventoryViewModel4 = new InventoryViewModel(product4);

        models.put(inventoryViewModel.getProduct().getId(), inventoryViewModel);
        models.put(inventoryViewModel2.getProduct().getId(), inventoryViewModel2);
        models.put(inventoryViewModel3.getProduct().getId(), inventoryViewModel3);
        models.put(inventoryViewModel4.getProduct().getId(), inventoryViewModel4);
    }

    @After
    public void tearDown() {
        RoboGuice.Util.reset();
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

        when(productRepositoryMock.listProductsArchivedOrNotInStockCard()).thenReturn(Arrays.asList(productMMIA, productVIA, productUnknownB, productUnknownA));
        when(stockRepositoryMock.queryStockCardByProductId(10L)).thenReturn(stockCardMMIA);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = initialInventoryPresenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        assertEquals(4, receivedInventoryViewModels.size());
    }

    @Test
    public void shouldOnlyActivatedProductsInInventoryList() throws LMISException {
        Product activeProduct1 = ProductBuilder.create().setPrimaryName("active product").setCode("P2").build();
        Product activeProduct2 = ProductBuilder.create().setPrimaryName("active product").setCode("P3").build();

        when(stockRepositoryMock.list()).thenReturn(new ArrayList<StockCard>());
        when(productRepositoryMock.listProductsArchivedOrNotInStockCard()).thenReturn(Arrays.asList(activeProduct1, activeProduct2));

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = initialInventoryPresenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);

        assertEquals(2, receivedInventoryViewModels.size());
    }

    @Test
    public void shouldLoadBasicProductsForInventory() throws LMISException {
        Product basicProduct1 = ProductBuilder.create().setPrimaryName("product").setCode("P1").setIsBasic(true).build();
        Product basicProduct2 = ProductBuilder.create().setPrimaryName("product").setCode("P2").setIsBasic(true).build();

        when(productRepositoryMock.listBasicProducts()).thenReturn(Arrays.asList(basicProduct1, basicProduct2));

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = initialInventoryPresenter.loadInventoryWithBasicProducts();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);

        assertEquals(2, receivedInventoryViewModels.size());
    }

    @Test
    public void shouldInitStockCardAndCreateAInitInventoryMovementItem() throws LMISException {
        StockCard stockcard = new StockCard();
        stockcard.setProduct(product);
        InventoryViewModel model = new InventoryViewModelBuilder(stockcard).setChecked(true).build();
        InventoryViewModel model2 = new InventoryViewModelBuilder(product).setChecked(true).build();

        model2.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2016").setQuantity("20").build());
        model.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Feb 2015").setQuantity("2020").build());
        model.getProduct().setArchived(true);

        initialInventoryPresenter.getInventoryViewModelList().add(model);
        initialInventoryPresenter.getInventoryViewModelList().add(model2);
        initialInventoryPresenter.getDefaultViewModelList().add(model);
        initialInventoryPresenter.getDefaultViewModelList().add(model2);
        initialInventoryPresenter.initOrArchiveBackStockCards("default");

        verify(stockRepositoryMock, times(1)).updateStockCardWithProduct(any(StockCard.class));
        verify(stockRepositoryMock, times(1)).addStockMovementAndUpdateStockCard(any(StockMovementItem.class));
    }

    @Test
    public void shouldReInventoryArchivedStockCard() throws LMISException {
        InventoryViewModel uncheckedModel = new InventoryViewModelBuilder(product)
                .setChecked(false)
                .build();

        Product archivedProduct = new ProductBuilder().setPrimaryName("Archived product").setCode("BBC")
                .setIsArchived(true).build();
        StockCard archivedStockCard = new StockCardBuilder().setStockOnHand(0).setProduct(archivedProduct).build();
        InventoryViewModel archivedViewModel = new InventoryViewModelBuilder(archivedStockCard)
                .setChecked(true)
                .build();

        List<InventoryViewModel> inventoryViewModelList = newArrayList(uncheckedModel, archivedViewModel);

        initialInventoryPresenter.getInventoryViewModelList().addAll(inventoryViewModelList);
        initialInventoryPresenter.getDefaultViewModelList().addAll(inventoryViewModelList);
        initialInventoryPresenter.initOrArchiveBackStockCards("default");

        assertFalse(archivedStockCard.getProduct().isArchived());
        verify(stockRepositoryMock, times(1)).updateStockCardWithProduct(archivedStockCard);
    }

    @Test
    public void shouldInitStockCardAndCreateAInitInventoryMovementItemWithLot() throws Exception {
        product.setArchived(false);

        InventoryViewModel model = new InventoryViewModelBuilder(product).setChecked(true).build();

        initialInventoryPresenter.getInventoryViewModelList().add(model);
        initialInventoryPresenter.getDefaultViewModelList().add(model);
        initialInventoryPresenter.initOrArchiveBackStockCards("default");

        ArgumentCaptor<StockMovementItem> argument = ArgumentCaptor.forClass(StockMovementItem.class);
        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(argument.capture());
    }
    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
            bind(ProductRepository.class).toInstance(productRepositoryMock);
            bind(InventoryRepository.class).toInstance(mockInventoryRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }

    }

    @Test
    public void shouldHaveTheSameNumberOfDefaultElementsPlusTwoHeaders() throws Exception {
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("");

        assertThat(initialInventoryPresenter.getInventoryViewModelList().size(), is(6));
    }

    @Test
    public void shouldHaveOneBasicAndOneNonBasicHeader() throws Exception {
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("");

        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId() == DEFAULT_ID);
        assertFalse(initialInventoryPresenter.getInventoryViewModelList().get(4).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(4).getProductId() == DEFAULT_ID);
    }

    @Test
    public void shouldHaveOneNonBasicHeader() throws Exception {
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("Product 4");

        assertThat(initialInventoryPresenter.getInventoryViewModelList().size(), is(2));
        assertFalse(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId() == DEFAULT_ID);
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId() != DEFAULT_ID);
    }

    @Test
    public void shouldHaveOneBasicHeader() throws Exception {
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("Product 1");

        assertThat(initialInventoryPresenter.getInventoryViewModelList().size(), is(2));
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId() == DEFAULT_ID);
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId() != DEFAULT_ID);
    }


    @Test
    public void shouldArrangeViewModelsWithNonEmptyQueryStringResultingInNonBasicInventoryViewModels() throws Exception {
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("Product 4");

        assertThat(initialInventoryPresenter.getInventoryViewModelList().size(), is(2));
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId()==4L);
    }


    @Test
    public void shouldArrangeViewModelsWithNonEmptyQueryStringResultingInBasicInventoryViewModels() throws Exception {
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("Product 1");

        assertThat(initialInventoryPresenter.getInventoryViewModelList().size(), is(2));
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId()==0);
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId()==1L);
    }

    @Test
    public void shouldArrangeViewModelsWithNonEmptyQueryStringResultingInBasicAndNonBasicInventoryViewModels() throws Exception {
        models.get(3L).getProduct().setPrimaryName("Product 43");
        initialInventoryPresenter.getDefaultViewModelList().addAll(newArrayList(models.get(1L),models.get(2L),models.get(3L),models.get(4L)));

        initialInventoryPresenter.filterViewModels("Product 4");

        assertThat(initialInventoryPresenter.getInventoryViewModelList().size(), is(4));
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId()==0);
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId()==3L);
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(2).getProductId()==0);
        assertFalse(initialInventoryPresenter.getInventoryViewModelList().get(2).isBasic());
        assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(3).getProductId()==4L);
    }
}