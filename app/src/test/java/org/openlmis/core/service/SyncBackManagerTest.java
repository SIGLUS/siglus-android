package org.openlmis.core.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class SyncBackManagerTest {
    private SharedPreferenceMgr sharedPreferenceMgr;
    private LMISRestApi lmisRestApi;
    private RnrFormRepository rnrFormRepository;
    private SyncBackManager syncBackManager;
    private StockMovementItem stockMovementItem;
    private StockRepository stockRepository;

    @Before
    public void setUp() throws Exception {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);//todo: add config file to enable all features for testing and dev

        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        lmisRestApi = mock(LMISRestApi.class);
        rnrFormRepository = mock(RnrFormRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncBackManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncBackManager.class);

        syncBackManager.lmisRestApi = lmisRestApi;

        UserInfoMgr.getInstance().setUser(new User());
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @Test
    public void shouldSyncRequisitionDataSuccess() throws LMISException, SQLException {
        when(sharedPreferenceMgr.getPreference()).thenReturn(LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
        List<RnRForm> data = new ArrayList<>();
        data.add(new RnRForm());
        data.add(new RnRForm());

        SyncDownRequisitionsResponse syncDownRequisitionsResponse = new SyncDownRequisitionsResponse();
        syncDownRequisitionsResponse.setRequisitions(data);
        when(lmisRestApi.fetchRequisitions(anyString())).thenReturn(syncDownRequisitionsResponse);
        syncBackManager.fetchAndSaveRequisitionData();
        verify(rnrFormRepository, times(2)).createFormAndItems(any(RnRForm.class));
    }

    @Test
    public void shouldFetchStockMovement() throws Throwable {

        String facilityId = "HF2";

        User user = new User();
        user.setFacilityId(facilityId);
        UserInfoMgr.getInstance().setUser(user);

        String startDate = "2015-11-04";
        String endDate = "2015-12-04";


        SyncDownStockCardResponse syncDownStockCardResponse = getStockCardResponse();


        StockRepository stockRepository = mock(StockRepository.class);
        syncBackManager.stockRepository = stockRepository;

        when(lmisRestApi.fetchStockMovementData(facilityId, startDate, endDate)).thenReturn(syncDownStockCardResponse);

        syncBackManager.fetchAndSaveStockCards(startDate, endDate);

        verify(stockRepository, times(2)).saveStockCardAndBatchUpdateMovements(any(StockCard.class));
        assertThat(stockMovementItem.isSynced(), is(true));
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
        SharedPreferences createdPreferences = LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);

        when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);
        stockRepository = mock(StockRepository.class);
        syncBackManager.stockRepository = stockRepository;
        when(lmisRestApi.fetchStockMovementData(anyString(), anyString(), anyString())).thenReturn(getStockCardResponse());

        when(stockRepository.list()).thenReturn(newArrayList(new StockCardBuilder().build()));

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();

        syncBackManager.syncBackStockCards(testSubscriber, true);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(lmisRestApi).fetchStockMovementData(anyString(), anyString(), anyString());

        assertFalse(createdPreferences.getBoolean(SharedPreferenceMgr.KEY_INIT_INVENTORY, true));

    }

    @NonNull
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

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }
    }
}

