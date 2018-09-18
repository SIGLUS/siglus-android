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

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
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
import org.openlmis.core.service.sync.SchedulerBuilder;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

@Singleton
public class SyncDownManager {
    private static final int DAYS_OF_MONTH = 30;
    private static final int MONTHS_OF_YEAR = 12;

    private boolean isSyncing = false;

    protected LMISRestApi lmisRestApi;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;
    @Inject
    RnrFormRepository rnrFormRepository;
    @Inject
    StockRepository stockRepository;
    @Inject
    ProgramRepository programRepository;
    @Inject
    ProductRepository productRepository;
    @Inject
    ProductProgramRepository productProgramRepository;
    @Inject
    ProgramDataFormRepository programDataFormRepository;
    @Inject
    StockService stockService;
    @Inject
    SyncStockCardsLastYearSilently syncStockCardsLastYearSilently;

    public SyncDownManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public void syncDownServerData(Subscriber<SyncProgress> subscriber) {
        if (isSyncing) {
            return;
        }

        isSyncing = true;
        Observable.create(new Observable.OnSubscribe<SyncProgress>() {
            @Override
            public void call(Subscriber<? super SyncProgress> subscriber) {
                try {
                    syncDownProducts(subscriber);
                    syncDownLastMonthStockCards(subscriber);
                    syncDownRequisition(subscriber);
                    syncDownRapidTests(subscriber);
                    isSyncing = false;
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    isSyncing = false;
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    private void syncDownRapidTests(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        if (!sharedPreferenceMgr.isRapidTestDataSynced()) {
            try {
                subscriber.onNext(SyncProgress.SyncingRapidTests);
                fetchAndSaveRapidTests();
                sharedPreferenceMgr.setRapidTestsDataSynced(true);
                subscriber.onNext(SyncProgress.RapidTestsSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setRapidTestsDataSynced(false);
                e.reportToFabric();
                throw new LMISException(errorMessage(R.string.msg_sync_rapid_tests_failed));
            }
        }
    }

    private void fetchAndSaveRapidTests() throws LMISException {
        SyncDownProgramDataResponse syncDownProgramDataResponse = lmisRestApi.fetchProgramDataForms(Long.parseLong(UserInfoMgr.getInstance().getUser().getFacilityId()));

        if (syncDownProgramDataResponse == null) {
            throw new LMISException("Can't get SyncDownRapidTestsResponse, you can check json parse to POJO logic");
        }

        programDataFormRepository.batchSaveForms(syncDownProgramDataResponse.getProgramDataForms());
    }

    public void syncDownServerData() {
        syncDownServerData(new Subscriber<SyncProgress>() {
            @Override
            public void onCompleted() {
                if (sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
                    sendSyncStartBroadcast();
                    sharedPreferenceMgr.setIsSyncingLastYearStockCards(true);
                    syncStockCardsLastYearSilently.performSync().subscribe(getSyncLastYearStockCardSubscriber());
                } else if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
                    sendSyncFinishedBroadcast();
                } else if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && sharedPreferenceMgr.isSyncingLastYearStockCards()) {
                    sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(SyncProgress syncProgress) {
            }
        });
    }

    @NonNull
    private Subscriber<List<StockCard>> getSyncLastYearStockCardSubscriber() {
        return new Subscriber<List<StockCard>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
                sharedPreferenceMgr.setStockCardLastYearSyncError(true);
                sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                new LMISException(e).reportToFabric();
                sendSyncErrorBroadcast();
            }

            @Override
            public void onNext(List<StockCard> stockCards) {
                saveStockCardsFromLastYear(stockCards).subscribe(getSaveStockCardsSubscriber());
            }
        };
    }

    @NonNull
    private Subscriber<Void> getSaveStockCardsSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(false);
                sharedPreferenceMgr.setStockCardLastYearSyncError(false);
                sendSyncFinishedBroadcast();
            }

            @Override
            public void onError(Throwable e) {
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
                sharedPreferenceMgr.setStockCardLastYearSyncError(true);
                sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                sendSyncErrorBroadcast();
            }

            @Override
            public void onNext(Void aVoid) {

            }
        };
    }

    private void sendSyncErrorBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_ERROR_SYNC_DATA);
        LMISApp.getContext().sendBroadcast(intent);
    }

    private void sendSyncStartBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_START_SYNC_DATA);
        LMISApp.getContext().sendBroadcast(intent);
    }

    private void sendSyncFinishedBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
        LMISApp.getContext().sendBroadcast(intent);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
    }

    private void syncDownRequisition(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        if (!sharedPreferenceMgr.isRequisitionDataSynced()) {
            try {
                subscriber.onNext(SyncProgress.SyncingRequisition);
                fetchAndSaveRequisition();
                sharedPreferenceMgr.setRequisitionDataSynced(true);
                subscriber.onNext(SyncProgress.RequisitionSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setRequisitionDataSynced(false);
                e.reportToFabric();
                throw new LMISException(errorMessage(R.string.msg_sync_requisition_failed));
            }
        }
    }

    private void syncDownLastMonthStockCards(Subscriber<? super SyncProgress> subscriber) throws LMISException {

        if (!sharedPreferenceMgr.isLastMonthStockDataSynced()) {
            try {
                subscriber.onNext(SyncProgress.SyncingStockCardsLastMonth);
                fetchLatestOneMonthMovements();
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
                subscriber.onNext(SyncProgress.StockCardsLastMonthSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(false);
                e.reportToFabric();
                throw new LMISException(errorMessage(R.string.msg_sync_stock_movement_failed));
            }
        }
    }

    private void syncDownProducts(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        try {
            subscriber.onNext(SyncProgress.SyncingProduct);
            fetchAndSaveProductsWithProgramsAndKits();
            subscriber.onNext(SyncProgress.ProductSynced);
        } catch (LMISException e) {
            e.reportToFabric();
            throw new LMISException(errorMessage(R.string.msg_sync_products_list_failed));
        }
    }

    private void fetchAndSaveProductsWithProgramsAndKits() throws LMISException {
        SyncDownLatestProductsResponse response = getSyncDownLatestProductResponse();
        List<Product> productList = new ArrayList<>();
        for (ProductAndSupportedPrograms productAndSupportedPrograms : response.getLatestProducts()) {
            Product product = productAndSupportedPrograms.getProduct();
            productProgramRepository.batchSave(productAndSupportedPrograms.getProductPrograms());

            updateDeactivateProductNotifyList(product);
            productList.add(product);
        }
        productRepository.batchCreateOrUpdateProducts(productList);
        sharedPreferenceMgr.setLastSyncProductTime(response.getLatestUpdatedTime());
    }

    protected void updateDeactivateProductNotifyList(Product product) throws LMISException {
        Product existingProduct = productRepository.getByCode(product.getCode());

        if (existingProduct == null) {
            return;
        }

        if (product.isActive() == existingProduct.isActive()) {
            return;
        }
        if (product.isActive()) {
            sharedPreferenceMgr.removeShowUpdateBannerTextWhenReactiveProduct(existingProduct.getPrimaryName());
            return;
        }

        StockCard stockCard = stockRepository.queryStockCardByProductId(existingProduct.getId());
        if (stockCard == null) {
            return;
        }

        if (stockCard.getProduct().isArchived()) {
            return;
        }

        if (stockCard.getStockOnHand() == 0) {
            sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, product.getPrimaryName());
        }
    }

    private SyncDownLatestProductsResponse getSyncDownLatestProductResponse() throws LMISException {
        return lmisRestApi.fetchLatestProducts(sharedPreferenceMgr.getLastSyncProductTime());
    }

    private void fetchAndSaveStockCards(String startDate, String endDate) throws LMISException {
        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();

        SyncDownStockCardResponse syncDownStockCardResponse = lmisRestApi.fetchStockMovementData(facilityId, startDate, endDate);

        try {
            stockRepository.batchCreateSyncDownStockCardsAndMovements(syncDownStockCardResponse.getStockCards());
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }
    }

    public Observable<Void> saveStockCardsFromLastYear(final List<StockCard> stockCards) {

        List<Observable<Void>> observables = new ArrayList<>();
        if(stockCards.isEmpty()){
            return zipObservables(observables);
        }

        Scheduler scheduler = SchedulerBuilder.createScheduler();


        int threadNumber = Runtime.getRuntime().availableProcessors();

        int numberOfElementsInAListForAnObservable = stockCards.size() / threadNumber;
        int startPosition = 0;
        for (int arrayNumber = 1; arrayNumber<=threadNumber; arrayNumber++) {
            int endPosition = arrayNumber==threadNumber ? stockCards.size()-1 : numberOfElementsInAListForAnObservable * arrayNumber;
            observables.add(saveStockCards(stockCards.subList(startPosition, endPosition), scheduler));
            startPosition = endPosition + 1;
        }
        return zipObservables(observables);
    }

    private Observable<Void> zipObservables(List<Observable<Void>> tasks) {
        return Observable.zip(tasks, new FuncN<Void>() {
            @Override
            public Void call(Object... args) {
                return null;
            }
        });
    }


    public Observable<Void> saveStockCards(final List<StockCard> stockCards, Scheduler scheduler) {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    stockRepository.batchCreateSyncDownStockCardsAndMovements(stockCards);
                    stockService.immediatelyUpdateAvgMonthlyConsumption();
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(scheduler);
    }

    private void fetchAndSaveRequisition() throws LMISException {
        SyncDownRequisitionsResponse syncDownRequisitionsResponse = lmisRestApi.fetchRequisitions(UserInfoMgr.getInstance().getUser().getFacilityCode());

        if (syncDownRequisitionsResponse == null) {
            throw new LMISException("Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
        }

        rnrFormRepository.createRnRsWithItems(syncDownRequisitionsResponse.getRequisitions());
    }

    private void fetchLatestOneMonthMovements() throws LMISException {
        Date now = new Date();
        Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
        String startDateStr = DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);

        Date endDate = DateUtil.addDayOfMonth(now, 1);
        String endDateStr = DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);
        fetchAndSaveStockCards(startDateStr, endDateStr);

        List<StockCard> syncedStockCard = stockRepository.list();
        if (!(syncedStockCard == null || syncedStockCard.isEmpty())) {
            sharedPreferenceMgr.setIsNeedsInventory(false);
        }
    }

    private String errorMessage(int code) {
        return LMISApp.getContext().getResources().getString(code);
    }

    public enum SyncLocalUserProgress {
        SyncLastSyncProductFail,
        SyncLastMonthStockDataFail,
        SyncRequisitionDataFail,
        SyncLastDataSuccess
    }

    public enum SyncProgress {
        SyncingProduct(R.string.msg_fetching_products),
        SyncingStockCardsLastMonth(R.string.msg_sync_stock_movements_data),
        SyncingRequisition(R.string.msg_sync_requisition_data),
        SyncingRapidTests,

        ProductSynced,
        StockCardsLastMonthSynced,
        RequisitionSynced,
        StockCardsLastYearSynced,
        RapidTestsSynced;

        private int messageCode;

        SyncProgress(int messageCode) {
            this.messageCode = messageCode;
        }

        SyncProgress() {
        }

        public int getMessageCode() {
            return messageCode;
        }
    }
}
