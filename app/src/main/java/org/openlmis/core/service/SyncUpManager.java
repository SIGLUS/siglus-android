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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.event.SyncPodFinishEvent;
import org.openlmis.core.event.SyncRnrFinishEvent;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NetWorkException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.CmmEntry;
import org.openlmis.core.network.model.DirtyDataItemEntry;
import org.openlmis.core.network.model.PodEntry;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.network.model.SyncUpStockMovementDataSplitResponse;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.Sets;
import rx.Observable;

@Singleton
@SuppressWarnings("PMD")
public class SyncUpManager {

  private static volatile boolean syncing = false;

  public static synchronized boolean isSyncing() {
    return SyncUpManager.syncing;
  }

  public static synchronized void setSyncing(boolean syncing) {
    SyncUpManager.syncing = syncing;
  }

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
  private PodRepository podRepository;

  @Inject
  DbUtil dbUtil;

  protected LMISRestApi lmisRestApi;

  public SyncUpManager() {
    lmisRestApi = LMISApp.getInstance().getRestApi();
  }

  public void syncUpData() {
    Log.d(TAG, "sync Up Data start");
    synchronized (SyncUpManager.class) {
      if (isSyncing()) {
        return;
      }
      setSyncing(true);
    }
    Log.d(TAG, "sync Up Data start " + isSyncing());

    setSyncing(true);
    boolean isSyncDeleted = syncDeleteMovement();
    if (isSyncDeleted) {
      boolean isSyncRnrSuccessful = syncRnr();
      if (isSyncRnrSuccessful) {
        sharedPreferenceMgr.setRnrLastSyncTime();
        EventBus.getDefault().post(new SyncRnrFinishEvent());
      }

      boolean isSyncPodSuccessful = syncPod();
      if (isSyncPodSuccessful) {
        sharedPreferenceMgr.setPodLastSyncTime();
      }
      EventBus.getDefault().post(new SyncPodFinishEvent());

      boolean isSyncStockSuccessful = syncStockCards();
      if (isSyncStockSuccessful) {
        sharedPreferenceMgr.setStockLastSyncTime();
        syncArchivedProducts();
      }

      // syncUpUnSyncedStockCardCodes();
      syncAppVersion();
      syncUpCmms();
    }
    Log.d(TAG, "sync Up Data end");
    setSyncing(false);
    if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && TextUtils
        .isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
      EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.FINISH));
    }
  }

  public boolean syncRnr() {
    List<RnRForm> forms;
    try {
      Log.d(TAG, "===> Preparing RnrForm for Syncing: Delete Deactivated Products...");
      forms = rnrFormRepository.queryAllUnsyncedForms();

      Log.d(TAG, "===> SyncRnR :" + forms.size() + " RnrForm ready to sync...");

      if (forms.isEmpty()) {
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
      forms = rnrFormRepository.queryAllUnsyncedForms();
      if (forms.isEmpty()) {
        return false;
      }
    } catch (LMISException e) {
      new LMISException(e, "SyncUpManager:fakeSyncRnr").reportToFabric();
      return false;
    }

    Observable.from(forms).filter(rnRForm -> {
      try {
        syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RNR_FORM, rnRForm.getId());
        return true;
      } catch (Exception e) {
        new LMISException(e, "SyncUpManager:fakeSyncRnr,sync failed").reportToFabric();
        syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.RNR_FORM, rnRForm.getId()));
        return false;
      }
    }).subscribe(this::markRnrFormSynced);

    return from(forms).allMatch(RnRForm::isSynced);
  }

  public boolean syncPod() {
    List<Pod> pods;
    try {
      pods = podRepository.queryUnsyncedPods();
      if (pods.isEmpty()) {
        return false;
      }
    } catch (Exception e) {
      new LMISException(e, "SyncUpManager:syncPod").reportToFabric();
      return false;
    }
    boolean allSubmitSuccess = true;
    for (Pod pod : pods) {
      Pod submittedPod = submitPod(pod);
      if (!submittedPod.isSynced()) {
        allSubmitSuccess = false;
      }
    }
    return allSubmitSuccess;
  }

  public boolean syncStockCards() {
    try {
      List<StockMovementItem> stockMovementItems = stockMovementRepository.listUnSynced();
      if (stockMovementItems.isEmpty()) {
        return false;
      }

      List<StockMovementEntry> movementEntriesToSync =
          convertStockMovementItemsToStockMovementEntriesForSync(stockMovementItems);

      if (movementEntriesToSync == null || movementEntriesToSync.isEmpty()) {
        new LMISException("SyncUpManager.movementEntriesToSync").reportToFabric();
        return false;
      }
      SyncUpStockMovementDataSplitResponse response = lmisRestApi.syncUpStockMovementDataSplit(movementEntriesToSync);

      List<StockMovementItem> shouldMarkSyncedItems = new ArrayList<>();

      Set<String> syncFailedProduct = Sets.newHashSet(response.getErrorProductCodes());
      for (StockMovementItem stockMovementItem : stockMovementItems) {
        if (CollectionUtils.isEmpty(syncFailedProduct)
            || !syncFailedProduct
            .contains(stockMovementItem.getStockCard().getProduct().getCode())) {
          shouldMarkSyncedItems.add(stockMovementItem);
        }
      }

      markStockDataSynced(shouldMarkSyncedItems);
      syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.STOCK_CARDS, 0L);
      if (!response.getErrorProductCodes().isEmpty()) {
        syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.SYNC_MOVEMENT, 2L);
        saveStockMovementErrors(response);
        return false;
      }
      sharedPreferenceMgr.setStockMovementSyncError("");
      Log.d(TAG, "===> SyncStockMovement : synced");
      return true;
    } catch (LMISException exception) {
      new LMISException(exception, "SyncUpManager.syncStockCards").reportToFabric();
      EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.ERROR, exception.getMessage()));
      syncErrorsRepository.save(new SyncError(exception.getMessage(), SyncType.STOCK_CARDS, 0L));
      Log.e(TAG, "===> SyncStockMovement : synced failed ->" + exception.getMessage());
      return false;
    }
  }

  private void saveStockMovementErrors(SyncUpStockMovementDataSplitResponse response) {
    List<String> syncErrorProduct = FluentIterable.from(response.getErrorProductCodes()).limit(3).toList();
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.ERROR, syncErrorProduct.toString()));
    sharedPreferenceMgr.setStockMovementSyncError(syncErrorProduct.toString());
    syncErrorsRepository.save(new SyncError(response.getErrorProductCodes().toString(), SyncType.SYNC_MOVEMENT, 2L));
  }

  public boolean fakeSyncStockCards() {
    try {
      List<StockMovementItem> stockMovementItems = stockMovementRepository.listUnSynced();
      if (stockMovementItems.isEmpty()) {
        return false;
      }
      markStockDataSynced(stockMovementItems);
      syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.STOCK_CARDS, 0L);
      Log.d(TAG, "===> SyncStockMovement : synced");
      return true;
    } catch (LMISException exception) {
      new LMISException(exception, "SyncUpManager.fakeSyncStockCards").reportToFabric();
      syncErrorsRepository.save(new SyncError(exception.getMessage(), SyncType.STOCK_CARDS, 0L));
      Log.e(TAG, "===> SyncStockMovement : synced failed ->" + exception.getMessage());
      return false;
    }
  }

  public void fakeSyncUpUnSyncedStockCardCodes() {
    if (sharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()) {
      return;
    }
    try {
      List<String> unSyncedStockCardCodes = FluentIterable
          .from(stockMovementRepository.listUnSynced())
          .transform(stockMovementItem -> stockMovementItem.getStockCard().getProduct().getCode())
          .toList();
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
            UserInfoMgr.getInstance().getUser().getUsername(),
            UserInfoMgr.getInstance().getVersion());
        lmisRestApi.updateAppVersion(request);
        sharedPreferenceMgr.setSyncedVersion(true);
      }
    } catch (LMISException e) {
      new LMISException(e, "SyncUpManager.syncArchivedProducts").reportToFabric();
    }
  }

  public void syncArchivedProducts() {
    try {
      List<String> archivedProductCodes = productRepository.listArchivedProductCodes();
      lmisRestApi.syncUpArchivedProducts(archivedProductCodes);
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

        lmisRestApi.syncUpCmms(cmmEntries);

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
    List<DirtyDataItemInfo> itemInfos = dirtyDataRepository.listunSyced();
    if (!CollectionUtils.isEmpty(itemInfos)) {
      Gson gson = new Gson();
      Type type = new TypeToken<List<StockMovementEntry>>() {
      }.getType();
      List<DirtyDataItemEntry> entries = FluentIterable.from(itemInfos)
          .transform(new Function<DirtyDataItemInfo, DirtyDataItemEntry>() {
            @Nullable
            @Override
            public DirtyDataItemEntry apply(@Nullable DirtyDataItemInfo dirtyDataItemInfo) {
              Objects.requireNonNull(dirtyDataItemInfo);
              return new DirtyDataItemEntry(dirtyDataItemInfo.getProductCode(),
                  gson.fromJson(dirtyDataItemInfo.getJsonData(), type), dirtyDataItemInfo.isFullyDelete());
            }
          }).toList();
      boolean isSyncedSuccessed = true;
      for (int start = 0; start < entries.size(); ) {
        int end = Math.min((start + unitCount), entries.size());
        List<DirtyDataItemEntry> sub = entries.subList(start, end);
        try {
          lmisRestApi.syncUpDeletedData(sub);
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
      syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RNR_FORM, rnRForm.getId());
      Log.d(TAG, "===> SyncRnr : synced ->");
      return true;
    } catch (NetWorkException e) {
      new LMISException(e, "SyncUpManager.submitRequisition").reportToFabric();
      Log.e(TAG, "===> SyncRnr : sync failed ->" + e.getMessage());
      return false;
    } catch (LMISException e) {
      new LMISException(e, "SyncUpManager.submitRequisition").reportToFabric();
      Log.e(TAG, "===> SyncRnr : sync failed ->" + e.getMessage());
      syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.RNR_FORM, rnRForm.getId()));
      return false;
    }
  }

  private List<StockMovementEntry> convertStockMovementItemsToStockMovementEntriesForSync(
      List<StockMovementItem> stockMovementItems) {

    return FluentIterable.from(stockMovementItems).transform(stockMovementItem -> {
      if (stockMovementItem.getStockCard().getProduct() != null) {
        return new StockMovementEntry(stockMovementItem);
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
      for (RnrFormItem rnrFormItem : rnRForm.getRnrFormItemListWrapper()) {
        if (rnrFormItem.getValidate() != null) {
          rnrFormItem.setValidate(DateUtil.convertDate(rnrFormItem.getValidate(), DateUtil.DB_DATE_FORMAT,
              DateUtil.SIMPLE_DATE_FORMAT));
        }
      }
      rnrFormRepository.createOrUpdateWithItems(rnRForm);
    } catch (LMISException e) {
      new LMISException(e, "SyncUpManager.markRnrFormSynced").reportToFabric();
      Log.e(TAG, "===> SyncRnr : mark synced failed -> " + rnRForm.getId());
    }
  }

  private Pod submitPod(Pod localPod) {
    try {
      Pod remotePod = lmisRestApi.submitPod(new PodEntry(localPod));
      remotePod.setId(localPod.getId());
      remotePod.setOriginOrderCode(localPod.getOriginOrderCode());
      remotePod.setSynced(podRepository.markSynced(remotePod));
      return remotePod;
    } catch (NetWorkException e) {
      new LMISException(e, "SyncUpManager.submitPod.network").reportToFabric();
    } catch (LMISException e) {
      new LMISException(e, "SyncUpManager.submitPod.lmis").reportToFabric();
      syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.POD, localPod.getId());
      syncErrorsRepository.save(new SyncError(e.getMessage(), SyncType.POD, localPod.getId()));
    }
    return localPod;
  }
}
