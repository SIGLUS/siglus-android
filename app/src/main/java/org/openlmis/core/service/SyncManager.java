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
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.model.ProductsResponse;
import org.openlmis.core.network.model.RequisitionResponse;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

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

    @Inject
    ProgramRepository programRepository;

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    StockRepository stockRepository;

    LMISRestApi lmisRestApi;
    @Inject
    private AccountManager accountManager;
    @InjectResource(R.string.sync_content_authority)
    private String syncContentAuthority;
    @InjectResource(R.string.sync_account_type)
    private String syncAccountType;
    @InjectResource(R.integer.sync_interval)
    private Integer syncInterval;

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

    public void syncProductsWithProgram() throws Exception {
        User user = UserInfoMgr.getInstance().getUser();
        ProductsResponse response = lmisRestApi.fetchProducts(user.getFacilityCode());
        List<Program> programsWithProducts = response.getProgramsWithProducts();
        for (Program programWithProducts : programsWithProducts) {
            try {
                programRepository.saveProgramWithProduct(programWithProducts);
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
    }

    public void syncProductsWithProgramAsync(Observer<Void> observer) {
        rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    syncProductsWithProgram();
                } catch (Exception e) {
                    subscriber.onError(new LMISException("Get Product List Failed."));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void syncBackData(Observer<Void> observer) {
        rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {

            @Override
            public void call(Subscriber<? super Void> subscriber) {
                //TODO should make real api call
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    subscriber.onError(new LMISException("Syncing back data failed"));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public boolean syncRnr() {
        List<RnRForm> forms = null;
        try {
            forms = rnrFormRepository.listUnSynced();
            Log.d(TAG, "===> SyncRnR :" + forms.size() + " RnrForm ready to sync...");

            if (forms.size() == 0){
                return false;
            }
        } catch (LMISException e) {
            e.printStackTrace();
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
            RequisitionResponse response = lmisRestApi.submitRequisition(rnRForm);
            return StringUtils.isEmpty(response.getError());
        } catch (Exception e) {
            Log.e(TAG, "===> SyncRnr : synced failed ->" + e.getMessage());
        }
        return false;
    }

    private void markRnrFormSynced(RnRForm rnRForm) {
        rnRForm.setSynced(true);
        try {
            rnrFormRepository.save(rnRForm);
        } catch (Exception e) {
            Log.e(TAG, "===> SyncRnr : mark synced failed -> " + rnRForm.getId());
        }
    }

    public boolean syncStockCards(){
        User user = UserInfoMgr.getInstance().getUser();
        if (user == null){
            return false;
        }

        List<StockMovementItem> stockMovementItems;
        try {
            stockMovementItems = stockRepository.listUnSynced();
        }catch (LMISException e){
            e.printStackTrace();
            return false;
        }

        if (stockMovementItems.isEmpty()){
            return false;
        }

        List<StockMovementEntry> syncList = FluentIterable.from(stockMovementItems).transform(new Function<StockMovementItem, StockMovementEntry>() {
            @Override
            public StockMovementEntry apply(StockMovementItem stockMovementItem) {
                StockMovementEntry entry = new StockMovementEntry();
                String date = DateUtil.formatDate(stockMovementItem.getMovementDate(), "yyyyMMdd'T'HHmmssZ");
                entry.setOccurred(date);
                entry.setProductCode(stockMovementItem.getStockCard().getProduct().getCode());
                entry.setQuantity(stockMovementItem.getMovementQuantity());
                entry.setReasonName(stockMovementItem.getReason());
                return entry;
            }
        }).toList();

        try {
            lmisRestApi.pushStockMovementData(user.getFacilityId(), syncList);
            Observable.from(stockMovementItems).forEach(new Action1<StockMovementItem>() {
                @Override
                public void call(StockMovementItem stockMovementItem) {
                    stockMovementItem.setSynced(true);
                }
            });

            stockRepository.batchUpdateStockMovements(stockMovementItems);
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "===> SyncStockMovement : synced failed ->" + e.getMessage());
        }

        return false;
    }
}
