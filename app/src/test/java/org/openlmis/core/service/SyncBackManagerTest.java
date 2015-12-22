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
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.SyncBackProductsResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.service.SyncBackManager.SyncProgress;
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

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.ProductSynced;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.RequisitionSynced;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.StockCardsLastMonthSynced;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.StockCardsLastYearSynced;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.SyncingProduct;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.SyncingRequisition;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.SyncingStockCardsLastMonth;
import static org.openlmis.core.service.SyncBackManager.SyncProgress.SyncingStockCardsLastYear;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class SyncBackManagerTest {
    private SharedPreferenceMgr sharedPreferenceMgr;
    private LMISRestApi lmisRestApi;
    private RnrFormRepository rnrFormRepository;
    private SyncBackManager syncBackManager;
    private StockMovementItem stockMovementItem;
    private StockRepository stockRepository;
    private ProgramRepository programRepository;
    private SharedPreferences createdPreferences;

    @Before
    public void setUp() throws Exception {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);//todo: add config file to enable all features for testing and dev

        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        lmisRestApi = mock(LMISRestApi.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        programRepository = mock(ProgramRepository.class);
        reset(rnrFormRepository);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncBackManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncBackManager.class);

        syncBackManager.lmisRestApi = lmisRestApi;
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
    public void shouldSyncBackServerData() throws Exception {
        //given
        mockProductResponse();
        mockRequisitionResponse();
        mockStockCardsResponse();

        //when
        SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
        syncBackManager.syncBackServerData(subscriber);
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
    public void shouldSyncProductsWithPrograms() throws Exception {
        //given
        mockProductResponse();

        //when
        TestSubscriber<Void> observer = new TestSubscriber<>();
        syncBackManager.syncProductsWithProgram(observer);
        observer.awaitTerminalEvent();
        observer.assertNoErrors();

        //then
        verify(programRepository).saveProgramWithProduct(any(Program.class));
    }

    private void mockProductResponse() {
        ArrayList<Program> programsWithProducts = new ArrayList<>();
        programsWithProducts.add(new Program());
        SyncBackProductsResponse response = new SyncBackProductsResponse();
        response.setProgramsWithProducts(programsWithProducts);
        when(lmisRestApi.fetchProducts(any(String.class))).thenReturn(response);
    }

    @Test
    public void shouldSyncRequisitionDataSuccess() throws LMISException, SQLException {
        mockRequisitionResponse();

        TestSubscriber<Void> observer = new TestSubscriber<>();
        syncBackManager.syncBackRequisition(observer);
        observer.awaitTerminalEvent();
        observer.assertNoErrors();

        verify(rnrFormRepository, times(2)).createFormAndItems(any(RnRForm.class));
    }

    private void mockRequisitionResponse() {
        when(sharedPreferenceMgr.getPreference()).thenReturn(LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
        List<RnRForm> data = new ArrayList<>();
        data.add(new RnRForm());
        data.add(new RnRForm());

        SyncDownRequisitionsResponse syncDownRequisitionsResponse = new SyncDownRequisitionsResponse();
        syncDownRequisitionsResponse.setRequisitions(data);
        when(lmisRestApi.fetchRequisitions(anyString())).thenReturn(syncDownRequisitionsResponse);
    }

    @Test
    public void shouldFetchLatestYearStockMovement() throws Throwable {
        when(sharedPreferenceMgr.getPreference()).thenReturn(LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
        stockRepository = mock(StockRepository.class);
        syncBackManager.stockRepository = stockRepository;
        when(lmisRestApi.fetchStockMovementData(anyString(), anyString(), anyString())).thenReturn(getStockCardResponse());

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        syncBackManager.syncBackStockCards(testSubscriber, false);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();

        verify(lmisRestApi, times(12)).fetchStockMovementData(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldFetchCurrentMonthStockMovement() throws Throwable {
        mockStockCardsResponse();

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();

        syncBackManager.syncBackStockCards(testSubscriber, true);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verifyLastMonthStockCardsSynced(createdPreferences);
    }

    private void verifyLastMonthStockCardsSynced(SharedPreferences createdPreferences) throws LMISException {
        verify(lmisRestApi).fetchStockMovementData(anyString(), anyString(), anyString());

        assertFalse(createdPreferences.getBoolean(SharedPreferenceMgr.KEY_INIT_INVENTORY, true));
        verify(stockRepository, times(2)).saveStockCardAndBatchUpdateMovements(any(StockCard.class));
        assertThat(stockMovementItem.isSynced(), is(true));
    }

    private void mockStockCardsResponse() throws ParseException, LMISException {
        createdPreferences = LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
        when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);

        stockRepository = mock(StockRepository.class);
        syncBackManager.stockRepository = stockRepository;
        when(lmisRestApi.fetchStockMovementData(anyString(), anyString(), anyString())).thenReturn(getStockCardResponse());

        when(stockRepository.list()).thenReturn(newArrayList(new StockCardBuilder().build()));
    }

    public SyncDownStockCardResponse getStockCardResponse() throws ParseException {
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

    private class SyncServerDataSubscriber extends TestSubscriber<SyncProgress> {
        public List<SyncProgress> syncProgresses = new ArrayList<>();

        @Override
        public void onNext(SyncProgress syncProgress) {
            syncProgresses.add(syncProgress);
            testSyncProgress(syncProgress);
        }
    }

    private void testSyncProgress(SyncProgress progress) {
        try {
            if (progress == ProductSynced) {
                verify(programRepository).saveProgramWithProduct(any(Program.class));
            }
            if (progress == StockCardsLastMonthSynced) {
                verifyLastMonthStockCardsSynced(createdPreferences);
            }
            if (progress == RequisitionSynced) {
                verify(rnrFormRepository, times(2)).createFormAndItems(any(RnRForm.class));
            }
            if (progress == StockCardsLastYearSynced) {
                verify(lmisRestApi, times(13)).fetchStockMovementData(anyString(), anyString(), anyString());
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
            bind(ProgramRepository.class).toInstance(programRepository);
        }
    }
}

