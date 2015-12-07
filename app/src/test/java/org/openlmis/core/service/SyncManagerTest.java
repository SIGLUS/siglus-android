/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.service;


import android.content.Context;
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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.StockCardResponse;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.network.model.SubmitRequisitionResponse;
import org.openlmis.core.network.model.SyncBackRequisitionsResponse;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class SyncManagerTest {

    SyncManager syncManager;
    RnrFormRepository rnrFormRepository;
    LMISRestApi lmisRestApi;
    StockRepository stockRepository;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private StockMovementItem stockMovementItem;

    @Before
    public void setup() throws LMISException {
        rnrFormRepository = mock(RnrFormRepository.class);
        lmisRestApi = mock(LMISRestApi.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        syncManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncManager.class);
        syncManager.lmisRestApi = lmisRestApi;

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);

        User user = new User("user", "123");
        user.setFacilityCode("FC1");
        user.setFacilityId("123");
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
    public void shouldSubmitAllUnsyncedRequisitions() throws LMISException, SQLException {
        List<RnRForm> unSyncedList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RnRForm form = new RnRForm();
            unSyncedList.add(form);
        }

        when(rnrFormRepository.listUnSynced()).thenReturn(unSyncedList);

        SubmitRequisitionResponse response = new SubmitRequisitionResponse();
        response.setRequisitionId("1");
        when(lmisRestApi.submitRequisition(any(RnRForm.class))).thenReturn(response);

        syncManager.syncRnr();
        verify(lmisRestApi, times(10)).submitRequisition(any(RnRForm.class));
        verify(rnrFormRepository, times(10)).save(any(RnRForm.class));

    }


    @Test
    public void shouldPushUnSyncedStockMovementData() throws LMISException, SQLException, ParseException {
        StockCard stockCard = createTestStockCardData();

        doReturn(null).when(lmisRestApi).pushStockMovementData(anyString(), anyList());
        syncManager.syncStockCards();
        stockRepository.refresh(stockCard);
        List<StockMovementItem> items = newArrayList(stockCard.getForeignStockMovementItems());

        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(true));
        assertThat(items.get(1).isSynced(), is(true));
    }

    @Test
    public void shouldNotMarkAsSyncedWhenStockMovementSyncFailed() throws LMISException, ParseException {
        StockCard stockCard = createTestStockCardData();

        doThrow(new RuntimeException("Sync Failed")).when(lmisRestApi).pushStockMovementData(anyString(), anyList());

        try {
            syncManager.syncStockCards();
        } catch (RuntimeException e) {
        }
        stockRepository.refresh(stockCard);
        List<StockMovementItem> items = newArrayList(stockCard.getForeignStockMovementItems());

        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(false));
        assertThat(items.get(1).isSynced(), is(false));
    }

    @NonNull
    private StockCard createTestStockCardData() throws LMISException, ParseException {
        ProductRepository productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        StockCard stockCard = StockCardBuilder.saveStockCardWithOneMovement(stockRepository);

        Product product = new Product();
        product.setCode("PD1");
        productRepository.create(product);

        stockCard.setProduct(product);
        stockRepository.update(stockCard);

        //ready to sync
        StockMovementItem item = new StockMovementItem();
        item.setMovementQuantity(100L);
        item.setStockOnHand(-1);
        item.setMovementDate(DateUtil.today());
        item.setMovementType(StockMovementItem.MovementType.RECEIVE);
        item.setStockCard(stockCard);
        item.setSynced(false);

        stockRepository.addStockMovementAndUpdateStockCard(stockCard, item);
        stockRepository.refresh(stockCard);
        return stockCard;
    }

    @Test
    public void shouldTestObservable() {
        TestSubscriber testSubscriber = new TestSubscriber();
      syncManager.fetchStockCardsData(testSubscriber,true);

        testSubscriber.awaitTerminalEvent();

        //xxxx assert
    }

    @Test
    public void shouldSetTypeAndCustomPropsAfterNewStockMovementEntry() throws LMISException, ParseException {
        StockCard stockCard = createTestStockCardData();
        StockMovementItem stockMovementItem = stockCard.getForeignStockMovementItems().iterator().next();
        StockMovementEntry stockMovementEntry = new StockMovementEntry(stockMovementItem, null);

        assertEquals(stockMovementEntry.getType(), "ADJUSTMENT");
        assertEquals(stockMovementEntry.getCustomProps().get("expirationDates"), stockMovementItem.getStockCard().getExpireDates());
    }

    @Test
    public void shouldSyncRequisitionDataSuccess() throws LMISException, SQLException {
        when(sharedPreferenceMgr.getPreference()).thenReturn(LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
        List<RnRForm> data = new ArrayList<>();
        data.add(new RnRForm());
        data.add(new RnRForm());

        SyncBackRequisitionsResponse syncBackRequisitionsResponse = new SyncBackRequisitionsResponse();
        syncBackRequisitionsResponse.setRequisitions(data);
        when(lmisRestApi.fetchRequisitions(anyString())).thenReturn(syncBackRequisitionsResponse);
        syncManager.fetchAndSaveRequisitionData();
        verify(rnrFormRepository, times(2)).createFormAndItems(any(RnRForm.class));
    }

    @Test
    public void shouldSyncAppVersion() throws Exception {
        when(sharedPreferenceMgr.hasSyncedVersion()).thenReturn(false);
        User user = new User();
        UserInfoMgr.getInstance().setUser(user);
        syncManager.syncAppVersion();
        verify(lmisRestApi).updateAppVersion(any(AppInfoRequest.class), any(Callback.class));
    }

    @Test
    public void shouldNotSyncAppVersion() throws Exception {
        when(sharedPreferenceMgr.hasSyncedVersion()).thenReturn(true);
        syncManager.syncAppVersion();
        verify(lmisRestApi, never()).updateAppVersion(any(AppInfoRequest.class), any(Callback.class));
    }

    @Test
    public void shouldFetchStockMovement() throws Throwable{

        String facilityId = "HF2";

        User user = new User();
        user.setFacilityId(facilityId);
        UserInfoMgr.getInstance().setUser(user);

        String startDate = "2015-11-04";
        String endDate = "2015-12-04";


        StockCardResponse stockCardResponse = getStockCardResponse();


        StockRepository stockRepository = mock(StockRepository.class);
        syncManager.stockRepository = stockRepository;

        when(lmisRestApi.fetchStockMovementData(facilityId, startDate, endDate)).thenReturn(stockCardResponse);

        syncManager.fetchAndSaveStockCards(startDate, endDate);

        verify(stockRepository, times(2)).save(any(StockCard.class));
        verify(stockRepository, times(5)).saveStockItem(stockMovementItem);
        assertThat(stockMovementItem.isSynced(), is(true));
    }

    @NonNull
    public StockCardResponse getStockCardResponse() throws ParseException {
        StockCard stockCard1= StockCardBuilder.buildStockCard();
        StockCard stockCard2= StockCardBuilder.buildStockCard();

        StockMovementItemBuilder builder = new StockMovementItemBuilder();

        stockMovementItem = builder.build();
        stockMovementItem.setSynced(false);

        ArrayList<StockMovementItem> stockMovementItems1 = newArrayList(stockMovementItem, stockMovementItem, stockMovementItem);
        stockCard1.setStockMovementItemsWrapper(stockMovementItems1);

        ArrayList<StockMovementItem> stockMovementItems2 = newArrayList(stockMovementItem, stockMovementItem);
        stockCard2.setStockMovementItemsWrapper(stockMovementItems2);

        StockCardResponse stockCardResponse = new StockCardResponse();
        stockCardResponse.setStockCards(newArrayList(stockCard1, stockCard2));
        return stockCardResponse;
    }

    @Test
    public void shouldFetchLatestYearStockMovement() throws Throwable{

        StockRepository stockRepository = mock(StockRepository.class);
        syncManager.stockRepository = stockRepository;
        when(lmisRestApi.fetchStockMovementData(anyString(), anyString(), anyString())).thenReturn(getStockCardResponse());

        syncManager.fetchLatestYearStockMovements();

        verify(lmisRestApi,times(12)).fetchStockMovementData(anyString(), anyString(), anyString());
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            binder.bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }
    }
}
