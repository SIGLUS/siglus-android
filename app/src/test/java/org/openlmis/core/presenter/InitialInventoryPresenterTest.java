package org.openlmis.core.presenter;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        initialInventoryPresenter.initOrArchiveBackStockCards();

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
        initialInventoryPresenter.initOrArchiveBackStockCards();

        assertFalse(archivedStockCard.getProduct().isArchived());
        verify(stockRepositoryMock, times(1)).updateStockCardWithProduct(archivedStockCard);
    }

    @Test
    public void shouldInitStockCardAndCreateAInitInventoryMovementItemWithLot() throws Exception {
        product.setArchived(false);

        InventoryViewModel model = new InventoryViewModelBuilder(product).setChecked(true).build();

        initialInventoryPresenter.getInventoryViewModelList().add(model);
        initialInventoryPresenter.initOrArchiveBackStockCards();

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

}