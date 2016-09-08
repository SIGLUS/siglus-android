package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
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
    private StockCard stockCard;
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

        stockCard = buildDefaultStockCard();

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    private StockCard buildDefaultStockCard() {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100);
        stockCard.setProduct(product);
        stockCard.setExpireDates(StringUtils.EMPTY);
        return stockCard;
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
        InventoryViewModel model = new StockCardViewModelBuilder(product).setChecked(true)
                .setQuantity("100").build();
        InventoryViewModel model2 = new StockCardViewModelBuilder(product).setChecked(false)
                .setQuantity("200").build();

        initialInventoryPresenter.getInventoryViewModelList().add(model);
        initialInventoryPresenter.getInventoryViewModelList().add(model2);
        initialInventoryPresenter.initOrArchiveBackStockCards();

        verify(stockRepositoryMock, times(1)).createOrUpdateStockCardWithStockMovement(any(StockCard.class));
    }

    @Test
    public void shouldNotClearExpiryDateWhenSohIsNotZeroAndIsArchivedDrug() throws LMISException {
        stockCard.setExpireDates("01/01/2016");
        product.setArchived(true);

        InventoryViewModel model = new StockCardViewModelBuilder(stockCard).setChecked(true)
                .setQuantity("10").build();

        initialInventoryPresenter.initOrArchiveBackStockCards();

        assertThat(model.getStockCard().getExpireDates(), is("01/01/2016"));

    }

    @Test
    public void shouldClearExpiryDateWhenSohIsZeroAndIsNewDrug() throws LMISException {
        InventoryViewModel model = new StockCardViewModelBuilder(product).setChecked(true)
                .setQuantity("0").setExpiryDates(newArrayList("01/01/2016")).build();

        initialInventoryPresenter.getInventoryViewModelList().add(model);
        initialInventoryPresenter.initOrArchiveBackStockCards();

        ArgumentCaptor<StockCard> argument = ArgumentCaptor.forClass(StockCard.class);
        verify(stockRepositoryMock).createOrUpdateStockCardWithStockMovement(argument.capture());
        assertThat(argument.getValue().getExpireDates(), is(""));
    }

    @Test
    public void shouldNotClearExpiryDateWhenSohIsNotZeroAndIsNewDrug() throws LMISException {
        InventoryViewModel model = new StockCardViewModelBuilder(product).setChecked(true)
                .setQuantity("10").setExpiryDates(newArrayList("01/01/2016")).build();

        initialInventoryPresenter.getInventoryViewModelList().add(model);
        initialInventoryPresenter.initOrArchiveBackStockCards();

        ArgumentCaptor<StockCard> argument = ArgumentCaptor.forClass(StockCard.class);
        verify(stockRepositoryMock).createOrUpdateStockCardWithStockMovement(argument.capture());
        assertThat(argument.getValue().getExpireDates(), is("01/01/2016"));
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

        initialInventoryPresenter.getInventoryViewModelList().addAll(inventoryViewModelList);
        initialInventoryPresenter.initOrArchiveBackStockCards();

        assertFalse(archivedStockCard.getProduct().isArchived());
        verify(stockRepositoryMock, times(1)).updateStockCardWithProduct(archivedStockCard);
    }

    @Test
    public void shouldInitStockCardAndCreateAInitInventoryMovementItemWithLot() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_lot_management,true);

        product.setArchived(false);

        InventoryViewModel model = new StockCardViewModelBuilder(product).setChecked(true)
                .setQuantity("10").setExpiryDates(newArrayList("01/01/2016")).build();

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