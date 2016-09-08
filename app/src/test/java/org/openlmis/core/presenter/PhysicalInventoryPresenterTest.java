package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryPresenterTest extends LMISRepositoryUnitTest {
    private PhysicalInventoryPresenter physicalInventoryPresenter;

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

        physicalInventoryPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PhysicalInventoryPresenter.class);
        physicalInventoryPresenter.attachView(view);

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
    public void shouldLoadPhysicalStockCardList() throws LMISException {
        StockCard stockCardVIA = StockCardBuilder.buildStockCard();
        stockCardVIA.setProduct(new ProductBuilder().setPrimaryName("VIA Product").build());
        StockCard stockCardMMIA = StockCardBuilder.buildStockCard();
        stockCardMMIA.setProduct(new ProductBuilder().setPrimaryName("MMIA Product").build());
        List<StockCard> stockCards = Arrays.asList(stockCardMMIA, stockCardVIA);
        when(stockRepositoryMock.list()).thenReturn(stockCards);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = physicalInventoryPresenter.loadInventory();
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
        Observable<List<InventoryViewModel>> observable = physicalInventoryPresenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        assertEquals(1, receivedInventoryViewModels.size());
        assertEquals("VIA Product", receivedInventoryViewModels.get(0).getProductName());
    }

    @Test
    public void shouldMakePositiveAdjustment() throws LMISException {

        InventoryViewModel model = new InventoryViewModel(stockCard);
        model.setQuantity("120");

        StockMovementItem item = physicalInventoryPresenter.calculateAdjustment(model, stockCard);

        assertThat(item.getMovementType(), is(MovementReasonManager.MovementType.POSITIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
        assertThat(item.getStockOnHand(), is(Long.parseLong(model.getQuantity())));
        assertThat(item.getStockCard(), is(stockCard));
    }

    @Test
    public void shouldMakeNegativeAdjustment() throws LMISException {
        InventoryViewModel model = new InventoryViewModel(stockCard);
        model.setQuantity("80");

        StockMovementItem item = physicalInventoryPresenter.calculateAdjustment(model, stockCard);

        assertThat(item.getMovementType(), is(MovementReasonManager.MovementType.NEGATIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
        assertThat(item.getStockOnHand(), is(Long.parseLong(model.getQuantity())));
        assertThat(item.getStockCard(), is(stockCard));
    }


    @Test
    public void shouldCalculateStockAdjustment() throws LMISException {
        InventoryViewModel model = new InventoryViewModel(stockCard);
        model.setQuantity("100");

        StockMovementItem item = physicalInventoryPresenter.calculateAdjustment(model, stockCard);

        assertThat(item.getMovementType(), is(MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
        assertThat(item.getMovementQuantity(), is(0L));
        assertThat(item.getStockOnHand(), is(Long.parseLong(model.getQuantity())));
        assertThat(item.getStockCard(), is(stockCard));
    }

    @Test
    public void shouldRestoreDraftInventory() throws Exception {

        physicalInventoryPresenter.getInventoryViewModelList().addAll(getStockCardViewModels());

        ArrayList<DraftInventory> draftInventories = new ArrayList<>();
        DraftInventory draftInventory = new DraftInventory();
        stockCard.setId(9);
        draftInventory.setStockCard(stockCard);
        draftInventory.setQuantity(20L);
        draftInventory.setExpireDates("11/10/2015");
        draftInventories.add(draftInventory);
        when(mockInventoryRepository.queryAllDraft()).thenReturn(draftInventories);

        physicalInventoryPresenter.restoreDraftInventory();
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(0).getQuantity(), is("20"));
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(0).getExpiryDates().get(0), is("11/10/2015"));
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(1).getQuantity(), is("15"));
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(1).getExpiryDates().get(0), is("11/02/2015"));
    }

    @Test
    public void shouldRestoreDraftInventoryWithLDraftLotItems() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_lot_management,true);
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();

        ArrayList<DraftInventory> draftInventories = new ArrayList<>();
        DraftInventory draftInventory = new DraftInventory();
        stockCard.setId(9);
        draftInventory.setStockCard(stockCard);
        draftInventory.setQuantity(20L);
        draftInventory.setExpireDates("11/10/2015");
        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("test").setExpiryDate("Sep 2016").setQuantity("10").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("testNew").setExpiryDate("Sep 2016").setQuantity("10").build();
        inventoryViewModels.get(0).setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        DraftLotItem draftLotItem1 = new DraftLotItem(lotMovementViewModel1, stockCard.getProduct(), false);
        DraftLotItem draftLotItem2 = new DraftLotItem(lotMovementViewModel2, stockCard.getProduct(), true);
        draftInventory.setDraftLotItemListWrapper(newArrayList(draftLotItem1, draftLotItem2));
        draftInventories.add(draftInventory);
        when(mockInventoryRepository.queryAllDraft()).thenReturn(draftInventories);

        physicalInventoryPresenter.getInventoryViewModelList().addAll(inventoryViewModels);
        physicalInventoryPresenter.restoreDraftInventory();
        assertThat(inventoryViewModels.get(0).getQuantity(), is("20"));
        assertThat(inventoryViewModels.get(0).getExpiryDates().get(0), is("11/10/2015"));
        assertThat(inventoryViewModels.get(0).getLotMovementViewModelList().get(0).getLotNumber(), is("testNew"));
        assertThat(inventoryViewModels.get(0).getExistingLotMovementViewModelList().get(0).getLotNumber(), is("test"));
    }

    @Test
    public void shouldRestoreDraftInventoryWithEmptyQuantity() throws Exception {
        ArrayList<DraftInventory> draftInventories = new ArrayList<>();
        DraftInventory draftInventory = new DraftInventory();
        stockCard.setId(9);
        draftInventory.setStockCard(stockCard);
        draftInventory.setQuantity(null);
        draftInventory.setExpireDates("11/10/2015");
        draftInventories.add(draftInventory);

        when(mockInventoryRepository.queryAllDraft()).thenReturn(draftInventories);

        physicalInventoryPresenter.getInventoryViewModelList().addAll(getStockCardViewModels());
        physicalInventoryPresenter.restoreDraftInventory();
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(0).getQuantity(), is(""));
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
        InventoryViewModel inventoryViewModelWithOutDraft = new PhysicalInventoryViewModel(buildDefaultStockCard());
        inventoryViewModelWithOutDraft.setStockCardId(stockCardId);
        inventoryViewModelWithOutDraft.setQuantity(quantity);
        ArrayList<String> expireDates = new ArrayList<>();
        expireDates.add(expireDate);
        inventoryViewModelWithOutDraft.setExpiryDates(expireDates);
        return inventoryViewModelWithOutDraft;
    }

    @Test
    public void shouldSetSignatureToViewModel() throws Exception {
        String signature = "signature";
        physicalInventoryPresenter.getInventoryViewModelList().addAll(getStockCardViewModels());
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        Subscription subscription = physicalInventoryPresenter.stockMovementObservable(signature).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        physicalInventoryPresenter.subscriptions.add(subscription);
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(0).getSignature(), is(signature));
        assertThat(physicalInventoryPresenter.getInventoryViewModelList().get(1).getSignature(), is(signature));
    }

    @Test
    public void shouldUpdateLatestDoPhysicalInventoryTime() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        String sign = "test";
        physicalInventoryPresenter.getInventoryViewModelList().clear();
        physicalInventoryPresenter.getInventoryViewModelList().addAll(inventoryViewModels);
        Observable observable = physicalInventoryPresenter.stockMovementObservable(sign);
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        verify(sharedPreferenceMgr).setLatestPhysicInventoryTime(anyString());
    }

    @Test
    public void shouldSaveInventoryWhenCompletePhysicalInventory() throws Exception {

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        physicalInventoryPresenter.getInventoryViewModelList().clear();
        physicalInventoryPresenter.getInventoryViewModelList().addAll(getStockCardViewModels());
        String sign = "signature";
        Observable observable = physicalInventoryPresenter.stockMovementObservable(sign);
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        verify(mockInventoryRepository).save(any(Inventory.class));
    }

    @Test
    public void shouldClearExpiryDatesWhenSaveStockCardWithEmptySOH() throws Exception {
        List<InventoryViewModel> inventoryViewModels = getStockCardViewModels();
        InventoryViewModel firstStockCardViewModel = inventoryViewModels.get(0);
        firstStockCardViewModel.setExpiryDates(Arrays.asList("01/01/2016", "02/01/2016"));
        firstStockCardViewModel.setQuantity("0");

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        String sign = "test";
        Observable observable = physicalInventoryPresenter.stockMovementObservable(sign);
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        assertThat(firstStockCardViewModel.getStockCard().getExpireDates(), is(""));
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