package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

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
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private PhysicalInventoryPresenter presenter;

    StockRepository stockRepositoryMock;
    ProductRepository productRepositoryMock;
    StockMovementRepository stockMovementRepositoryMock;
    InventoryPresenter.InventoryView view;
    private Product product;
    private StockCard stockCardWithIdNine;
    private StockCard stockCardWithIdThree;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private InventoryRepository mockInventoryRepository;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);
        productRepositoryMock = mock(ProductRepository.class);
        stockMovementRepositoryMock = mock(StockMovementRepository.class);
        mockInventoryRepository = mock(InventoryRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        view = mock(InventoryPresenter.InventoryView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PhysicalInventoryPresenter.class);
        presenter.attachView(view);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setCode("ABC");

        stockCardWithIdNine = buildDefaultStockCard(9);
        stockCardWithIdThree = buildDefaultStockCard(3);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    private StockCard buildDefaultStockCard(long stockID) {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100);
        stockCard.setId(stockID);
        stockCard.setProduct(product);
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
        Observable<List<InventoryViewModel>> observable = presenter.loadInventory();
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
        Observable<List<InventoryViewModel>> observable = presenter.loadInventory();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
        assertEquals(1, receivedInventoryViewModels.size());
        assertEquals("VIA Product", receivedInventoryViewModels.get(0).getProductName());
    }

    @Test
    public void shouldMakePositiveAdjustment() throws LMISException {

        InventoryViewModel model = new InventoryViewModel(stockCardWithIdNine);
        model.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2015").setQuantity("120").build());

        StockMovementItem item = presenter.calculateAdjustment(model, stockCardWithIdNine);

        assertThat(item.getMovementType(), is(MovementReasonManager.MovementType.POSITIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
        assertThat(item.getStockOnHand(), is(120L));
        assertThat(item.getStockCard(), is(stockCardWithIdNine));
    }

    @Test
    public void shouldMakeNegativeAdjustment() throws LMISException {
        InventoryViewModel model = new InventoryViewModel(stockCardWithIdNine);
        model.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2015").setQuantity("80").build());

        StockMovementItem item = presenter.calculateAdjustment(model, stockCardWithIdNine);

        assertThat(item.getMovementType(), is(MovementReasonManager.MovementType.NEGATIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
        assertThat(item.getStockOnHand(), is(80L));
        assertThat(item.getStockCard(), is(stockCardWithIdNine));
    }


    @Test
    public void shouldCalculateStockAdjustment() throws LMISException {
        InventoryViewModel model = new InventoryViewModel(stockCardWithIdNine);
        model.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2015").setQuantity("100").build());

        StockMovementItem item = presenter.calculateAdjustment(model, stockCardWithIdNine);

        assertThat(item.getMovementType(), is(MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
        assertThat(item.getMovementQuantity(), is(0L));
        assertThat(item.getStockOnHand(), is(100L));
        assertThat(item.getStockCard(), is(stockCardWithIdNine));
    }

    @Test
    public void shouldRestoreDraftInventoryWithLDraftLotItems() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();

        ArrayList<DraftInventory> draftInventories = new ArrayList<>();
        DraftInventory draftInventory = new DraftInventory();
//        stockCard.setId(9);
        draftInventory.setStockCard(stockCardWithIdNine);
        draftInventory.setQuantity(20L);
        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("test").setExpiryDate("Sep 2016").setQuantity("10").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("testNew").setExpiryDate("Sep 2016").setQuantity("10").build();
        inventoryViewModels.get(0).setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        DraftLotItem draftLotItem1 = new DraftLotItem(lotMovementViewModel1, stockCardWithIdNine.getProduct(), false);
        DraftLotItem draftLotItem2 = new DraftLotItem(lotMovementViewModel2, stockCardWithIdNine.getProduct(), true);
        draftInventory.setDraftLotItemListWrapper(newArrayList(draftLotItem1, draftLotItem2));
        draftInventories.add(draftInventory);
        when(mockInventoryRepository.queryAllDraft()).thenReturn(draftInventories);

        presenter.getInventoryViewModelList().addAll(inventoryViewModels);
        presenter.restoreDraftInventory();
        assertThat(inventoryViewModels.get(0).getNewLotMovementViewModelList().get(0).getLotNumber(), is("testNew"));
        assertThat(inventoryViewModels.get(0).getExistingLotMovementViewModelList().get(0).getLotNumber(), is("test"));
    }

    private ArrayList<InventoryViewModel> getStockCardViewModels() {
        ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        inventoryViewModels.add(buildInventoryViewModelWithOutDraft(9, "11", null));
        InventoryViewModel inventoryViewModelWithOutDraft = buildInventoryViewModelWithOutDraft(3, "15", "11/02/2015");
        inventoryViewModels.add(inventoryViewModelWithOutDraft);
        return inventoryViewModels;
    }

    private ArrayList<StockMovementItem> getInversionDateOrTimeStockMovementItem() {
        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();
        StockMovementItem item = new StockMovementItem();
        item.setStockCard(stockCardWithIdThree);
        item.setCreatedTime(DateUtil.addDayOfMonth(new Date(), 4));

        StockMovementItem item1 = new StockMovementItem();
        item1.setStockCard(stockCardWithIdNine);
        item1.setCreatedTime(DateUtil.addDayOfMonth(new Date(), 4));
        stockMovementItems.add(item);
        stockMovementItems.add(item1);

        return stockMovementItems;
    }

    private ArrayList<StockMovementItem> getNormalDateOrTimeStockMovementItem() {
        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();
        StockMovementItem item = new StockMovementItem();
        item.setStockCard(stockCardWithIdThree);
        item.setCreatedTime(new Date());

        StockMovementItem item1 = new StockMovementItem();
        item1.setStockCard(stockCardWithIdNine);
        item1.setCreatedTime(new Date());
        stockMovementItems.add(item);
        stockMovementItems.add(item1);

        return stockMovementItems;
    }

    @NonNull
    private InventoryViewModel buildInventoryViewModelWithOutDraft(int stockCardId, String quantity, String expireDate) {
        InventoryViewModel inventoryViewModelWithOutDraft = new PhysicalInventoryViewModel(buildDefaultStockCard(6));
        inventoryViewModelWithOutDraft.setStockCardId(stockCardId);
        return inventoryViewModelWithOutDraft;
    }

    @Test
    public void shouldSetSignatureToViewModel() throws Exception {
        String signature = "signature";
        presenter.getInventoryViewModelList().addAll(getStockCardViewModels());
        when(stockMovementRepositoryMock.queryEachStockCardNewestMovement()).thenReturn(getNormalDateOrTimeStockMovementItem());
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        Subscription subscription = presenter.doInventory(signature).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        presenter.subscriptions.add(subscription);

        assertThat(presenter.getInventoryViewModelList().get(0).getSignature(), is(signature));
        assertThat(presenter.getInventoryViewModelList().get(1).getSignature(), is(signature));
    }

    @Test
    public void shouldPreventInversionDateOrTimeInsert() {
        String signature = "signature";
        presenter.getInventoryViewModelList().addAll(getStockCardViewModels());
        when(stockMovementRepositoryMock.queryEachStockCardNewestMovement()).thenReturn(getInversionDateOrTimeStockMovementItem());
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        Subscription subscription = presenter.doInventory(signature).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        presenter.subscriptions.add(subscription);

        assertThat(subscriber.getOnErrorEvents().get(0).getMessage(), is(LMISTestApp.getContext().getResources().getString(R.string.msg_invalid_stock_movement)));
    }

    @Test
    public void shouldUpdateLatestDoPhysicalInventoryTime() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getStockCardViewModels();

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        String sign = "test";
        presenter.getInventoryViewModelList().clear();
        presenter.getInventoryViewModelList().addAll(inventoryViewModels);
        Observable observable = presenter.doInventory(sign);
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        verify(sharedPreferenceMgr).setLatestPhysicInventoryTime(anyString());
    }

    @Test
    public void shouldSaveInventoryWhenCompletePhysicalInventory() throws Exception {

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        presenter.getInventoryViewModelList().clear();
        presenter.getInventoryViewModelList().addAll(getStockCardViewModels());
        String sign = "signature";
        Observable observable = presenter.doInventory(sign);
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        verify(mockInventoryRepository).save(any(Inventory.class));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
            bind(ProductRepository.class).toInstance(productRepositoryMock);
            bind(StockMovementRepository.class).toInstance(stockMovementRepositoryMock);
            bind(InventoryRepository.class).toInstance(mockInventoryRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }
    }

    @Test
    public void shouldGetCompleteCount() throws Exception {
        PhysicalInventoryViewModel inventoryViewModel = new PhysicalInventoryViewModel(stockCardWithIdThree);
        inventoryViewModel.setDone(true);
        presenter.inventoryViewModelList.add(inventoryViewModel);
        presenter.inventoryViewModelList.add(new PhysicalInventoryViewModel(stockCardWithIdThree));
        presenter.inventoryViewModelList.add(new PhysicalInventoryViewModel(stockCardWithIdThree));
        presenter.inventoryViewModelList.add(new PhysicalInventoryViewModel(stockCardWithIdThree));

        assertEquals(1, presenter.getCompleteCount());
    }
}