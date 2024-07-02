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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.event.CmmCalculateEvent;
import org.openlmis.core.event.SyncRnrFinishEvent;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.AdditionalProductProgramRepository;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.FacilityInfoResponse;
import org.openlmis.core.network.model.RnrFormStatusEntry;
import org.openlmis.core.network.model.RnrFormStatusRequest;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.service.sync.SchedulerBuilder;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class SyncDownManager {

  private static volatile boolean syncing = false;

  public static synchronized boolean isSyncing() {
    return SyncDownManager.syncing;
  }

  public static synchronized void setSyncing(boolean syncing) {
    SyncDownManager.syncing = syncing;
  }

  private static final int DAYS_OF_MONTH = 30;

  private static final String TAG = SyncDownManager.class.getSimpleName();

  protected LMISRestApi lmisRestApi;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;
  @Inject
  RnrFormRepository rnrFormRepository;
  @Inject
  StockRepository stockRepository;
  @Inject
  ProductRepository productRepository;
  @Inject
  ProductProgramRepository productProgramRepository;
  @Inject
  StockService stockService;
  @Inject
  SyncStockCardsLastYearSilently syncStockCardsLastYearSilently;
  @Inject
  DirtyDataManager dirtyDataManager;
  @Inject
  UserRepository userRepository;
  @Inject
  RegimenRepository regimenRepository;
  @Inject
  PodRepository podRepository;
  @Inject
  AdditionalProductProgramRepository additionalProductProgramRepository;
  @Inject
  ProgramRepository programRepository;

  public SyncDownManager() {
    lmisRestApi = LMISApp.getInstance().getRestApi();
  }

  public void syncDownServerData(Subscriber<SyncProgress> subscriber) {
    synchronized (SyncDownManager.class) {
      if (isSyncing()) {
        return;
      }
      setSyncing(true);
    }
    Observable.create((Observable.OnSubscribe<SyncProgress>) subscriber1 -> {
      try {
        syncDownFacilityInfo(subscriber1);
        syncDownRegimens(subscriber1);
        syncDownProducts(subscriber1);
        syncDownPods(subscriber1);
        syncDownLastMonthStockCards(subscriber1);
        syncDownRequisition(subscriber1);
        setSyncing(false);
        subscriber1.onCompleted();
      } catch (LMISException e) {
        setSyncing(false);
        subscriber1.onError(e);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
  }

  public void syncDownServerData() {
    syncDownServerData(new Subscriber<SyncProgress>() {
      @Override
      public void onCompleted() {
        if (sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
          EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.START));
          sharedPreferenceMgr.setIsSyncingLastYearStockCards(true);
          syncStockCardsLastYearSilently.performSync().subscribe(getSyncLastYearStockCardSubscriber());
        } else if (!sharedPreferenceMgr.shouldSyncLastYearStockData()
            && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
          if (TextUtils.isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
            EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.FINISH));
            sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
          }
        } else if (!sharedPreferenceMgr.shouldSyncLastYearStockData()
            && sharedPreferenceMgr.isSyncingLastYearStockCards()) {
          sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        }
      }

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
      }

      @Override
      public void onNext(SyncProgress syncProgress) {
        if (SyncProgress.SHOULD_REFRESH_ISSUE_VOUCHER_LIST == syncProgress) {
          EventBus.getDefault().post(Constants.REFRESH_ISSUE_VOUCHER_LIST);
        }
      }
    });
  }

  private void syncDownFacilityInfo(Subscriber<? super SyncProgress> subscriber)
      throws LMISException {
    try {
      subscriber.onNext(SyncProgress.SYNCING_FACILITY_INFO);
      fetchAndSaveFacilityInfo();
      subscriber.onNext(SyncProgress.FACILITY_INFO_SYNCED);
    } catch (LMISException e) {
      LMISException e1 = new LMISException(
          errorMessage(R.string.msg_fetching_facility_info_failed));
      e1.reportToFabric();
      throw e;
    }
  }

  private void syncDownRegimens(Subscriber<? super SyncProgress> subscriber) throws LMISException {
    try {
      subscriber.onNext(SyncProgress.SYNCING_REGIMENS);
      fetchAndSaveRegimens();
      subscriber.onNext(SyncProgress.REGIMENS_SYNCED);
    } catch (LMISException e) {
      LMISException e1 = new LMISException(errorMessage(R.string.msg_fetching_regimens_failed));
      e1.reportToFabric();
      throw e;
    }
  }

  private void syncDownPods(Subscriber<? super SyncProgress> subscriber) throws LMISException {
    try {
      subscriber.onNext(SyncProgress.SYNCING_PODS);
      if (fetchAndSavePods()) {
        subscriber.onNext(SyncProgress.SHOULD_REFRESH_ISSUE_VOUCHER_LIST);
      }
      sharedPreferenceMgr.setKeyIsPodDataSynced();
      subscriber.onNext(SyncProgress.PODS_SYNCED);
    } catch (LMISException e) {
      LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_pod_failed));
      e1.reportToFabric();
      throw e1;
    }
  }

  private boolean fetchAndSavePods() throws LMISException {
    List<Pod> pods;
    if (sharedPreferenceMgr.isPodDataInitialSynced()) {
      pods = lmisRestApi.fetchPods(true);
    } else {
      pods = lmisRestApi.fetchPods(false);
    }
    if (pods == null) {
      LMISException e = new LMISException("fetch pods info exception");
      e.reportToFabric();
      throw e;
    }
    ImmutableList<Pod> filteredPods = from(pods)
        .filter(pod -> {
          try {
            return podRepository.queryByOrderCode(Objects.requireNonNull(pod).getOrderCode()) == null;
          } catch (LMISException e) {
            return false;
          }
        })
        .toList();
    podRepository.batchCreatePodsWithItems(filteredPods);

    boolean hasNewPods = !filteredPods.isEmpty();
    if (hasNewPods) {
      saveNewShippedProgramNames(filteredPods);
    }

    return hasNewPods;
  }

  void saveNewShippedProgramNames(List<Pod> newPods) throws LMISException {
    ImmutableList<Pod> newShippedPods = from(newPods)
        .filter(pod -> pod.getOrderStatus() == OrderStatus.SHIPPED)
        .toList();

    ArrayList<String> shippedProgramCodes = new ArrayList<>();
    List<String> shippedProgramNames = new ArrayList<>();

    List<Program> programs = programRepository.list();
    HashMap<String, String> programCodeAndNamePair = new HashMap<>();
    for (Program program : programs) {
      programCodeAndNamePair.put(program.getProgramCode(), program.getProgramName());
    }

    for (Pod pod : newShippedPods) {
      String programCode = pod.getRequisitionProgramCode();
      if (!shippedProgramCodes.contains(programCode)) {
        shippedProgramCodes.add(programCode);
        shippedProgramNames.add(programCodeAndNamePair.get(programCode));
      }
    }

    String existingShippedProgramNames = sharedPreferenceMgr.getNewShippedProgramNames();
    if (existingShippedProgramNames != null) {
      for (String existingProgramName : existingShippedProgramNames.split(",")) {
        if (!shippedProgramNames.contains(existingProgramName)) {
          shippedProgramNames.add(existingProgramName);
        }
      }
    }

    sharedPreferenceMgr.setNewShippedProgramNames(
        shippedProgramNames.toString().replace("[", "").replace("]", "")
    );
  }

  @NonNull
  private Subscriber<List<StockCard>> getSyncLastYearStockCardSubscriber() {
    return new Subscriber<List<StockCard>>() {
      @Override
      public void onCompleted() {
        // do nothing
      }

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
        sharedPreferenceMgr.setStockCardLastYearSyncError(true);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        new LMISException(e, "getSyncLastYearStockCardSubscriber:onError").reportToFabric();
        EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.ERROR));
      }

      @Override
      public void onNext(List<StockCard> stockCards) {
        saveStockCardsFromLastYear(stockCards).subscribe(getSaveStockCardsSubscriber());
      }
    };
  }

  @NonNull
  private Subscriber<Object> getSaveStockCardsSubscriber() {
    return new Subscriber<Object>() {
      @Override
      public void onCompleted() {
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(false);
        sharedPreferenceMgr.setStockCardLastYearSyncError(false);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        sharedPreferenceMgr.setStockLastSyncTime();
        EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.FINISH));
      }

      @Override
      public void onError(Throwable e) {
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
        sharedPreferenceMgr.setStockCardLastYearSyncError(true);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.ERROR));
      }

      @Override
      public void onNext(Object aVoid) {
        // do nothing
      }
    };
  }

  private void syncDownRequisition(Subscriber<? super SyncProgress> subscriber)
      throws LMISException {
    subscriber.onNext(SyncProgress.SYNCING_REQUISITION);

    if (!sharedPreferenceMgr.isRequisitionDataSynced()) {
      syncDownFullRequisitions(subscriber);
    } else {
      // incremental sync down due to superior can create requisition for subordinate
      syncDownIncrementalNonEmergencyRequisitions();
      // pulling requisition status
      syncDownRequisitionsStatus();
      // would not update wrong data in the process of syncing up, so notifying UI here
      EventBus.getDefault().post(new SyncRnrFinishEvent());
    }

    subscriber.onNext(SyncProgress.REQUISITION_SYNCED);
  }

  private void syncDownRequisitionsStatus() throws LMISException {
    try {
      List<RnRForm> inApprovalForms = rnrFormRepository.listAllInApprovalForms();
      if (!inApprovalForms.isEmpty()) {
        List<RnrFormStatusRequest> request = from(inApprovalForms)
            .filter(rnRForm -> rnRForm != null)
            .transform(RnRForm::convertToRequisitionsStatusRequest)
            .toList();

        List<RnrFormStatusEntry> requisitionStatusEntries = lmisRestApi.fetchRequisitionsStatus(request);
        if (!requisitionStatusEntries.isEmpty()) {
          List<RnrFormStatusEntry> validRnrFormStatusEntry = from(requisitionStatusEntries)
              .filter(
                  rnrFormStatusEntry -> rnrFormStatusEntry != null && rnrFormStatusEntry.isValidStatus()
              ).toList();
          rnrFormRepository.updateFormsStatusAndDeleteRejectedFormsSignatures(validRnrFormStatusEntry);
        }
      }
    } catch (LMISException e) {
      LMISException wrappedException = new LMISException(
          e, errorMessage(R.string.msg_sync_requisition_failed)
      );
      wrappedException.reportToFabric();
      throw wrappedException;
    }
  }

  private void syncDownIncrementalNonEmergencyRequisitions() throws LMISException {
    try {
      String incrementalStartDate = getIncrementalStartDate();
      if (incrementalStartDate == null) {
        return;
      }

      SyncDownRequisitionsResponse syncDownRequisitionsResponse =
          fetchRequisition(incrementalStartDate);
      List<RnRForm> requisitionResponseList = syncDownRequisitionsResponse.getRequisitionResponseList();
      if (requisitionResponseList != null && !requisitionResponseList.isEmpty()) {
        List<RnRForm> nonEmergencyForms = from(requisitionResponseList)
            .filter(rnRForm -> rnRForm != null && !rnRForm.isEmergency())
            .toList();

        if (nonEmergencyForms != null && !nonEmergencyForms.isEmpty()) {
          rnrFormRepository.saveAndDeleteDuplicatedPeriodRequisitions(nonEmergencyForms);
        }
      }
    } catch (LMISException e) {
      LMISException wrappedException = new LMISException(
          e, errorMessage(R.string.msg_sync_requisition_failed)
      );
      wrappedException.reportToFabric();
      throw wrappedException;
    }
  }

  @Nullable
  private String getIncrementalStartDate() throws LMISException {
    Date startDate;

    RnRForm oldestSyncedProgramRnRForm = rnrFormRepository.queryOldestSyncedRnRFormGroupByProgram();
    if (oldestSyncedProgramRnRForm == null) {
      return null;
    } else {
      startDate = DateUtil.dateMinusMonth(oldestSyncedProgramRnRForm.getPeriodBegin(), -1);
    }

    if (DateUtil.dateMinusMonth(startDate, -13).compareTo(DateUtil.getCurrentDate()) < 0) {
      return getStartDate();
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.set(Calendar.DAY_OF_MONTH, 1);

    return DateUtil.formatDate(calendar.getTime(), DateUtil.DB_DATE_FORMAT);
  }

  private void syncDownFullRequisitions(Subscriber<? super SyncProgress> subscriber) throws LMISException {
    try {
      fetchAndSaveRequisition();
      sharedPreferenceMgr.setRequisitionDataSynced(true);
    } catch (LMISException e) {
      sharedPreferenceMgr.setRequisitionDataSynced(false);
      LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_requisition_failed));
      e1.reportToFabric();
      throw e1;
    }
  }

  private void syncDownLastMonthStockCards(Subscriber<? super SyncProgress> subscriber) throws LMISException {

    if (!sharedPreferenceMgr.isLastMonthStockDataSynced()) {
      try {
        subscriber.onNext(SyncProgress.SYNCING_STOCK_CARDS_LAST_MONTH);
        fetchLatestOneMonthMovements();
        sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
      } catch (LMISException e) {
        sharedPreferenceMgr.setLastMonthStockCardDataSynced(false);
        Log.w(TAG, e);
        LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_stock_movement_failed));
        e1.reportToFabric();
        throw e1;
      }
    } else {
      dirtyDataManager.initialDirtyDataCheck();
    }
    subscriber.onNext(SyncProgress.STOCK_CARDS_LAST_MONTH_SYNCED);
  }

  private void syncDownProducts(Subscriber<? super SyncProgress> subscriber) throws LMISException {
    try {
      subscriber.onNext(SyncProgress.SYNCING_PRODUCT);
      fetchAndSaveProductsWithProgramsAndKits();
      subscriber.onNext(SyncProgress.PRODUCT_SYNCED);
    } catch (LMISException e) {
      LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_products_list_failed));
      e1.reportToFabric();
      throw e1;
    }
  }

  private void fetchAndSaveProductsWithProgramsAndKits() throws LMISException {
    SyncDownLatestProductsResponse response = getSyncDownLatestProductResponse();
    productRepository.saveProductAndProductProgram(response);
  }

  private SyncDownLatestProductsResponse getSyncDownLatestProductResponse() throws LMISException {
    boolean isFirstLoginVersion200 = sharedPreferenceMgr.getKeyIsFirstLoginVersion200();
    return lmisRestApi.fetchLatestProducts(
        isFirstLoginVersion200 ? null : sharedPreferenceMgr.getLastSyncProductTime());
  }

  private void fetchAndSaveStockCards(String startDate, String endDate) throws LMISException {
    StockCardsLocalResponse adaptedResponse = lmisRestApi.fetchStockMovementData(startDate, endDate);
    try {
      stockRepository.batchCreateSyncDownStockCardsAndMovements(adaptedResponse.getStockCards());
    } catch (SQLException e) {
      new LMISException(e, "fetchAndSaveStockCards exception").reportToFabric();
    }
  }

  public Observable<Object> saveStockCardsFromLastYear(final List<StockCard> stockCards) {
    if (stockCards.isEmpty()) {
      return Observable.empty();
    }
    EventBus.getDefault().post(new CmmCalculateEvent(true));
    return Observable.create(subscriber -> {
      try {
        TransactionManager
            .callInTransaction(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), () -> {
              stockRepository.batchSaveLastYearMovements(stockCards);
              stockCards.clear();
              stockService.immediatelyUpdateAvgMonthlyConsumption();
              return null;
            });
        subscriber.onCompleted();
      } catch (Exception e) {
        subscriber.onError(e);
      }
    }).observeOn(SchedulerBuilder.createScheduler())
    .doOnTerminate(() -> EventBus.getDefault().post(new CmmCalculateEvent(false)));
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
    userRepository.saveFacilityInfo(facilityInfoResponse);
    sharedPreferenceMgr.setReportTypesData(facilityInfoResponse.getSupportedReportTypes());
  }

  private void fetchAndSaveRegimens() throws LMISException {
    SyncDownRegimensResponse syncDownRegimensResponse = lmisRestApi.fetchRegimens();
    if (syncDownRegimensResponse == null) {
      LMISException e = new LMISException("fetch regimen exception");
      e.reportToFabric();
      throw e;
    }
    regimenRepository.batchSave(syncDownRegimensResponse.getRegimenList());
  }

  private void fetchAndSaveRequisition() throws LMISException {
    SyncDownRequisitionsResponse syncDownRequisitionsResponse = fetchRequisition(getStartDate());

    rnrFormRepository.createRnRsWithItems(
        syncDownRequisitionsResponse.getRequisitionResponseList());
  }

  @NonNull
  private SyncDownRequisitionsResponse fetchRequisition(@NonNull String startDate) throws LMISException {
    SyncDownRequisitionsResponse syncDownRequisitionsResponse =
        lmisRestApi.fetchRequisitions(startDate);

    if (syncDownRequisitionsResponse == null) {
      LMISException e = new LMISException(
          "Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
      e.reportToFabric();
      throw e;
    }
    return syncDownRequisitionsResponse;
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
    SYNC_LAST_SYNC_PRODUCT_FAIL,
    SYNC_LAST_MONTH_STOCK_DATA_FAIL,
    SYNC_REQUISITION_DATA_FAIL,
    SYNC_LAST_DATA_SUCCESS
  }

  public enum SyncProgress {
    SYNCING_FACILITY_INFO(R.string.msg_fetching_facility_info),
    SYNCING_REGIMENS(R.string.msg_fetching_regimens),
    SYNCING_SERVICE_LIST(R.string.msg_service_lists),
    SYNCING_PRODUCT(R.string.msg_fetching_products),
    SYNCING_STOCK_CARDS_LAST_MONTH(R.string.msg_sync_stock_movements_data),
    SYNCING_REQUISITION(R.string.msg_sync_requisition_data),
    SYNCING_RAPID_TESTS,
    SYNCING_PODS(R.string.msg_fetching_pods),

    PRODUCT_SYNCED,
    SERVICE_SYNCED,
    FACILITY_INFO_SYNCED,
    REGIMENS_SYNCED,
    STOCK_CARDS_LAST_MONTH_SYNCED,
    REQUISITION_SYNCED,
    STOCK_CARDS_LAST_YEAR_SYNCED,
    RAPID_TESTS_SYNCED,
    PODS_SYNCED,
    SHOULD_REFRESH_ISSUE_VOUCHER_LIST,
    SHOULD_GO_TO_INITIAL_INVENTORY;

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
