package org.openlmis.core.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownProductsResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.ProductSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.RequisitionSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.StockCardsLastMonthSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.StockCardsLastYearSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingProduct;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingRequisition;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingStockCardsLastMonth;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingStockCardsLastYear;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class SyncDownManagerTest {
    private SyncDownManager syncDownManager;

    private LMISRestApi lmisRestApi;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private StockMovementItem stockMovementItem;

    private ProgramRepository programRepository;
    private RnrFormRepository rnrFormRepository;
    private StockRepository stockRepository;
    private SharedPreferences createdPreferences;

    private ProductRepository productRepository;
    private Product productWithKits;
    private Product newProductWithoutPrograms;

    @Before
    public void setUp() throws Exception {
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        lmisRestApi = mock(LMISRestApi.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        programRepository = mock(ProgramRepository.class);
        productRepository = mock(ProductRepository.class);

        reset(rnrFormRepository);
        reset(lmisRestApi);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncDownManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncDownManager.class);

        syncDownManager.lmisRestApi = lmisRestApi;
        User user = new User();
        user.setFacilityCode("HF XXX");
        UserInfoMgr.getInstance().setUser(user);
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @Test
    public void shouldSyncDownServerData() throws Exception {
        //given
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_latest_product_list, false);
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_stock_movement_273, true);
        mockProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();

        //when
        SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
        syncDownManager.syncDownServerData(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        //then
        assertThat(subscriber.syncProgresses.get(0), is(SyncingProduct));
        assertThat(subscriber.syncProgresses.get(1), is(ProductSynced));
        assertThat(subscriber.syncProgresses.get(2), is(SyncingStockCardsLastMonth));
        assertThat(subscriber.syncProgresses.get(3), is(StockCardsLastMonthSynced));
        assertThat(subscriber.syncProgresses.get(4), is(SyncingRequisition));
        assertThat(subscriber.syncProgresses.get(5), is(RequisitionSynced));
        assertThat(subscriber.syncProgresses.get(6), is(SyncingStockCardsLastYear));
        assertThat(subscriber.syncProgresses.get(7), is(StockCardsLastYearSynced));
    }

    @Test
    public void shouldOnlySyncOnceWhenInvokedTwice() throws Exception {
        //given
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_latest_product_list, false);
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_stock_movement_273, true);
        mockProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();

        //when
        CountOnNextSubscriber firstEnterSubscriber = new CountOnNextSubscriber();
        CountOnNextSubscriber laterEnterSubscriber = new CountOnNextSubscriber();
        syncDownManager.syncDownServerData(firstEnterSubscriber);
        syncDownManager.syncDownServerData(laterEnterSubscriber);

        firstEnterSubscriber.awaitTerminalEvent();
        firstEnterSubscriber.assertNoErrors();
        laterEnterSubscriber.assertNoTerminalEvent();

        //then
        verify(lmisRestApi, times(1)).fetchProducts(anyString());
        assertThat(firstEnterSubscriber.syncProgresses.size(), is(8));
        assertThat(laterEnterSubscriber.syncProgresses.size(), is(0));
    }

    @Test
    public void shouldSyncDownLatestProductList() throws Exception {
        //given
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_latest_product_list, true);
        mockProductResponse();

        //when
        syncDownManager.syncDownLatestProducts();

        //then
        verify(lmisRestApi).fetchLatestProducts(anyString(), anyString());
        verify(programRepository).createOrUpdateProgramWithProduct(anyList());
        verify(sharedPreferenceMgr).setLastSyncProductTime("today");
    }

    @Test
    public void shouldSyncDownNewLatestProductList() throws Exception {

        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_latest_product_list, true);
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_sync_back_stock_movement_273, true);
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_kit, true);
        mockSyncDownLatestProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();
        when(productRepository.getByCode(anyString())).thenReturn(new Product());

        Program program = new Program();
        when(programRepository.queryByCode("PR")).thenReturn(program);
        productWithKits.setProgram(program);

        SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
        syncDownManager.syncDownServerData(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        verify(lmisRestApi).fetchLatestProducts(anyString());
        verify(productRepository).batchCreateOrUpdateProducts(anyList());
        verify(sharedPreferenceMgr).setLastSyncProductTime("today");
    }

    private void testSyncProgress(SyncProgress progress) {
        try {
            if (progress == ProductSynced) {
                if (!(LMISTestApp.getInstance()).getFeatureToggleFor(R.bool.feature_kit)) {
                    verify(programRepository).createOrUpdateProgramWithProduct(any(ArrayList.class));
                }
                verify(sharedPreferenceMgr).setHasGetProducts(true);
            }
            if (progress == StockCardsLastMonthSynced) {
                verifyLastMonthStockCardsSynced();
                verify(sharedPreferenceMgr).setLastMonthStockCardDataSynced(true);
            }
            if (progress == RequisitionSynced) {
                verify(rnrFormRepository, times(1)).createFormAndItems(any(ArrayList.class));
                verify(sharedPreferenceMgr).setRequisitionDataSynced(true);
            }
            if (progress == StockCardsLastYearSynced) {
                verify(lmisRestApi, times(13)).fetchStockMovementData(anyString(), anyString(), anyString());
                verify(sharedPreferenceMgr).setShouldSyncLastYearStockCardData(false);
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    private void mockProductResponse() throws LMISException {
        ArrayList<Program> programsWithProducts = new ArrayList<>();
        Program program = new Program();
        program.setProducts(new ArrayList<Product>());
        programsWithProducts.add(program);
        SyncDownProductsResponse response = new SyncDownProductsResponse();
        response.setLatestUpdatedTime("today");
        response.setProgramsWithProducts(programsWithProducts);
        when(lmisRestApi.fetchProducts(any(String.class))).thenReturn(response);
        when(lmisRestApi.fetchLatestProducts(any(String.class), any(String.class))).thenReturn(response);
    }

    private void mockSyncDownLatestProductResponse() throws LMISException {
        List<ProductAndSupportedPrograms> productsAndSupportedPrograms = new ArrayList<>();
        ProductAndSupportedPrograms productAndSupportedPrograms = new ProductAndSupportedPrograms();
        productWithKits = new Product();
        productWithKits.setCode("ABC");
        productAndSupportedPrograms.setProduct(productWithKits);
        productAndSupportedPrograms.setSupportedPrograms(newArrayList("PR"));
        productsAndSupportedPrograms.add(productAndSupportedPrograms);

        ProductAndSupportedPrograms productWithoutSupportedPrograms = new ProductAndSupportedPrograms();
        newProductWithoutPrograms = new Product();
        newProductWithoutPrograms.setCode("ABCD");
        productWithoutSupportedPrograms.setProduct(newProductWithoutPrograms);
        productWithoutSupportedPrograms.setSupportedPrograms(null);

        productsAndSupportedPrograms.add(productWithoutSupportedPrograms);

        SyncDownLatestProductsResponse response = new SyncDownLatestProductsResponse();
        response.setLatestUpdatedTime("today");
        response.setLatestProducts(productsAndSupportedPrograms);
        when(lmisRestApi.fetchLatestProducts(any(String.class))).thenReturn(response);
    }

    private void mockRequisitionResponse() throws LMISException {
        when(sharedPreferenceMgr.getPreference()).thenReturn(LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
        List<RnRForm> data = new ArrayList<>();
        data.add(new RnRForm());
        data.add(new RnRForm());

        SyncDownRequisitionsResponse syncDownRequisitionsResponse = new SyncDownRequisitionsResponse();
        syncDownRequisitionsResponse.setRequisitions(data);
        when(lmisRestApi.fetchRequisitions(anyString())).thenReturn(syncDownRequisitionsResponse);
    }

    private void mockStockCardsResponse() throws ParseException, LMISException {
        createdPreferences = LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
        when(sharedPreferenceMgr.shouldSyncLastYearStockData()).thenReturn(true);
        when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);

        stockRepository = mock(StockRepository.class);
        syncDownManager.stockRepository = stockRepository;
        when(lmisRestApi.fetchStockMovementData(anyString(), anyString(), anyString())).thenReturn(getStockCardResponse());

        when(stockRepository.list()).thenReturn(newArrayList(new StockCardBuilder().build()));
    }

    private void verifyLastMonthStockCardsSynced() throws LMISException {
        verify(lmisRestApi).fetchStockMovementData(anyString(), anyString(), anyString());

        verify(sharedPreferenceMgr).setIsNeedsInventory(false);
        verify(stockRepository, times(2)).saveStockCardAndBatchUpdateMovements(any(StockCard.class));
    }

    private SyncDownStockCardResponse getStockCardResponse() throws ParseException {
        StockCard stockCard1 = StockCardBuilder.buildStockCard();
        StockCard stockCard2 = StockCardBuilder.buildStockCard();

        StockMovementItemBuilder builder = new StockMovementItemBuilder();

        stockMovementItem = builder.build();
        stockMovementItem.setSynced(false);

        ArrayList<StockMovementItem> stockMovementItems1 = newArrayList(stockMovementItem, stockMovementItem, stockMovementItem);
        stockCard1.setStockMovementItemsWrapper(stockMovementItems1);

        ArrayList<StockMovementItem> stockMovementItems2 = newArrayList(stockMovementItem, stockMovementItem);
        stockCard2.setStockMovementItemsWrapper(stockMovementItems2);

        SyncDownStockCardResponse syncDownStockCardResponse = new SyncDownStockCardResponse();
        syncDownStockCardResponse.setStockCards(newArrayList(stockCard1, stockCard2));
        return syncDownStockCardResponse;
    }

    private class SyncServerDataSubscriber extends CountOnNextSubscriber {
        @Override
        public void onNext(SyncProgress syncProgress) {
            super.onNext(syncProgress);
            testSyncProgress(syncProgress);
        }
    }

    private class CountOnNextSubscriber extends TestSubscriber<SyncProgress> {
        public List<SyncProgress> syncProgresses = new ArrayList<>();

        @Override
        public void onNext(SyncProgress syncProgress) {
            syncProgresses.add(syncProgress);
        }
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
            bind(ProgramRepository.class).toInstance(programRepository);
            bind(ProductRepository.class).toInstance(productRepository);
        }
    }
}
