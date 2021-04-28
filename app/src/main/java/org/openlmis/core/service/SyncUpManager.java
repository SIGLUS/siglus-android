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
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.CmmEntry;
import org.openlmis.core.network.model.DirtyDataItemEntry;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.network.model.SyncUpStockMovementDataSplitResponse;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.Sets;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import rx.Observable;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

@Singleton
public class SyncUpManager {

    private static final String TAG = "SyncUpManager";

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    @Inject
    StockMovementRepository stockMovementRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    CmmRepository cmmRepository;

    @Inject
    ProgramDataFormRepository programDataFormRepository;

    @Inject
    private SyncErrorsRepository syncErrorsRepository;

    @Inject
    private DirtyDataRepository dirtyDataRepository;

    @Inject
    DbUtil dbUtil;

    protected LMISRestApi lmisRestApi;

    public static volatile boolean isSyncing = false;

    public SyncUpManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public void syncUpData(Context context) {
        Log.d(TAG, "sync Up Data start");
        if (isSyncing) {
            return;
        }
        isSyncing = true;
        boolean isSyncDeleted = syncDeleteMovement();
        if (isSyncDeleted) {
            boolean isSyncRnrSuccessful = syncRnr();
            if (isSyncRnrSuccessful) {
                sharedPreferenceMgr.setRnrLastSyncTime();
            }

            boolean isSyncStockSuccessful = syncStockCards();
            if (isSyncStockSuccessful) {
                sharedPreferenceMgr.setStockLastSyncTime();
                syncArchivedProducts();
            }

            syncRapidTestForms();
            syncUpUnSyncedStockCardCodes();
            syncAppVersion();
            syncUpCmms();
        }
        Log.d(TAG, "sync Up Data end");
        isSyncing = false;
        if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && TextUtils.isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
            Intent intent = new Intent();
            intent.setAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public boolean syncRnr() {
        List<RnRForm> forms;
        try {
            Log.d(TAG, "===> Preparing RnrForm for Syncing: Delete Deactivated Products...");
            forms = rnrFormRepository.queryAllUnsyncedForms();

            Log.d(TAG, "===> SyncRnR :" + forms.size() + " RnrForm ready to sync...");

            if (forms.size() == 0) {
                return false;
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager:syncRnr").reportToFabric();
            return false;
        }

        Observable.from(forms).filter(this::submitRequisition).subscribe(this::markRnrFormSynced);

        return from(forms).allMatch(RnRForm::isSynced);
    }

    public boolean fakeSyncRnr() {
        List<RnRForm> forms;
        try {
            Log.d(TAG, "===> Preparing RnrForm for Syncing: Delete Deactivated Products...");
            forms = rnrFormRepository.queryAllUnsyncedForms();
            Log.d(TAG, "===> SyncRnR :" + forms.size() + " RnrForm ready to sync...");
            if (forms.size() == 0) {
                return false;
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager:fakeSyncRnr").reportToFabric();
            return false;
        }

        Observable.from(forms).filter(rnRForm -> {
            try {
                syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RnRForm, rnRForm.getId());
                Log.d(TAG, "===> SyncRnr : synced ->");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "===> SyncRnr : sync failed ->" + e.getMessage());
                new LMISException(e, "SyncUpManager:fakeSyncRnr,sync failed").reportToFabric();
                syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.RnRForm, rnRForm.getId()));
                return false;
            }
        }).subscribe(this::markRnrFormSynced);

        return from(forms).allMatch(RnRForm::isSynced);
    }

    public boolean syncStockCards() {
        try {
            List<StockMovementItem> stockMovementItems = stockMovementRepository.listUnSynced();
            if (stockMovementItems.isEmpty()) {
                return false;
            }

            final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
            List<StockMovementEntry> movementEntriesToSync = convertStockMovementItemsToStockMovementEntriesForSync(facilityId, stockMovementItems);

            if (movementEntriesToSync == null || movementEntriesToSync.isEmpty()) {
                new LMISException("SyncUpManager.movementEntriesToSync").reportToFabric();
                return false;
            }
            SyncUpStockMovementDataSplitResponse response = lmisRestApi.syncUpStockMovementDataSplit(facilityId, movementEntriesToSync);

            List<StockMovementItem> shouldMarkSyncedItems = new ArrayList<>();

            Set<String> syncFailedProduct = Sets.newHashSet(response.getErrorProductCodes());
            for (StockMovementItem stockMovementItem : stockMovementItems) {
                if (CollectionUtils.isEmpty(syncFailedProduct)
                        || !syncFailedProduct.contains(stockMovementItem.getStockCard().getProduct().getCode())) {
                    shouldMarkSyncedItems.add(stockMovementItem);
                }
            }

            markStockDataSynced(shouldMarkSyncedItems);
            syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.StockCards, 0L);
            if (!response.getErrorProductCodes().isEmpty()) {
                syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.SyncMovement, 2L);
                saveStockMovementErrors(response);
                return false;
            }
            sharedPreferenceMgr.setStockMovementSyncError("");
            Log.d(TAG, "===> SyncStockMovement : synced");
            return true;
        } catch (LMISException exception) {
            new LMISException(exception, "SyncUpManager.syncStockCards").reportToFabric();
            syncErrorsRepository.save(new SyncError(exception.getMessage(), SyncType.StockCards, 0L));
            Log.e(TAG, "===> SyncStockMovement : synced failed ->" + exception.getMessage());
            return false;
        }
    }

    private void saveStockMovementErrors(SyncUpStockMovementDataSplitResponse response) {
        List<String> syncErrorProduct = FluentIterable.from(response.getErrorProductCodes()).limit(3).toList();
        Intent intent = new Intent(Constants.INTENT_FILTER_ERROR_SYNC_DATA);
        intent.putExtra(Constants.SYNC_MOVEMENT_ERROR, syncErrorProduct.toString());
        LocalBroadcastManager.getInstance(LMISApp.getContext()).sendBroadcast(intent);
        sharedPreferenceMgr.setStockMovementSyncError(syncErrorProduct.toString());
        syncErrorsRepository.save(new SyncError(response.getErrorProductCodes().toString(), SyncType.SyncMovement, 2L));
    }

    public boolean fakeSyncStockCards() {
        try {
            List<StockMovementItem> stockMovementItems = stockMovementRepository.listUnSynced();
            if (stockMovementItems.isEmpty()) {
                return false;
            }
            markStockDataSynced(stockMovementItems);
            syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.StockCards, 0L);
            Log.d(TAG, "===> SyncStockMovement : synced");
            return true;
        } catch (LMISException exception) {
            new LMISException(exception, "SyncUpManager.fakeSyncStockCards").reportToFabric();
            syncErrorsRepository.save(new SyncError(exception.getMessage(), SyncType.StockCards, 0L));
            Log.e(TAG, "===> SyncStockMovement : synced failed ->" + exception.getMessage());
            return false;
        }
    }

    public void syncUpUnSyncedStockCardCodes() {
        if (sharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()) {
            return;
        }
        try {
            List<String> unSyncedStockCardCodes = FluentIterable
                    .from(stockMovementRepository.listUnSynced())
                    .transform(stockMovementItem -> stockMovementItem.getStockCard().getProduct().getCode())
                    .toList();

            final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
            lmisRestApi.syncUpUnSyncedStockCards(facilityId, unSyncedStockCardCodes);
            sharedPreferenceMgr.setLastMovementHandShakeDateToToday();
            boolean isAllStockCardSyncSuccessful = unSyncedStockCardCodes.isEmpty();
            if (isAllStockCardSyncSuccessful) {
                sharedPreferenceMgr.setStockLastSyncTime();
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.syncUpUnSyncedStockCardCodes").reportToFabric();
        }
    }

    public void fakeSyncUpUnSyncedStockCardCodes() {
        if (sharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()) {
            return;
        }
        try {
            List<String> unSyncedStockCardCodes = FluentIterable
                    .from(stockMovementRepository.listUnSynced())
                    .transform(stockMovementItem -> stockMovementItem.getStockCard().getProduct().getCode()).toList();
            sharedPreferenceMgr.setLastMovementHandShakeDateToToday();
            boolean isAllStockCardSyncSuccessful = unSyncedStockCardCodes.isEmpty();
            if (isAllStockCardSyncSuccessful) {
                sharedPreferenceMgr.setStockLastSyncTime();
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.fakeSyncUpUnSyncedStockCardCodes").reportToFabric();
        }
    }

    public void syncAppVersion() {
        try {
            if (!sharedPreferenceMgr.hasSyncedVersion()) {
                AppInfoRequest request = new AppInfoRequest(UserInfoMgr.getInstance().getFacilityCode(),
                        UserInfoMgr.getInstance().getUser().getUsername(), UserInfoMgr.getInstance().getVersion());
                lmisRestApi.updateAppVersion(request);
                sharedPreferenceMgr.setSyncedVersion(true);
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.syncArchivedProducts").reportToFabric();
        }
    }

    public void syncArchivedProducts() {

        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
        try {
            List<String> archivedProductCodes = productRepository.listArchivedProductCodes();
            lmisRestApi.syncUpArchivedProducts(facilityId, archivedProductCodes);
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.syncArchivedProducts").reportToFabric();
        }
    }

    public void syncUpCmms() {
        try {
            List<Cmm> unsyncedCmms = cmmRepository.listUnsynced();
            if (!unsyncedCmms.isEmpty()) {
                List<CmmEntry> cmmEntries = FluentIterable
                        .from(unsyncedCmms)
                        .transform(CmmEntry::createFrom)
                        .toList();

                lmisRestApi.syncUpCmms(UserInfoMgr.getInstance().getUser().getFacilityId(), cmmEntries);

                for (Cmm cmm : unsyncedCmms) {
                    cmm.setSynced(true);
                    cmmRepository.save(cmm);
                }
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.syncUpCmms").reportToFabric();
        }
    }

    public void fakeSyncUpCmms() {
        try {
            List<Cmm> unsyncedCmms = cmmRepository.listUnsynced();
            if (!unsyncedCmms.isEmpty()) {
                for (Cmm cmm : unsyncedCmms) {
                    cmm.setSynced(true);
                    cmmRepository.save(cmm);
                }
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.fakeSyncUpCmms").reportToFabric();
        }
    }

    public boolean syncDeleteMovement() {
        int unitCount = 30;
        String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
        List<DirtyDataItemInfo> itemInfos = dirtyDataRepository.listunSyced();

        if (!CollectionUtils.isEmpty(itemInfos)) {
            List<DirtyDataItemEntry> entries = FluentIterable.from(itemInfos).transform(new Function<DirtyDataItemInfo, DirtyDataItemEntry>() {
                @Nullable
                @Override
                public DirtyDataItemEntry apply(@Nullable DirtyDataItemInfo dirtyDataItemInfo) {
                    return new DirtyDataItemEntry(dirtyDataItemInfo.getProductCode(), dirtyDataItemInfo.getJsonData(), dirtyDataItemInfo.isFullyDelete());
                }
            }).toList();
            boolean isSyncedSuccessed = true;
            for (int start = 0; start < entries.size(); ) {
                int end = (start + unitCount) >= entries.size() ? entries.size() : start + unitCount;
                List<DirtyDataItemEntry> sub = entries.subList(start, end);
                try {
                    lmisRestApi.syncUpDeletedData(Long.parseLong(facilityId), sub);
                    List<DirtyDataItemInfo> itemInfoList = itemInfos.subList(start, end);
                    dbUtil.withDaoAsBatch(DirtyDataItemInfo.class, (DbUtil.Operation<DirtyDataItemInfo, Void>) dao -> {
                        for (DirtyDataItemInfo item : itemInfoList) {
                            item.setSynced(true);
                            dao.createOrUpdate(item);
                        }
                        return null;
                    });
                } catch (LMISException e) {
                    isSyncedSuccessed = false;
                    new LMISException(e, "SyncUpManager.syncUpDeletedData").reportToFabric();
                }
                start += unitCount;
            }
            return isSyncedSuccessed;
        } else {
            return true;
        }
    }

    private boolean submitRequisition(RnRForm rnRForm) {
        try {
            if (rnRForm.isEmergency()) {
                lmisRestApi.submitEmergencyRequisition(rnRForm);
            } else {
                lmisRestApi.submitRequisition(rnRForm);
            }
            syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RnRForm, rnRForm.getId());
            Log.d(TAG, "===> SyncRnr : synced ->");
            return true;
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.submitRequisition").reportToFabric();
            Log.e(TAG, "===> SyncRnr : sync failed ->" + e.getMessage());
            syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.RnRForm, rnRForm.getId()));
            return false;
        }
    }

    private boolean submitProgramDataForm(ProgramDataForm programDataForm) {
        try {
            lmisRestApi.syncUpProgramDataForm(programDataForm);
            Log.d(TAG, "===> SyncRapidTests: Rapid Tests synced...");
            return true;
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.submitProgramDataForm").reportToFabric();
            return false;
        }
    }

    private List<StockMovementEntry> convertStockMovementItemsToStockMovementEntriesForSync(final String facilityId,
                                                                                            List<StockMovementItem> stockMovementItems) {

        return FluentIterable.from(stockMovementItems).transform(stockMovementItem -> {
            if (stockMovementItem.getStockCard().getProduct() != null) {
                return new StockMovementEntry(stockMovementItem, facilityId);
            } else {
                return null;
            }
        }).toList();
    }

    private void markStockDataSynced(List<StockMovementItem> stockMovementItems) throws LMISException {

        Observable.from(stockMovementItems).forEach(stockMovementItem -> stockMovementItem.setSynced(true));

        stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotMovements(stockMovementItems);
    }

    private void markRnrFormSynced(RnRForm rnRForm) {
        rnRForm.setSynced(true);
        try {
            rnrFormRepository.createOrUpdateWithItems(rnRForm);
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.markRnrFormSynced").reportToFabric();
            Log.e(TAG, "===> SyncRnr : mark synced failed -> " + rnRForm.getId());
        }
    }

    public void syncRapidTestForms() {
        List<ProgramDataForm> forms;
        try {
            Log.d(TAG, "===> Preparing RapidTestForms for Syncing");
            forms = FluentIterable
                    .from(programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE))
                    .filter(programDataForm -> !programDataForm.isSynced()
                            && programDataForm.getStatus().equals(ProgramDataForm.STATUS.AUTHORIZED))
                    .toList();

            Log.d(TAG, "===> SyncRapidTestForms :" + forms.size() + " ProgramDataForm ready to sync...");

            if (forms.size() == 0) {
                return;
            }
        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.syncRapidTestForms").reportToFabric();
            return;
        }
        Observable.from(forms)
                .filter(this::submitProgramDataForm)
                .subscribe(this::markProgramDataFormsSynced);
    }

    public void fakeSyncRapidTestForms() {
        List<ProgramDataForm> forms;
        try {
            Log.d(TAG, "===> Preparing RapidTestForms for Syncing");
            forms = FluentIterable
                    .from(programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE))
                    .filter(programDataForm -> !programDataForm.isSynced()
                            && programDataForm.getStatus().equals(ProgramDataForm.STATUS.AUTHORIZED))
                    .toList();

            Log.d(TAG, "===> SyncRapidTestForms :" + forms.size() + " ProgramDataForm ready to sync...");

        } catch (LMISException e) {
            new LMISException(e, "SyncUpManager.fakeSyncRapidTestForms").reportToFabric();
            return;
        }


        for (ProgramDataForm form : forms) {
            markProgramDataFormsSynced(form);
        }
    }

    private void markProgramDataFormsSynced(ProgramDataForm programDataForm) {
        programDataForm.setSynced(true);
        try {
            programDataFormRepository.batchCreateOrUpdate(programDataForm);
        } catch (SQLException e) {
            new LMISException(e, "SyncUpManager.markProgramDataFormsSynced").reportToFabric();
            Log.e(TAG, "===> SyncRapidTests : mark synced failed -> " + programDataForm.getId());
        }
    }
}
