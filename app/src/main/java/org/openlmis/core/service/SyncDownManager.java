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

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
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
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.ServiceFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.ProgramCacheManager;
import org.openlmis.core.network.model.FacilityInfoResponse;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.SupportedProgram;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownProgramDataResponse;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownServiceResponse;
import org.openlmis.core.service.sync.SchedulerBuilder;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
@SuppressWarnings("PMD")
public class SyncDownManager {

  private static final int DAYS_OF_MONTH = 30;
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
  @Inject
  RegimenRepository regimenRepository;

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
        syncDownRegimens(subscriber1);
        // syncDownService(subscriber1);
        syncDownProducts(subscriber1);
        syncDownLastMonthStockCards(subscriber1);
        // syncDownRequisition(subscriber1);
        // syncDownRapidTests(subscriber1);
        isSyncing = false;
        subscriber1.onCompleted();
      } catch (LMISException e) {
        isSyncing = false;
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


  private void syncDownService(Subscriber<? super SyncProgress> subscriber) throws LMISException {
    try {
      subscriber.onNext(SyncProgress.SYNCING_SERVICE_LIST);
      fetchAndSaveService();
      subscriber.onNext(SyncProgress.SERVICE_SYNCED);
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

  private void syncDownRapidTests(Subscriber<? super SyncProgress> subscriber)
      throws LMISException {
    if (!sharedPreferenceMgr.isRapidTestDataSynced()) {
      try {
        subscriber.onNext(SyncProgress.SYNCING_RAPID_TESTS);
        fetchAndSaveRapidTests();
        sharedPreferenceMgr.setRapidTestsDataSynced(true);
        subscriber.onNext(SyncProgress.RAPID_TESTS_SYNCED);
      } catch (LMISException e) {
        sharedPreferenceMgr.setRapidTestsDataSynced(false);
        LMISException e1 = new LMISException(e, errorMessage(R.string.msg_sync_rapid_tests_failed));
        e1.reportToFabric();
        throw e1;
      }
    }
  }

  private void fetchAndSaveRapidTests() throws LMISException {
    SyncDownProgramDataResponse syncDownProgramDataResponse = lmisRestApi
        .fetchProgramDataForms(Long.parseLong(UserInfoMgr.getInstance().getUser().getFacilityId()));
    if (syncDownProgramDataResponse == null) {
      LMISException e = new LMISException(
          "Can't get SyncDownRapidTestsResponse, you can check json parse to POJO logic");
      e.reportToFabric();
      throw e;
    }

    programDataFormRepository.batchSaveForms(syncDownProgramDataResponse.getProgramDataForms());
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
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        sharedPreferenceMgr.setStockLastSyncTime();
        EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.FINISH));
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
        // do nothing
      }
    };
  }

  private void sendSyncErrorBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.ERROR));
  }

  private void syncDownRequisition(Subscriber<? super SyncProgress> subscriber)
      throws LMISException {
    if (!sharedPreferenceMgr.isRequisitionDataSynced()) {
      try {
        subscriber.onNext(SyncProgress.SYNCING_REQUISITION);
        fetchAndSaveRequisition();
        sharedPreferenceMgr.setRequisitionDataSynced(true);
        subscriber.onNext(SyncProgress.REQUISITION_SYNCED);
      } catch (LMISException e) {
        sharedPreferenceMgr.setRequisitionDataSynced(false);
        LMISException e1 = new LMISException(errorMessage(R.string.msg_sync_requisition_failed));
        e1.reportToFabric();
        throw e1;
      }
    }
  }

  private void syncDownLastMonthStockCards(Subscriber<? super SyncProgress> subscriber)
      throws LMISException {

    if (!sharedPreferenceMgr.isLastMonthStockDataSynced()) {
      try {
        // 1 re-sync && if(stockRepository.list()!=null) do not goto initial inventory
        // 2 initial inventory
        subscriber.onNext(SyncProgress.SYNCING_STOCK_CARDS_LAST_MONTH);
        fetchLatestOneMonthMovements();
        sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
      } catch (LMISException e) {
        sharedPreferenceMgr.setLastMonthStockCardDataSynced(false);
        Log.w(TAG, e);
        LMISException e1 = new LMISException(errorMessage(R.string.msg_sync_stock_movement_failed));
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
      sharedPreferenceMgr
          .removeShowUpdateBannerTextWhenReactiveProduct(existingProduct.getPrimaryName());
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
    return lmisRestApi.fetchLatestProducts(
        isFirstLoginVersion87 ? null : sharedPreferenceMgr.getLastSyncProductTime());
  }

  private void fetchAndSaveStockCards(String startDate, String endDate) throws LMISException {
    StockCardsLocalResponse adaptedResponse = lmisRestApi.fetchStockMovementData(startDate, endDate);
    try {
      stockRepository.batchCreateSyncDownStockCardsAndMovements(adaptedResponse.getStockCards());
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
      int endPosition = arrayNumber == threadNumber ? stockCards.size()
          : numberOfElementsInAListForAnObservable * arrayNumber;
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
    User user = UserInfoMgr.getInstance().getUser();
    user.setFacilityCode(facilityInfoResponse.getCode());
    user.setFacilityName(facilityInfoResponse.getName());
    userRepository.createOrUpdate(user);
    UserInfoMgr.getInstance().setUser(user);
    List<Program> programs = covertFacilityInfoToProgram(facilityInfoResponse);
    ProgramCacheManager.addPrograms(programs);
    programRepository.batchCreateOrUpdatePrograms(programs);
    List<ReportTypeForm> reportTypeForms = covertFacilityInfoToReportTypeForm(facilityInfoResponse);
    sharedPreferenceMgr.setReportTypesData(reportTypeForms);
    reportTypeFormRepository.batchCreateOrUpdateReportTypes(reportTypeForms);
  }

  private List<ReportTypeForm> covertFacilityInfoToReportTypeForm(
      FacilityInfoResponse facilityInfoResponse) {
    List<ReportTypeForm> reportTypeForms = new ArrayList<>();
    for (SupportedProgram supportedProgram : facilityInfoResponse.getSupportedPrograms()) {
      ReportTypeForm reportTypeForm = ReportTypeForm
          .builder()
          .code(supportedProgram.getCode())
          .name(supportedProgram.getName())
          .active(supportedProgram.isSupportActive())
          .startTime(
              DateUtil.parseString(supportedProgram.getSupportStartDate(), DateUtil.DB_DATE_FORMAT))
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
    final String facilityCode = UserInfoMgr.getInstance().getUser().getFacilityCode();
    SyncDownRequisitionsResponse syncDownRequisitionsResponse = lmisRestApi
        .fetchRequisitions(facilityCode, getStartDate());
    if (syncDownRequisitionsResponse == null) {
      LMISException e = new LMISException(
          "Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
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

    PRODUCT_SYNCED,
    SERVICE_SYNCED,
    FACILITY_INFO_SYNCED,
    REGIMENS_SYNCED,
    STOCK_CARDS_LAST_MONTH_SYNCED,
    REQUISITION_SYNCED,
    STOCK_CARDS_LAST_YEAR_SYNCED,
    RAPID_TESTS_SYNCED,
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
