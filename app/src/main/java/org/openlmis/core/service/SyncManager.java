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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

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
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.network.model.SyncBackProductsResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.InjectResource;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY;
import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Singleton
public class SyncManager {

    private static final String TAG = "SyncManager";
    public static final int DAYS_OF_MONTH = 30;
    public static final int MONTHS_OF_YEAR = 12;
    private final Object STOCK_MONTH_SYNC_LOCK = new Object();
    private final Object STOCK_YEAR_SYNC_LOCK = new Object();

    @Inject
    ProgramRepository programRepository;

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    @Inject
    StockRepository stockRepository;

    protected LMISRestApi lmisRestApi;

    @Inject
    private AccountManager accountManager;
    @InjectResource(R.string.sync_content_authority)
    private String syncContentAuthority;
    @InjectResource(R.string.sync_account_type)
    private String syncAccountType;
    @InjectResource(R.integer.sync_interval)
    private Integer syncInterval;
    private boolean SaveProductLock = false;
    private boolean saveRequisitionLock = false;
    private boolean saveStockCardLock = false;
    private Observable<Void> productObservable;
    private SyncErrorsRepository syncErrorsRepository = new SyncErrorsRepository(LMISApp.getContext());

    public SyncManager() {
        lmisRestApi = new LMISRestManager().getLmisRestApi();
    }

    public void kickOff() {
        Account account = findFirstLmisAccount();
        if (account != null) {
            setIsSyncable(account, syncContentAuthority, 1);
            setSyncAutomatically(account, syncContentAuthority, true);
            addPeriodicSync(account, syncContentAuthority, periodicSyncParams(), syncInterval);
        }
        Log.d(TAG, "sync service started");
    }

    public void shutDown() {
        Account account = findFirstLmisAccount();
        if (account != null) {
            ContentResolver.cancelSync(account, syncContentAuthority);
            ContentResolver.setSyncAutomatically(account, syncContentAuthority, false);
        }
        Log.d(TAG, "sync service stopped");
    }

    private Account findFirstLmisAccount() {
        List<Account> accounts = newArrayList(accountManager.getAccounts());
        List<Account> lmisAccounts = from(accounts).filter(new Predicate<Account>() {
            @Override
            public boolean apply(Account input) {
                return syncAccountType.equals(input.type);
            }
        }).toList();

        if (lmisAccounts.size() > 0) {
            return lmisAccounts.get(0);
        }

        return null;
    }

    private Bundle periodicSyncParams() {
        Bundle extras = new Bundle();
        extras.putBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false);
        extras.putBoolean(SYNC_EXTRAS_EXPEDITED, false);
        extras.putBoolean(SYNC_EXTRAS_DO_NOT_RETRY, false);
        extras.putBoolean(SYNC_EXTRAS_MANUAL, false);
        return extras;
    }

    public void createSyncAccount(User user) {
        Account account = new Account(user.getUsername(), syncAccountType);
        accountManager.addAccountExplicitly(account, user.getPassword(), null);
    }


    public void requestSyncImmediately() {
        Account account = findFirstLmisAccount();
        if (account != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

            ContentResolver.requestSync(findFirstLmisAccount(), syncContentAuthority, bundle);
        }
    }

    public void syncProductsWithProgram() throws LMISException {
        User user = UserInfoMgr.getInstance().getUser();

        if (StringUtils.isEmpty(user.getFacilityCode())) {
            throw new NoFacilityForUserException("No Facility for this User");
        }
        SyncBackProductsResponse response = lmisRestApi.fetchProducts(user.getFacilityCode());

        if (SaveProductLock || sharedPreferenceMgr.getPreference().getBoolean(SharedPreferenceMgr.KEY_HAS_GET_PRODUCTS, false)) {
            throw new LMISException("It's Syncing in Background or Loaded");
        }

        SaveProductLock = true;
        try {
            List<Program> programsWithProducts = response.getProgramsWithProducts();
            for (Program programWithProducts : programsWithProducts) {
                programRepository.saveProgramWithProduct(programWithProducts);
            }
            sharedPreferenceMgr.getPreference().edit().putBoolean(SharedPreferenceMgr.KEY_HAS_GET_PRODUCTS, true);
        } finally {
            SaveProductLock = false;
        }
    }

    public void syncProductsWithProgramAsync(Observer<Void> observer) {
        getProductsObservable().subscribe(observer);
    }

    public Observable<Void> getProductsObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    syncProductsWithProgram();
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void syncRequisitionData(Observer<Void> observer) {
        rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {

            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    fetchAndSaveRequisitionData();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(new LMISException("Syncing back data failed"));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    protected void fetchAndSaveRequisitionData() throws LMISException {
        SyncDownRequisitionsResponse syncDownRequisitionsResponse = lmisRestApi.fetchRequisitions(UserInfoMgr.getInstance().getUser().getFacilityCode());

        if (syncDownRequisitionsResponse == null) {
            throw new LMISException("Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
        }

        SyncDownRequisitionsResponse syncBackRequisitionsResponse = lmisRestApi.fetchRequisitions(UserInfoMgr.getInstance().getUser().getFacilityCode());
        if (syncBackRequisitionsResponse == null) {
            throw new LMISException("Can't get SyncBackRequisitionsResponse, you can check json parse to POJO logic");
        }

        if (saveRequisitionLock || sharedPreferenceMgr.getPreference().getBoolean(SharedPreferenceMgr.KEY_IS_REQUISITION_DATA_SYNCED, false)) {
            throw new LMISException("Sync Requisition Background or Loaded");
        }
        saveRequisitionLock = true;

        try {
            List<RnRForm> rnRForms = syncDownRequisitionsResponse.getRequisitions();
            for (RnRForm form : rnRForms) {
                rnrFormRepository.createFormAndItems(form);
            }
            sharedPreferenceMgr.getPreference().edit().putBoolean(SharedPreferenceMgr.KEY_IS_REQUISITION_DATA_SYNCED, true);
        } finally {
            saveRequisitionLock = false;
        }
    }

    public boolean syncRnr() {
        List<RnRForm> forms = null;
        try {
            forms = rnrFormRepository.listUnSynced();
            Log.d(TAG, "===> SyncRnR :" + forms.size() + " RnrForm ready to sync...");

            if (forms.size() == 0) {
                return false;
            }
        } catch (LMISException e) {
            e.reportToFabric();
            return false;
        }

        Observable.from(forms).filter(new Func1<RnRForm, Boolean>() {
            @Override
            public Boolean call(RnRForm rnRForm) {
                return submitRequisition(rnRForm);
            }
        }).subscribe(new Action1<RnRForm>() {
            @Override
            public void call(RnRForm rnRForm) {
                markRnrFormSynced(rnRForm);
            }
        });

        return from(forms).allMatch(new Predicate<RnRForm>() {
            @Override
            public boolean apply(RnRForm rnRForm) {
                return rnRForm.isSynced();
            }
        });
    }

    private boolean submitRequisition(RnRForm rnRForm) {
        try {
            lmisRestApi.submitRequisition(rnRForm);
            return true;
        } catch (UndeclaredThrowableException e) {
            new LMISException(e).reportToFabric();
            Log.e(TAG, "===> SyncRnr : synced failed ->" + e.getMessage());
            syncErrorsRepository.save(new SyncError(e.getCause().getMessage(), SyncType.RnRForm, rnRForm.getId()));
            return false;
        }
    }

    private void markRnrFormSynced(RnRForm rnRForm) {
        rnRForm.setSynced(true);
        try {
            rnrFormRepository.save(rnRForm);
        } catch (LMISException e) {
            e.reportToFabric();
            Log.e(TAG, "===> SyncRnr : mark synced failed -> " + rnRForm.getId());
        }
    }

    private List<StockMovementItem> fetchUnSyncedStockMovements() {
        List<StockMovementItem> stockMovementItems;
        try {
            stockMovementItems = stockRepository.listUnSynced();
            Log.d(TAG, "===> SyncStockMovement :" + stockMovementItems.size() + " StockMovement ready to sync...");
            return stockMovementItems;

        } catch (LMISException e) {
            e.reportToFabric();
            Log.e(TAG, "===> SyncStockMovement : synced failed ->" + e.getMessage());
            return null;
        }
    }

    private List<StockMovementEntry> convertStockMovementItemsToStockMovementEntriesForSync(final String facilityId, List<StockMovementItem> stockMovementItems) {

        return FluentIterable.from(stockMovementItems).transform(new Function<StockMovementItem, StockMovementEntry>() {
            @Override
            public StockMovementEntry apply(StockMovementItem stockMovementItem) {
                return new StockMovementEntry(stockMovementItem, facilityId);
            }
        }).toList();
    }

    public boolean syncStockCards() {
        List<StockMovementItem> stockMovementItems = fetchUnSyncedStockMovements();
        if (null == stockMovementItems || stockMovementItems.isEmpty()) {
            return false;
        }

        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
        List<StockMovementEntry> movementEntriesToSync = convertStockMovementItemsToStockMovementEntriesForSync(facilityId, stockMovementItems);

        try {
            lmisRestApi.syncUpStockMovementData(facilityId, movementEntriesToSync);
            markStockDataSynced(stockMovementItems);
            return true;

        } catch (LMISException exception) {
            new LMISException(exception).reportToFabric();
            Log.e(TAG, "===> SyncStockMovement : synced failed ->" + exception.getMessage());
            return false;

        } catch (UndeclaredThrowableException e) {
            new LMISException(e).reportToFabric();
            syncErrorsRepository.save(new SyncError(e.getCause().getMessage(), SyncType.StockCards, 0l));
            return false;
        }
    }

    private void markStockDataSynced(List<StockMovementItem> stockMovementItems) throws LMISException {

        Observable.from(stockMovementItems).forEach(new Action1<StockMovementItem>() {
            @Override
            public void call(StockMovementItem stockMovementItem) {
                stockMovementItem.setSynced(true);
            }
        });

        stockRepository.batchUpdateStockMovements(stockMovementItems);
    }

    public void fetchStockCardsData(Observer<Void> observer, final boolean isSyncMonth) {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_sync_back_stock_movement_273)) {
            return;
        }
        rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {

            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    synchronized (STOCK_MONTH_SYNC_LOCK) {
                        if (isSyncMonth && !sharedPreferenceMgr.getPreference().getBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, false)) {
                            fetchLatestOneMonthMovements();
                            sharedPreferenceMgr.getPreference().edit().putBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, true).apply();
                        }
                    }

                    synchronized (STOCK_YEAR_SYNC_LOCK) {
                        if (!isSyncMonth && !sharedPreferenceMgr.getPreference().getBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_YEAR_STOCKMOVEMENTS, false)) {
                            fetchLatestYearStockMovements();
                            sharedPreferenceMgr.getPreference().edit().putBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_YEAR_STOCKMOVEMENTS, true).apply();
                        }
                    }
                } catch (Throwable throwable) {
                    subscriber.onError(new LMISException("Syncing StockCard back failed"));
                    new LMISException(throwable).reportToFabric();
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void fetchLatestYearStockMovements() throws Throwable {
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

    public void fetchLatestOneMonthMovements() throws Throwable {
        Date now = new Date();
        Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
        String startDateStr = DateUtil.formatDate(startDate, "yyyy-MM-dd");

        Date endDate = DateUtil.addDayOfMonth(now, 1);
        String endDateStr = DateUtil.formatDate(endDate, "yyyy-MM-dd");
        fetchAndSaveStockCards(startDateStr, endDateStr);
    }

    public void fetchAndSaveStockCards(String startDate, String endDate) throws Throwable {
        //default start date is one month before and end date is one day after
        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();

        SyncDownStockCardResponse syncDownStockCardResponse = lmisRestApi.fetchStockMovementData(facilityId, startDate, endDate);

        for (StockCard stockCard : syncDownStockCardResponse.getStockCards()) {
            StockMovementItem oldestItem = stockRepository.getOldestMovementItemById(stockCard.getId());
            Long stockOnHand = stockCard.getStockOnHand();

            if (oldestItem != null && sharedPreferenceMgr.getPreference().getBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, false)) {
                stockOnHand = oldestItem.calculateStockMovementStockOnHand(oldestItem.getStockOnHand());
            }
            stockCard.setUpStockOnHandForMovements(stockOnHand);

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

    public void syncAppVersion() {
        if (!sharedPreferenceMgr.hasSyncedVersion()) {
            AppInfoRequest request = new AppInfoRequest(UserInfoMgr.getInstance().getFacilityCode(), UserInfoMgr.getInstance().getUser().getUsername(), UserInfoMgr.getInstance().getVersion());

            lmisRestApi.updateAppVersion(request, new Callback<Void>() {
                @Override
                public void success(Void o, Response response) {
                    sharedPreferenceMgr.setSyncedVersion(true);
                }

                @Override
                public void failure(RetrofitError error) {
                    sharedPreferenceMgr.setSyncedVersion(false);
                }
            });
        }
    }
}
