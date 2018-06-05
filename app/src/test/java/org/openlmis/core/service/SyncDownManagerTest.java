package org.openlmis.core.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownProgramDataResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.ProductSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.RapidTestsSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.RequisitionSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.StockCardsLastMonthSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.StockCardsLastYearSynced;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingProduct;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingRapidTests;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingRequisition;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingStockCardsLastMonth;
//import static org.openlmis.core.service.SyncDownManager.SyncProgress.SyncingStockCardsLastYear;
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
    private StockService stockService;

    @Before
    public void setUp() throws Exception {
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        lmisRestApi = mock(LMISRestApi.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        programRepository = mock(ProgramRepository.class);
        productRepository = mock(ProductRepository.class);
        stockRepository = mock(StockRepository.class);
        stockService = mock(StockService.class);

        reset(rnrFormRepository);
        reset(lmisRestApi);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncDownManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncDownManager.class);

        syncDownManager.lmisRestApi = lmisRestApi;
        User user = new User();
        user.setFacilityCode("HF XXX");
        user.setFacilityId("123");
        UserInfoMgr.getInstance().setUser(user);
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        syncDownManager.stockService = stockService;
    }

    @Test
    public void shouldSyncDownServerData() throws Exception {
        //given
        mockSyncDownLatestProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();
        mockRapidTestsResponse();

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
        assertThat(subscriber.syncProgresses.get(6), is(SyncingRapidTests));
        assertThat(subscriber.syncProgresses.get(7), is(RapidTestsSynced));
    }

    @Test
    public void shouldOnlySyncOnceWhenInvokedTwice() throws Exception {
        //given
        mockSyncDownLatestProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();
        mockRapidTestsResponse();

        //when
        CountOnNextSubscriber firstEnterSubscriber = new CountOnNextSubscriber();
        CountOnNextSubscriber laterEnterSubscriber = new CountOnNextSubscriber();
        syncDownManager.syncDownServerData(firstEnterSubscriber);
        syncDownManager.syncDownServerData(laterEnterSubscriber);

        firstEnterSubscriber.awaitTerminalEvent();
        firstEnterSubscriber.assertNoErrors();
        laterEnterSubscriber.assertNoTerminalEvent();

        //then
        assertThat(firstEnterSubscriber.syncProgresses.size(), is(8));
        assertThat(laterEnterSubscriber.syncProgresses.size(), is(0));
    }

    @Test
    public void shouldSyncDownNewLatestProductList() throws Exception {
        mockSyncDownLatestProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();
        mockRapidTestsResponse();
        when(productRepository.getByCode(anyString())).thenReturn(new Product());

        Program program = new Program();
        when(programRepository.queryByCode("PR")).thenReturn(program);

        SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
        syncDownManager.syncDownServerData(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        verify(lmisRestApi).fetchLatestProducts(anyString());
        verify(productRepository).batchCreateOrUpdateProducts(anyList());
        verify(sharedPreferenceMgr).setLastSyncProductTime("today");
    }

    @Test
    public void shouldUpdateNotifyBannerListWhenSOHIsZeroAndProductIsDeActive() throws Exception {
        //given
        Product product = new Product();
        product.setPrimaryName("name");
        product.setActive(false);
        product.setCode("code");
        product.setArchived(false);

        Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(true).setIsArchived(true).build();
        when(productRepository.getByCode(product.getCode())).thenReturn(existingProduct);

        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(0);
        when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

        //when
        syncDownManager.updateDeactivateProductNotifyList(product);

        //then
        verify(sharedPreferenceMgr).setIsNeedShowProductsUpdateBanner(true, "name");
    }

    @Test
    public void shouldNotUpdateNotifyBannerListWhenProductIsArchived() throws Exception {
        //given
        Product product = new Product();
        product.setPrimaryName("name");
        product.setActive(false);
        product.setCode("code");
        product.setArchived(true);

        Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(true).setIsArchived(true).build();
        when(productRepository.getByCode(product.getCode())).thenReturn(existingProduct);

        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(0);
        when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

        //when
        syncDownManager.updateDeactivateProductNotifyList(product);

        //then
        verify(sharedPreferenceMgr, never()).setIsNeedShowProductsUpdateBanner(true, "name");
    }

    @Test
    public void shouldRemoveNotifyBannerListWhenReactiveProduct() throws Exception {
        //given
        Product product = new Product();
        product.setPrimaryName("new name");
        product.setActive(true);
        product.setCode("code");

        Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(false).setPrimaryName("name").build();
        when(productRepository.getByCode(product.getCode())).thenReturn(existingProduct);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(0);
        when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

        //when
        syncDownManager.updateDeactivateProductNotifyList(product);

        //then
        verify(sharedPreferenceMgr).removeShowUpdateBannerTextWhenReactiveProduct("name");
    }

    @Test
    public void shouldNotUpdateBannerWhenExistingProductIsNull() throws Exception {
        Product product = new Product();
        when(productRepository.getByCode(anyString())).thenReturn(null);
        syncDownManager.updateDeactivateProductNotifyList(product);
        verify(stockRepository, times(0)).queryStockCardByProductId(anyLong());
    }

    private void testSyncProgress(SyncProgress progress) throws SQLException {
        try {
            if (progress == StockCardsLastMonthSynced) {
                verifyLastMonthStockCardsSynced();
                verify(sharedPreferenceMgr).setLastMonthStockCardDataSynced(true);
            }
            if (progress == RequisitionSynced) {
                verify(rnrFormRepository, times(1)).createRnRsWithItems(any(ArrayList.class));
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

    private void mockSyncDownLatestProductResponse() throws LMISException {
        List<ProductAndSupportedPrograms> productsAndSupportedPrograms = new ArrayList<>();
        ProductAndSupportedPrograms productAndSupportedPrograms = new ProductAndSupportedPrograms();
        productWithKits = new Product();
        productWithKits.setCode("ABC");
        productAndSupportedPrograms.setProduct(productWithKits);
        ProductProgram productProgram = new ProductProgramBuilder().setProductCode("ABC").setProgramCode("PR").setActive(true).build();
        productAndSupportedPrograms.setProductPrograms(newArrayList(productProgram));
        productsAndSupportedPrograms.add(productAndSupportedPrograms);

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

        when(lmisRestApi.fetchStockMovementData(anyString(), anyString(), anyString())).thenReturn(getStockCardResponse());

        when(stockRepository.list()).thenReturn(newArrayList(new StockCardBuilder().build()));
    }

    private void mockRapidTestsResponse() throws ParseException, LMISException {
        createdPreferences = LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
        when(sharedPreferenceMgr.isRapidTestDataSynced()).thenReturn(false);
        when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);

        when(lmisRestApi.fetchProgramDataForms(anyLong())).thenReturn(getRapidTestsResponse());
    }

    private SyncDownProgramDataResponse getRapidTestsResponse() {
        ProgramDataForm programDataForm1 = new ProgramDataFormBuilder()
                .setProgram(new Program())
                .setPeriod(DateUtil.parseString("2016-03-21", DateUtil.DB_DATE_FORMAT))
                .setStatus(ProgramDataForm.STATUS.AUTHORIZED)
                .setSynced(false).build();
        ProgramDataForm programDataForm2 = new ProgramDataFormBuilder()
                .setProgram(new Program())
                .setPeriod(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT))
                .setStatus(ProgramDataForm.STATUS.AUTHORIZED)
                .setSynced(false).build();

        SyncDownProgramDataResponse syncDownProgramDataResponse = new SyncDownProgramDataResponse();
        syncDownProgramDataResponse.setProgramDataForms(newArrayList(programDataForm1, programDataForm2));
        return syncDownProgramDataResponse;
    }

    private void verifyLastMonthStockCardsSynced() throws LMISException, SQLException {
        verify(lmisRestApi).fetchStockMovementData(anyString(), anyString(), anyString());

        verify(sharedPreferenceMgr).setIsNeedsInventory(false);
        verify(stockRepository).batchCreateSyncDownStockCardsAndMovements(any(List.class));
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
            try {
                testSyncProgress(syncProgress);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            bind(StockRepository.class).toInstance(stockRepository);
        }
    }
}
