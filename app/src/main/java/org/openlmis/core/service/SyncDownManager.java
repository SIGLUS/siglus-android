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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

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
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.ServiceFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SyncDownKitChangeDraftProductsResponse;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownReportTypeResponse;
import org.openlmis.core.network.model.SyncDownProgramDataResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownServiceResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.network.model.SyncUpProgramResponse;
import org.openlmis.core.service.sync.SchedulerBuilder;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class SyncDownManager {
    private static final int DAYS_OF_MONTH = 30;
    private static final int MONTHS_OF_YEAR = 12;
    private static final String TAG = SyncDownManager.class.getSimpleName();

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
    ReportTypeFormRepository reportTypeFormRepository;
    @Inject
    ServiceFormRepository serviceFormRepository;
    @Inject
    StockService stockService;
    @Inject
    SyncStockCardsLastYearSilently syncStockCardsLastYearSilently;
    @Inject
    DirtyDataManager dirtyDataManager;

    public SyncDownManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public void syncDownServerData(Subscriber<SyncProgress> subscriber) {
        if (isSyncing) {
            return;
        }

        isSyncing = true;
        Observable.create((Observable.OnSubscribe<SyncProgress>) subscriber1 -> {
            try {
                SyncDownPrograms(subscriber1);
                syncDownService(subscriber1);
                syncDownReportType(subscriber1);
                syncDownProducts(subscriber1);
                syncDownLastMonthStockCards(subscriber1);
                syncDownRequisition(subscriber1);
                syncDownRapidTests(subscriber1);
                isSyncing = false;
                subscriber1.onCompleted();
            } catch (LMISException e) {
                isSyncing = false;
                subscriber1.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }


    private void syncDownReportType(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        try {
            subscriber.onNext(SyncProgress.SyncingReportType);
            fetchAndSaveReportType();
            subscriber.onNext(SyncProgress.ReportTypeSynced);
        } catch (LMISException e) {
            LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_report_type_failed));
            e1.reportToFabric();
            throw e1;
        }
    }

    private void SyncDownPrograms(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        try {
            subscriber.onNext(SyncProgress.SyncingPrograms);
            fetchAndSaveprogram();
            subscriber.onNext(SyncProgress.ProgramSynced);
        } catch (LMISException e) {
//            LMISException e1 = new LMISException(errorMessage(R.string.msg_sync_program_failed));
//            e1.reportToFabric();
            throw e;
        }
    }

    private void fetchAndSaveprogram() throws LMISException {
        SyncUpProgramResponse response = lmisRestApi.fetchPrograms(Long.parseLong(UserInfoMgr.getInstance().getUser().getFacilityId()));
        programRepository.updateProgramWithRegimen(response.getPrograms());
    }

    private void fetchAndSaveReportType() throws LMISException {
        SyncDownReportTypeResponse response = lmisRestApi.fetchReportTypeForms(Long.parseLong(UserInfoMgr.getInstance().getUser().getFacilityId()));
        sharedPreferenceMgr.setReportTypesData(response.getReportTypes());
        reportTypeFormRepository.batchCreateOrUpdateReportTypes(response.getReportTypes());
    }

    private void syncDownService(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        try {
            subscriber.onNext(SyncProgress.SyncingServiceList);
            fetchAndSaveService();
            subscriber.onNext(SyncProgress.ServiceSynced);
        } catch (LMISException e) {
            LMISException e1 = new LMISException(errorMessage(R.string.msg_service_lists));
            e1.reportToFabric();
            throw e1;
        }
    }

    private void fetchAndSaveService() throws LMISException {
        SyncDownServiceResponse response = lmisRestApi
                .fetchPTVService(sharedPreferenceMgr.getLastSyncServiceTime(),
                        Constants.PTV_PROGRAM_CODE);
        serviceFormRepository.batchCreateOrUpdateServiceList(response.getLatestServices());
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
                LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_rapid_tests_failed));
                e1.reportToFabric();
                throw e1;
            }
        }
    }

    private void fetchAndSaveRapidTests() throws LMISException {
        SyncDownProgramDataResponse syncDownProgramDataResponse = lmisRestApi.fetchProgramDataForms(Long.parseLong(UserInfoMgr.getInstance().getUser().getFacilityId()));
        if (syncDownProgramDataResponse == null) {
            LMISException e = new LMISException("Can't get SyncDownRapidTestsResponse, you can check json parse to POJO logic");
            e.reportToFabric();
            throw e;
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
                    syncChangeKit();
                    if (TextUtils.isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
                        sendSyncFinishedBroadcast();
                    }
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

    private void syncChangeKit() {
        Observable.create((Observable.OnSubscribe<SyncProgress>) subscriber -> {
            try {
                Log.d(TAG, " sync the kit change.");
                fetchKitChangeProduct();
                subscriber.onNext(SyncProgress.ShouldGoToInitialInventory);
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.newThread()).subscribeOn(Schedulers.io()).subscribe();
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
                new LMISException(e, "getSyncLastYearStockCardSubscriber:onError").reportToFabric();
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
        LocalBroadcastManager.getInstance(LMISApp.getContext()).sendBroadcast(intent);
    }

    private void sendSyncStartBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_START_SYNC_DATA);
        LocalBroadcastManager.getInstance(LMISApp.getContext()).sendBroadcast(intent);
    }

    private void sendSyncFinishedBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
        LocalBroadcastManager.getInstance(LMISApp.getContext()).sendBroadcast(intent);
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
                LMISException e1 = new LMISException(errorMessage(R.string.msg_sync_requisition_failed));
                e1.reportToFabric();
                throw e1;
            }
        }
    }

    private void syncDownLastMonthStockCards(Subscriber<? super SyncProgress> subscriber) throws LMISException {

        if (!sharedPreferenceMgr.isLastMonthStockDataSynced()) {
            try {
                // 1 re-sync && if(stockRepository.list()!=null) do not goto initial inventory
                // 2 initial inventory
                subscriber.onNext(SyncProgress.SyncingStockCardsLastMonth);
                fetchLatestOneMonthMovements();
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
                // in fetchLatestOneMonthMovements() will set isNeedsInventory to false, default is true
                if (sharedPreferenceMgr.isNeedsInventory()) {
                    fetchKitChangeProduct();
                }
                subscriber.onNext(SyncProgress.StockCardsLastMonthSynced);

            } catch (LMISException e) {
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(false);
                e.printStackTrace();
                LMISException e1 = new LMISException(errorMessage(R.string.msg_sync_stock_movement_failed));
                e1.reportToFabric();
                throw e1;
            }
        } else {
            dirtyDataManager.initialDirtyDataCheck();
        }
    }

    private void syncDownProducts(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        try {
            subscriber.onNext(SyncProgress.SyncingProduct);
            fetchAndSaveProductsWithProgramsAndKits();
            subscriber.onNext(SyncProgress.ProductSynced);
        } catch (LMISException e) {
            LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_products_list_failed));
            e1.reportToFabric();
            throw e1;
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

    public void fetchKitChangeProduct() throws LMISException {
        SyncDownKitChangeDraftProductsResponse responseAgain = getSyncDownKitChangeProductResponse();
        List<Product> productList = new ArrayList<>();
        for (ProductAndSupportedPrograms productAndSupportedPrograms : responseAgain.getKitChangeProducts()) {
            Product product = productAndSupportedPrograms.getProduct();
            productProgramRepository.batchSave(productAndSupportedPrograms.getProductPrograms());
            updateDeactivateProductNotifyList(product);
            productList.add(product);
        }
        productRepository.batchCreateOrUpdateProducts(productList);
    }

    private SyncDownKitChangeDraftProductsResponse getSyncDownKitChangeProductResponse() throws LMISException {
        return lmisRestApi.fetchKitChangeDraftProductsAgain(sharedPreferenceMgr.getLastSyncProductTime());
    }

    private void fetchAndSaveStockCards(String startDate, String endDate) throws LMISException {
        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();

        SyncDownStockCardResponse syncDownStockCardResponse = lmisRestApi.fetchStockMovementData(facilityId, startDate, endDate);

        try {
            stockRepository.batchCreateSyncDownStockCardsAndMovements(syncDownStockCardResponse.getStockCards());
        } catch (SQLException e) {
            new LMISException(e, "fetchAndSaveStockCards exception").reportToFabric();
        }
    }

    public Observable<Void> saveStockCardsFromLastYear(final List<StockCard> stockCards) {

        List<Observable<Void>> observables = new ArrayList<>();
        if (stockCards.isEmpty()) {
            return zipObservables(observables);
        }

        Scheduler scheduler = SchedulerBuilder.createScheduler();


        int threadNumber = Runtime.getRuntime().availableProcessors();

        int numberOfElementsInAListForAnObservable = stockCards.size() / threadNumber;
        int startPosition = 0;
        for (int arrayNumber = 1; arrayNumber <= threadNumber; arrayNumber++) {
            int endPosition = arrayNumber == threadNumber ? stockCards.size() - 1 : numberOfElementsInAListForAnObservable * arrayNumber;
            observables.add(saveStockCards(stockCards.subList(startPosition, endPosition), scheduler));
            startPosition = endPosition + 1;
        }
        return zipObservables(observables);
    }

    private Observable<Void> zipObservables(List<Observable<Void>> tasks) {
        return Observable.zip(tasks, args -> null);
    }


    public Observable<Void> saveStockCards(final List<StockCard> stockCards, Scheduler scheduler) {

        return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
            try {
                stockRepository.batchCreateSyncDownStockCardsAndMovements(stockCards);
                stockService.immediatelyUpdateAvgMonthlyConsumption();
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).observeOn(scheduler);
    }

    private String getStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        final Date startTime = DateUtil.dateMinusMonth(calendar.getTime(),
                sharedPreferenceMgr.getMonthOffsetThatDefinedOldData());

        return DateUtil.formatDate(startTime, DateUtil.DB_DATE_FORMAT);
    }

    private void fetchAndSaveRequisition() throws LMISException {
        final String facilityCode = UserInfoMgr.getInstance().getUser().getFacilityCode();
        SyncDownRequisitionsResponse syncDownRequisitionsResponse = lmisRestApi.fetchRequisitions(facilityCode, getStartDate());
        if (syncDownRequisitionsResponse == null) {
            LMISException e = new LMISException("Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
            e.reportToFabric();
            throw e;
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
        SyncingServiceList(R.string.msg_service_lists),
        SyncingReportType(R.string.msg_fetching_products),
        SyncingPrograms(R.string.msg_fetching_programs),
        SyncingProduct(R.string.msg_fetching_products),
        SyncingStockCardsLastMonth(R.string.msg_sync_stock_movements_data),
        SyncingRequisition(R.string.msg_sync_requisition_data),
        SyncingRapidTests,

        ProductSynced,
        ServiceSynced,
        ProgramSynced,
        ReportTypeSynced,
        StockCardsLastMonthSynced,
        RequisitionSynced,
        StockCardsLastYearSynced,
        RapidTestsSynced,
        ShouldGoToInitialInventory;

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
