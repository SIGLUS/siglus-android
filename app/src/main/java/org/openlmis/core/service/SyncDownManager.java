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
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.ServiceFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.adapter.V3ProductsResponseAdapter;
import org.openlmis.core.network.model.FacilityInfoResponse;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SupportedProgram;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownProgramDataResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownServiceResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.service.sync.SchedulerBuilder;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

    public static volatile boolean isSyncing = false;

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
    @Inject
    UserRepository userRepository;

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
                // TODO: Remove the comment when developing to the corresponding api
                syncDownFacilityInfo(subscriber1);
//                syncDownService(subscriber1);
                syncDownProducts(subscriber1);
//                syncDownLastMonthStockCards(subscriber1);
//                syncDownRequisition(subscriber1);
//                syncDownRapidTests(subscriber1);
                isSyncing = false;
                subscriber1.onCompleted();
            } catch (LMISException e) {
                isSyncing = false;
                subscriber1.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    private void syncDownFacilityInfo(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        try {
            subscriber.onNext(SyncProgress.SyncingFacilityInfo);
            fetchAndSaveFacilityInfo();
            subscriber.onNext(SyncProgress.FacilityInfoSynced);
        } catch (LMISException e) {
            LMISException e1 = new LMISException(errorMessage(R.string.msg_fetching_facility_info_failed));
            e1.reportToFabric();
            throw e;
        }
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
                    // TODO: Remove the comment when developing to the corresponding api
//                    syncStockCardsLastYearSilently.performSync().subscribe(getSyncLastYearStockCardSubscriber());
                } else if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
                    if (TextUtils.isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
                        sendSyncFinishedBroadcast();
                    }
                } else if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && sharedPreferenceMgr.isSyncingLastYearStockCards()) {
                    sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.w(TAG,e);
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
                Log.w(TAG,e);
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
                sharedPreferenceMgr.setStockLastSyncTime();
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
            } catch (LMISException e) {
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(false);
                Log.w(TAG,e);
                LMISException e1 = new LMISException(errorMessage(R.string.msg_sync_stock_movement_failed));
                e1.reportToFabric();
                throw e1;
            }
        } else {
            dirtyDataManager.initialDirtyDataCheck();
        }
        subscriber.onNext(SyncProgress.StockCardsLastMonthSynced);
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
            productProgramRepository.batchSave(product, productAndSupportedPrograms.getProductPrograms());

            updateDeactivateProductNotifyList(product);
            productList.add(product);
        }
        productRepository.batchCreateOrUpdateProducts(productList);
        sharedPreferenceMgr.setKeyIsFirstLoginVersion87();
        sharedPreferenceMgr.setLastSyncProductTime(response.getLastSyncTime());
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
        boolean isFirstLoginVersion87 = sharedPreferenceMgr.getKeyIsFirstLoginVersion87();
        return lmisRestApi.fetchLatestProducts(isFirstLoginVersion87 ? null : sharedPreferenceMgr.getLastSyncProductTime());
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
            int endPosition = arrayNumber == threadNumber ? stockCards.size() : numberOfElementsInAListForAnObservable * arrayNumber;
            observables.add(saveStockCards(stockCards.subList(startPosition, endPosition), scheduler));
            startPosition = endPosition;
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
        calendar.setTime(DateUtil.getCurrentDate());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        final Date startTime = DateUtil.dateMinusMonth(calendar.getTime(),
                sharedPreferenceMgr.getMonthOffsetThatDefinedOldData());

        return DateUtil.formatDate(startTime, DateUtil.DB_DATE_FORMAT);
    }

    private void fetchAndSaveFacilityInfo() throws LMISException {
        FacilityInfoResponse facilityInfoResponse = lmisRestApi.fetchFacilityInfo();
        if (facilityInfoResponse == null) {
            LMISException e = new LMISException("fetch facility info exception");
            e.reportToFabric();
            throw e;
        }
        List<Program> programs = covertFacilityInfoToProgram(facilityInfoResponse);
        List<ReportTypeForm> reportTypeForms = covertFacilityInfoToReportTypeForm(facilityInfoResponse);
        User user = UserInfoMgr.getInstance().getUser();
        user.setFacilityCode(facilityInfoResponse.getCode());
        user.setFacilityName(facilityInfoResponse.getName());
        userRepository.createOrUpdate(user);
        UserInfoMgr.getInstance().setUser(user);
        V3ProductsResponseAdapter.addPrograms(programs);
        programRepository.batchCreateOrUpdatePrograms(programs);
        sharedPreferenceMgr.setReportTypesData(reportTypeForms);
        reportTypeFormRepository.batchCreateOrUpdateReportTypes(reportTypeForms);
    }

    private List<ReportTypeForm> covertFacilityInfoToReportTypeForm(FacilityInfoResponse facilityInfoResponse) {
        List<ReportTypeForm> reportTypeForms = new ArrayList<>();
        for (SupportedProgram supportedProgram : facilityInfoResponse.getSupportedPrograms()) {
            ReportTypeForm reportTypeForm = ReportTypeForm
                    .builder()
                    .code(supportedProgram.getCode())
                    .name(supportedProgram.getName())
                    .active(supportedProgram.isSupportActive())
                    .startTime(DateUtil.parseString(supportedProgram.getSupportStartDate(), DateUtil.DB_DATE_FORMAT))
                    .build();
            reportTypeForms.add(reportTypeForm);
        }
        return reportTypeForms;
    }

    private List<Program> covertFacilityInfoToProgram(FacilityInfoResponse facilityInfoResponse) {
        List<Program> programs = new ArrayList<>();
        for (SupportedProgram supportedProgram : facilityInfoResponse.getSupportedPrograms()) {
            Program program = Program
                    .builder()
                    .programCode(supportedProgram.getCode())
                    .programName(supportedProgram.getName())
                    .isSupportEmergency(supportedProgram.getCode().equals("VC"))
                    .build();
            programs.add(program);
        }
        return programs;
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
        Date now = DateUtil.getCurrentDate();
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
        SyncingFacilityInfo(R.string.msg_fetching_facility_info),
        SyncingServiceList(R.string.msg_service_lists),
        SyncingProduct(R.string.msg_fetching_products),
        SyncingStockCardsLastMonth(R.string.msg_sync_stock_movements_data),
        SyncingRequisition(R.string.msg_sync_requisition_data),
        SyncingRapidTests,

        ProductSynced,
        ServiceSynced,
        FacilityInfoSynced,
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
