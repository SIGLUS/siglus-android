package org.openlmis.core.presenter;


import androidx.annotation.NonNull;
import android.util.LongSparseArray;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class BulkInitialInventoryPresenterTest extends LMISRepositoryUnitTest {

    private BulkInitialInventoryPresenter bulkInitialInventoryPresenter;

    StockRepository stockRepositoryMock;
    StockMovementRepository stockMovementRepositoryMock;
    ProductRepository productRepositoryMock;
    InventoryPresenter.InventoryView view;
    private Product product;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private InventoryRepository mockInventoryRepository;
    private LongSparseArray<InventoryViewModel> models;
    private LongSparseArray<Product> basicProducts;
    private LongSparseArray<Product> noBasicProducts;
    private int DEFAULT_ID = 0;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);
        stockMovementRepositoryMock = mock(StockMovementRepository.class);
        productRepositoryMock = mock(ProductRepository.class);
        mockInventoryRepository = mock(InventoryRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        view = mock(InventoryPresenter.InventoryView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        bulkInitialInventoryPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(BulkInitialInventoryPresenter.class);
        bulkInitialInventoryPresenter.attachView(view);


        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
        models = new LongSparseArray<>();
        basicProducts = new LongSparseArray<>();
        noBasicProducts = new LongSparseArray<>();
        getInventoryViewModelList();
        getBasicProductList();
        getNoBasicProductList();
    }


    @After
    public void tearDown() {
        RoboGuice.Util.reset();
    }


    @Test
    public void shouldRestoreDraftInventoryWithDraftLotItems() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();
        ArrayList<DraftInitialInventory> draftInitialInventories = new ArrayList<>();
        DraftInitialInventory draftInitialInventory = new DraftInitialInventory();
        Product product = ProductBuilder.create().setProductId(1L).setCode("basicCode").setIsBasic(true).setPrimaryName("basicName").build();
        StockCard stockCard = buildDefaultStockCard();
        stockCard.setStockOnHand(107);

        draftInitialInventory.setProduct(product);
        draftInitialInventory.setQuantity(107L);

        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("test").setExpiryDate("Sep 2016").setQuantity("110").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("testNew").setExpiryDate("Sep 2016").setQuantity("10").build();

        inventoryViewModels.get(0).setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        DraftInitialInventoryLotItem draftInitialInventoryLotItem1 = new DraftInitialInventoryLotItem(lotMovementViewModel1,product);
        DraftInitialInventoryLotItem draftInitialInventoryLotItem2 = new DraftInitialInventoryLotItem(lotMovementViewModel2,product);

        draftInitialInventory.setDraftLotItemListWrapper(newArrayList(draftInitialInventoryLotItem1, draftInitialInventoryLotItem2));
        draftInitialInventories.add(draftInitialInventory);

        when(mockInventoryRepository.queryAllInitialDraft()).thenReturn(draftInitialInventories);
        bulkInitialInventoryPresenter.getInventoryViewModelList().addAll(inventoryViewModels);
        bulkInitialInventoryPresenter.restoreDraftInventory();

        assertThat(inventoryViewModels.get(0).getNewLotMovementViewModelList().size(), is(0));
        assertThat(inventoryViewModels.get(0).getExistingLotMovementViewModelList().get(0).getLotNumber(), is("test"));

    }

    @Test
    public void shouldLoadBasicProductsList() throws LMISException {
        when(productRepositoryMock.listBasicProducts()).thenReturn(newArrayList(basicProducts.get(1L), basicProducts.get(2L), basicProducts.get(3L), basicProducts.get(4L), basicProducts.get(5L)));

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = bulkInitialInventoryPresenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        //Basic Product amount: 5, Basic Product Header: 1
        assertEquals(6, receivedInventoryViewModels.size());
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
            bind(StockMovementRepository.class).toInstance(stockMovementRepositoryMock);
            bind(ProductRepository.class).toInstance(productRepositoryMock);
            bind(InventoryRepository.class).toInstance(mockInventoryRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }

    }

    private ArrayList<InventoryViewModel> getStockCardViewModels() {
        ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        inventoryViewModels.add(buildInventoryViewModelWithOutDraft(9, "11", null));
        InventoryViewModel inventoryViewModelWithOutDraft = buildInventoryViewModelWithOutDraft(3, "15", "11/02/2015");
        inventoryViewModels.add(inventoryViewModelWithOutDraft);
        return inventoryViewModels;
    }

    @NonNull
    private InventoryViewModel buildInventoryViewModelWithOutDraft(int stockCardId, String quantity, String expireDate) {
        InventoryViewModel inventoryViewModelWithOutDraft = new BulkInitialInventoryViewModel(buildDefaultStockCard());
        inventoryViewModelWithOutDraft.setStockCardId(stockCardId);
        return inventoryViewModelWithOutDraft;
    }
    private StockCard buildDefaultStockCard() {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100);
        stockCard.setProduct(product);
        return stockCard;
    }


    @NonNull
    private void getBasicProductList() {
        Product product1 = ProductBuilder.create().setProductId(1L).setIsBasic(true).setCode("bABC").setPrimaryName("bTest Product").build();
        Product product2 = ProductBuilder.create().setProductId(2L).setIsBasic(true).setCode("bProductCode2").setPrimaryName("bProductName2").build();
        Product product3 = ProductBuilder.create().setProductId(3L).setIsBasic(true).setCode("bProductCode3").setPrimaryName("bProductName3").build();
        Product product4 = ProductBuilder.create().setProductId(4L).setIsBasic(true).setCode("bProductCode4").setPrimaryName("bProductName4").build();
        Product product5 = ProductBuilder.create().setProductId(5L).setIsBasic(true).setCode("bProductCode5").setPrimaryName("bProductName5").build();
        basicProducts.put(product1.getId(), product1);
        basicProducts.put(product2.getId(), product2);
        basicProducts.put(product3.getId(), product3);
        basicProducts.put(product4.getId(), product4);
        basicProducts.put(product5.getId(), product5);
    }

    @NonNull
    private void getNoBasicProductList() {
        Product product1 = ProductBuilder.create().setProductId(1L).setIsBasic(false).setCode("ABC").setPrimaryName("Test Product").build();
        Product product2 = ProductBuilder.create().setProductId(2L).setIsBasic(false).setCode("productCode2").setPrimaryName("productName2").build();
        Product product3 = ProductBuilder.create().setProductId(3L).setIsBasic(false).setCode("productCode3").setPrimaryName("productName3").build();
        Product product4 = ProductBuilder.create().setProductId(4L).setIsBasic(false).setCode("productCode4").setPrimaryName("productName4").build();
        Product product5 = ProductBuilder.create().setProductId(5L).setIsBasic(false).setCode("productCode5").setPrimaryName("productName5").build();
        noBasicProducts.put(product1.getId(), product1);
        noBasicProducts.put(product2.getId(), product2);
        noBasicProducts.put(product3.getId(), product3);
        noBasicProducts.put(product4.getId(), product4);
        noBasicProducts.put(product5.getId(), product5);
    }

    @NonNull
    private void getInventoryViewModelList() {
        product = new Product();
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
}
