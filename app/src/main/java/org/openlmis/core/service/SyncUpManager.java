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

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.CmmEntry;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.List;

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
    SharedPreferenceMgr sharedPreferenceMgr;

    @Inject
    StockRepository stockRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    CmmRepository cmmRepository;

    @Inject
    ProgramDataFormRepository programDataFormRepository;

    @Inject
    private SyncErrorsRepository syncErrorsRepository;

    protected LMISRestApi lmisRestApi;

    public SyncUpManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
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
            e.reportToFabric();
            return false;
        }

        Observable.from(forms).filter(new Func1<RnRForm, Boolean>() {
            @Override
            public Boolean call(RnRForm rnRForm) {
                try {
                    syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RnRForm, rnRForm.getId());
                    Log.d(TAG, "===> SyncRnr : synced ->");
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "===> SyncRnr : sync failed ->" + e.getMessage());
                    syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.RnRForm, rnRForm.getId()));
                    return false;
                }
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
        try {
            List<StockMovementItem> stockMovementItems = stockRepository.listUnSynced();
            if (stockMovementItems.isEmpty()) {
                return false;
            }

            final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
            List<StockMovementEntry> movementEntriesToSync = convertStockMovementItemsToStockMovementEntriesForSync(facilityId, stockMovementItems);

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

    public boolean fakeSyncStockCards() {
        try {
            List<StockMovementItem> stockMovementItems = stockRepository.listUnSynced();
            if (stockMovementItems.isEmpty()) {
                return false;
            }
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

    public void syncUpUnSyncedStockCardCodes() {
        if (sharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()) {
            return;
        }
        try {
            List<String> unSyncedStockCardCodes = FluentIterable.from(stockRepository.listUnSynced()).transform(new Function<StockMovementItem, String>() {
                @Override
                public String apply(StockMovementItem stockMovementItem) {
                    return stockMovementItem.getStockCard().getProduct().getCode();
                }
            }).toList();

            final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
            lmisRestApi.syncUpUnSyncedStockCards(facilityId, unSyncedStockCardCodes);
            sharedPreferenceMgr.setLastMovementHandShakeDateToToday();
            boolean isAllStockCardSyncSuccessful = unSyncedStockCardCodes.isEmpty();
            if (isAllStockCardSyncSuccessful) {
                sharedPreferenceMgr.setStockLastSyncTime();
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void fakeSyncUpUnSyncedStockCardCodes() {
        if (sharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()) {
            return;
        }
        try {
            List<String> unSyncedStockCardCodes = FluentIterable.from(stockRepository.listUnSynced()).transform(new Function<StockMovementItem, String>() {
                @Override
                public String apply(StockMovementItem stockMovementItem) {
                    return stockMovementItem.getStockCard().getProduct().getCode();
                }
            }).toList();
            sharedPreferenceMgr.setLastMovementHandShakeDateToToday();
            boolean isAllStockCardSyncSuccessful = unSyncedStockCardCodes.isEmpty();
            if (isAllStockCardSyncSuccessful) {
                sharedPreferenceMgr.setStockLastSyncTime();
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void syncAppVersion() {
        try {
            if (!sharedPreferenceMgr.hasSyncedVersion()) {
                AppInfoRequest request = new AppInfoRequest(UserInfoMgr.getInstance().getFacilityCode(), UserInfoMgr.getInstance().getUser().getUsername(), UserInfoMgr.getInstance().getVersion());
                lmisRestApi.updateAppVersion(request);
                sharedPreferenceMgr.setSyncedVersion(true);
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void syncArchivedProducts() {

        final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
        try {
            List<String> archivedProductCodes = productRepository.listArchivedProductCodes();
            lmisRestApi.syncUpArchivedProducts(facilityId, archivedProductCodes);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void syncUpCmms() {
        try {
            List<Cmm> unsyncedCmms = cmmRepository.listUnsynced();
            if (!unsyncedCmms.isEmpty()) {
                List<CmmEntry> cmmEntries = FluentIterable.from(unsyncedCmms).transform(new Function<Cmm, CmmEntry>() {
                    @Override
                    public CmmEntry apply(Cmm cmm) {
                        return CmmEntry.createFrom(cmm);
                    }
                }).toList();

                lmisRestApi.syncUpCmms(UserInfoMgr.getInstance().getUser().getFacilityId(), cmmEntries);

                for (Cmm cmm : unsyncedCmms) {
                    cmm.setSynced(true);
                    cmmRepository.save(cmm);
                }
            }
        } catch (LMISException e) {
            e.reportToFabric();
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
            e.reportToFabric();
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
            e.reportToFabric();
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
            e.reportToFabric();
            return false;
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

        stockRepository.batchCreateOrUpdateStockMovementsAndLotMovements(stockMovementItems);
    }

    private void markRnrFormSynced(RnRForm rnRForm) {
        rnRForm.setSynced(true);
        try {
            rnrFormRepository.createOrUpdateWithItems(rnRForm);
        } catch (LMISException e) {
            e.reportToFabric();
            Log.e(TAG, "===> SyncRnr : mark synced failed -> " + rnRForm.getId());
        }
    }

    public void syncRapidTestForms() {
        List<ProgramDataForm> forms;
        try {
            Log.d(TAG, "===> Preparing RapidTestForms for Syncing");
            forms = FluentIterable.from(programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE)).filter(new Predicate<ProgramDataForm>() {
                @Override
                public boolean apply(ProgramDataForm programDataForm) {
                    return !programDataForm.isSynced() && programDataForm.getStatus().equals(ProgramDataForm.STATUS.AUTHORIZED);
                }
            }).toList();

            Log.d(TAG, "===> SyncRapidTestForms :" + forms.size() + " ProgramDataForm ready to sync...");

            if (forms.size() == 0) {
                return;
            }
        } catch (LMISException e) {
            e.reportToFabric();
            return;
        }
        Observable.from(forms).filter(new Func1<ProgramDataForm, Boolean>() {
            @Override
            public Boolean call(ProgramDataForm programDataForm) {
                return submitProgramDataForm(programDataForm);
            }
        }).subscribe(new Action1<ProgramDataForm>() {
            @Override
            public void call(ProgramDataForm programDataForm) {
                markProgramDataFormsSynced(programDataForm);
            }
        });
    }

    public void fakeSyncRapidTestForms() {
        List<ProgramDataForm> forms;
        try {
            Log.d(TAG, "===> Preparing RapidTestForms for Syncing");
            forms = FluentIterable.from(programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE)).filter(new Predicate<ProgramDataForm>() {
                @Override
                public boolean apply(ProgramDataForm programDataForm) {
                    return !programDataForm.isSynced() && programDataForm.getStatus().equals(ProgramDataForm.STATUS.AUTHORIZED);
                }
            }).toList();

            Log.d(TAG, "===> SyncRapidTestForms :" + forms.size() + " ProgramDataForm ready to sync...");

        } catch (LMISException e) {
            e.reportToFabric();
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
            new LMISException(e).reportToFabric();
            Log.e(TAG, "===> SyncRapidTests : mark synced failed -> " + programDataForm.getId());
        }
    }
}
