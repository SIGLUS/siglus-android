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

import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.StockMovementEntry;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

@Singleton
public class SyncUpManager {

    private static final String TAG = "SyncUpManager";

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    RnrFormItemRepository rnrFormItemRepository;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    @Inject
    StockRepository stockRepository;

    @Inject
    private SyncErrorsRepository syncErrorsRepository;

    protected LMISRestApi lmisRestApi;

    public SyncUpManager() {
        lmisRestApi = new LMISRestManager().getLmisRestApi();
    }

    public boolean syncRnr() {
        List<RnRForm> forms;
        try {
            Log.d(TAG, "===> Preparing RnrForm for Syncing: Delete Deactivated Products...");
            forms = rnrFormRepository.deleteDeactivatedProductItemsFromUnsyncedForms();

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
            syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.StockCards, 0L);
            Log.d(TAG, "===> SyncStockMovement : synced");
            return true;
        } catch (LMISException exception) {
            exception.reportToFabric();
            syncErrorsRepository.save(new SyncError(exception.getMessage(), SyncType.StockCards, 0L));
            Log.e(TAG, "===> SyncStockMovement : synced failed ->" + exception.getMessage());
            return false;
        }
    }

    public void syncAppVersion() {
        if (!sharedPreferenceMgr.hasSyncedVersion()) {
            AppInfoRequest request = new AppInfoRequest(UserInfoMgr.getInstance().getFacilityCode(), UserInfoMgr.getInstance().getUser().getUsername(), UserInfoMgr.getInstance().getVersion());
            lmisRestApi.updateAppVersion(request, new Callback<Void>() {
                @Override
                public void success(Void o, Response response) {
                    sharedPreferenceMgr.setSyncedVersion(true);
                    Log.d(TAG, "===> SyncAppVersion : synced");
                }

                @Override
                public void failure(RetrofitError error) {
                    sharedPreferenceMgr.setSyncedVersion(false);
                    Log.d(TAG, "===> SyncAppVersion : sync failed");
                }
            });
        }
    }

    private boolean submitRequisition(RnRForm rnRForm) {
        try {
            lmisRestApi.submitRequisition(rnRForm);
            syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RnRForm, rnRForm.getId());
            Log.d(TAG, "===> SyncRnr : synced ->");
            return true;
        } catch (LMISException e) {
            e.reportToFabric();
            Log.e(TAG, "===> SyncRnr : sync failed ->" + e.getMessage());
            syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.RnRForm, rnRForm.getId()));
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

    private void markStockDataSynced(List<StockMovementItem> stockMovementItems) throws LMISException {

        Observable.from(stockMovementItems).forEach(new Action1<StockMovementItem>() {
            @Override
            public void call(StockMovementItem stockMovementItem) {
                stockMovementItem.setSynced(true);
            }
        });

        stockRepository.batchCreateOrUpdateStockMovements(stockMovementItems);
    }
}
