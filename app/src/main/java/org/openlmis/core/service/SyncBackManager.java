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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NoFacilityForUserException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.model.SyncBackProductsResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class SyncBackManager {
    public static final int DAYS_OF_MONTH = 30;
    public static final int MONTHS_OF_YEAR = 12;

    private boolean SaveProductLock = false;
    private boolean saveRequisitionLock = false;
    private boolean isSyncing = false;

    private final Object STOCK_MONTH_SYNC_LOCK = new Object();
    private final Object STOCK_YEAR_SYNC_LOCK = new Object();

    protected LMISRestApi lmisRestApi;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;
    @Inject
    RnrFormRepository rnrFormRepository;
    @Inject
    StockRepository stockRepository;
    @Inject
    ProgramRepository programRepository;

    public SyncBackManager() {
        lmisRestApi = new LMISRestManager().getLmisRestApi();
    }

    public void syncBackServerData(Subscriber<SyncProgress> subscriber) {
        if (isSyncing) {
            return;
        }

        Observable.create(new Observable.OnSubscribe<SyncProgress>() {
            @Override
            public void call(Subscriber<? super SyncProgress> subscriber) {
                try {
                    isSyncing = true;
                    syncProducts(subscriber);
                    syncBackLastMonthStockCards(subscriber);
                    syncBackRequisition(subscriber);
                    syncLastYearStockCardsSilently(subscriber);

                    isSyncing = false;
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    isSyncing = false;
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    private void syncLastYearStockCardsSilently(Subscriber<? super SyncProgress> subscriber) {
        if (!sharedPreferenceMgr.isLastYearStockDataSynced()) {
            try {
                subscriber.onNext(SyncProgress.SyncingStockCardsLastYear);
                fetchLatestYearStockMovements();
                sharedPreferenceMgr.setLastYearStockCardDataSynced(true);
                subscriber.onNext(SyncProgress.StockCardsLastYearSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setLastYearStockCardDataSynced(false);
                e.reportToFabric();
            }
        }
    }

    private void syncBackRequisition(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        if (!sharedPreferenceMgr.isRequisitionDataSynced()) {
            try {
                subscriber.onNext(SyncProgress.SyncingRequisition);
                fetchAndSaveRequisition();
                sharedPreferenceMgr.setRequisitionDataSynced(true);
                subscriber.onNext(SyncProgress.RequisitionSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setRequisitionDataSynced(false);
                throw new LMISException(errorMessage(R.string.msg_sync_requisition_failed));
            }
        }
    }

    private void syncBackLastMonthStockCards(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        if (!sharedPreferenceMgr.isLastMonthStockDataSynced()) {
            try {
                subscriber.onNext(SyncProgress.SyncingStockCardsLastMonth);
                fetchLatestOneMonthMovements();
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
                subscriber.onNext(SyncProgress.StockCardsLastMonthSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setLastMonthStockCardDataSynced(false);
                throw new LMISException(errorMessage(R.string.msg_sync_stockmovement_failed));
            }
        }
    }

    private void syncProducts(Subscriber<? super SyncProgress> subscriber) throws LMISException {
        if (!sharedPreferenceMgr.hasGetProducts()) {
            try {
                subscriber.onNext(SyncProgress.SyncingProduct);
                fetchAndSaveProductsWithProgram();
                sharedPreferenceMgr.setHasGetProducts(true);
                subscriber.onNext(SyncProgress.ProductSynced);
            } catch (LMISException e) {
                sharedPreferenceMgr.setHasGetProducts(false);
                throw e;
            }
        }
    }

    public void syncProductsWithProgram(Observer<Void> observer) {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    fetchAndSaveProductsWithProgram();
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void syncBackStockCards(Observer<Void> observer, final boolean isSyncMonth) {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_sync_back_stock_movement_273)) {
            return;
        }
        Observable.create(new Observable.OnSubscribe<Void>() {

            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    synchronized (STOCK_MONTH_SYNC_LOCK) {
                        if (isSyncMonth && !sharedPreferenceMgr.isLastMonthStockDataSynced()) {
                            fetchLatestOneMonthMovements();
                            sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
                        }
                    }

                    synchronized (STOCK_YEAR_SYNC_LOCK) {
                        if (!isSyncMonth && !sharedPreferenceMgr.isLastYearStockDataSynced()) {
                            fetchLatestYearStockMovements();
                            sharedPreferenceMgr.setLastYearStockCardDataSynced(true);
                        }
                    }

                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(new LMISException("Syncing StockCard back failed"));
                    new LMISException(throwable).reportToFabric();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void syncBackRequisition(Observer<Void> observer) {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    fetchAndSaveRequisition();
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(new LMISException("Syncing back requisition failed"));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private void fetchAndSaveProductsWithProgram() throws LMISException {
        User user = UserInfoMgr.getInstance().getUser();
        user.setFacilityCode("");
        if (StringUtils.isEmpty(user.getFacilityCode())) {
            throw new NoFacilityForUserException(errorMessage(R.string.msg_user_not_facility));
        }
        try {
            SyncBackProductsResponse response = lmisRestApi.fetchProducts(user.getFacilityCode());

            if (SaveProductLock || sharedPreferenceMgr.hasGetProducts()) {
                throw new LMISException("It's Syncing in Background or Loaded");
            }

            SaveProductLock = true;
            List<Program> programsWithProducts = response.getProgramsWithProducts();
            for (Program programWithProducts : programsWithProducts) {
                programRepository.saveProgramWithProduct(programWithProducts);
            }
        } catch (Exception e) {
            throw new LMISException(errorMessage(R.string.msg_sync_products_list_failed));
        } finally {
            SaveProductLock = false;
        }
    }

    private void fetchAndSaveStockCards(String startDate, String endDate) throws LMISException {
        //default start date is one month before and end date is one day after
        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();

        SyncDownStockCardResponse syncDownStockCardResponse = lmisRestApi.fetchStockMovementData(facilityId, startDate, endDate);

        for (StockCard stockCard : syncDownStockCardResponse.getStockCards()) {

            for (StockMovementItem item : stockCard.getStockMovementItemsWrapper()) {
                item.setSynced(true);
            }

            if (stockCard.getId() <= 0) {
                stockRepository.saveStockCardAndBatchUpdateMovements(stockCard);
            } else {
                stockRepository.batchCreateOrUpdateStockMovements(stockCard.getStockMovementItemsWrapper());
            }
        }
    }

    private void fetchAndSaveRequisition() throws LMISException {
        SyncDownRequisitionsResponse syncDownRequisitionsResponse = lmisRestApi.fetchRequisitions(UserInfoMgr.getInstance().getUser().getFacilityCode());

        if (syncDownRequisitionsResponse == null) {
            throw new LMISException("Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
        }

        if (saveRequisitionLock || sharedPreferenceMgr.isRequisitionDataSynced()) {
            throw new LMISException("Sync Requisition Background or Loaded");
        }
        saveRequisitionLock = true;

        try {
            List<RnRForm> rnRForms = syncDownRequisitionsResponse.getRequisitions();
            for (RnRForm form : rnRForms) {
                rnrFormRepository.createFormAndItems(form);//todo: all or nothing with transaction
            }
        } finally {
            saveRequisitionLock = false;
        }
    }

    private void fetchLatestOneMonthMovements() throws LMISException {
        Date now = new Date();
        Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
        String startDateStr = DateUtil.formatDate(startDate, "yyyy-MM-dd");

        Date endDate = DateUtil.addDayOfMonth(now, 1);
        String endDateStr = DateUtil.formatDate(endDate, "yyyy-MM-dd");
        fetchAndSaveStockCards(startDateStr, endDateStr);

        List<StockCard> syncedStockCard = stockRepository.list();
        if (!(syncedStockCard == null || syncedStockCard.isEmpty())) {
            sharedPreferenceMgr.getPreference().edit().putBoolean(SharedPreferenceMgr.KEY_INIT_INVENTORY, false).apply();
        }
    }

    private void fetchLatestYearStockMovements() throws LMISException {
        long syncEndTimeMillions = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, new Date().getTime());

        Date now = new Date(syncEndTimeMillions);

        int startMonth = sharedPreferenceMgr.getPreference().getInt(SharedPreferenceMgr.KEY_STOCK_SYNC_CURRENT_INDEX, 1);

        for (int month = startMonth; month <= MONTHS_OF_YEAR; month++) {
            Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH * (month + 1));
            String startDateStr = DateUtil.formatDate(startDate, "yyyy-MM-dd");

            Date endDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH * month);
            String endDateStr = DateUtil.formatDate(endDate, "yyyy-MM-dd");

            try {
                fetchAndSaveStockCards(startDateStr, endDateStr);
            } catch (Throwable throwable) {
                sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, syncEndTimeMillions).apply();
                sharedPreferenceMgr.getPreference().edit().putInt(SharedPreferenceMgr.KEY_STOCK_SYNC_CURRENT_INDEX, month).apply();
                throw throwable;
            }
        }
    }

    private String errorMessage(int code) {
        return LMISApp.getContext().getResources().getString(code);
    }

    public enum SyncProgress {
        SyncingProduct,
        ProductSynced,

        SyncingStockCardsLastMonth,
        StockCardsLastMonthSynced,

        SyncingRequisition,
        RequisitionSynced,

        SyncingStockCardsLastYear,
        StockCardsLastYearSynced;
    }
}
