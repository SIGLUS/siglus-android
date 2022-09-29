package org.openlmis.core.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManagerMock;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class SyncDownManagerIT {

  private static final int DAYS_OF_MONTH = 30;
  private SyncDownManager syncDownManager;
  private ProgramRepository programRepository;
  private ReportTypeFormRepository reportTypeFormRepository;
  private ProductRepository productRepository;
  private ProductProgramRepository productProgramRepository;
  private UserRepository userRepository;
  private StockRepository stockRepository;
  private LotRepository lotRepository;
  private RegimenRepository regimenRepository;
  private PodRepository podRepository;
  private User defaultUser1;
  private SharedPreferenceMgr sharedPreferenceMgr;
  private StockMovementRepository stockMovementRepository;
  private RnrFormRepository rnrFormRepository;
  private LMISTestApp appInject;
  private LMISRestApi mockedApi;


  @Before
  public void setup() {
    userRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(UserRepository.class);
    programRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramRepository.class);
    reportTypeFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ReportTypeFormRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductProgramRepository.class);
    stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockRepository.class);
    lotRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(LotRepository.class);
    regimenRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RegimenRepository.class);
    podRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(PodRepository.class);
    stockMovementRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockMovementRepository.class);
    rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RnrFormRepository.class);
    syncDownManager = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(SyncDownManager.class);
    sharedPreferenceMgr = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(SharedPreferenceMgr.class);
    appInject = (LMISTestApp) RuntimeEnvironment.application;
    mockedApi = mock(LMISRestApi.class);
    appInject.setRestApi(mockedApi);

    defaultUser1 = new User();
    defaultUser1.setUsername("cs_gelo");
    defaultUser1.setPassword("password");
    defaultUser1.setFacilityId("808");
    defaultUser1.setFacilityName("CS Gelo");
    defaultUser1.setFacilityCode("HF615");
    defaultUser1.setIsTokenExpired(true);
    userRepository.createOrUpdate(defaultUser1);
    UserInfoMgr.getInstance().setUser(defaultUser1);
  }

  private void testSyncProgress(SyncDownManager.SyncProgress progress) {
    System.out.println(progress);
  }

  private void mockResponse(LMISRestManagerMock lmisRestManager) {
    Date now = new Date();
    Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
    String startDateStr = DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);

    Date endDate = DateUtil.addDayOfMonth(now, 1);
    String endDateStr = DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);

    String fetchProgramsJson = JsonFileReader.readJson(getClass(), "fetchProgramsDown.json");
    String fetchReportTypesMapping = JsonFileReader
        .readJson(getClass(), "fetchReportTypesMapping.json");
    String fetchMovementDate = JsonFileReader.readJson(getClass(), "fetchStockMovementDate.json");
    String fetchRequisitionsData = JsonFileReader
        .readJson(getClass(), "fetchRequisitionsData.json");
    String fetchProgramDataFacilities = JsonFileReader
        .readJson(getClass(), "fetchProgramDataFacilities.json");
    String rapidTestsResponseJson = JsonFileReader
        .readJson(getClass(), "SyncDownRapidTestsResponse.json");
    String syncDownKitChagneResponseJson = JsonFileReader
        .readJson(getClass(), "fetchKitChangeReponse.json");
    String json = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    String authSuccessResponse = JsonFileReader.readJson(getClass(), "AuthSuccessResponse.json");
    String V3ProductsResponseAdapterResponse = JsonFileReader
        .readJson(getClass(), "SyncDownLatestProductResponse.json");
    String regimenJson = JsonFileReader.readJson(getClass(), "fetchRegimenResponse.json");
    String podJson = JsonFileReader.readJson(getClass(), "fetchPodsData.json");

    lmisRestManager.addNewMockedResponse(
        "/api/oauth/token?grant_type=password&username=cs_gelo&password=password", 200, "OK", authSuccessResponse);
    lmisRestManager.addNewMockedResponse(
        "/rest-api/programData/facilities/" + getDefaultUser().getFacilityId(), 200, "OK",
        rapidTestsResponseJson);
    lmisRestManager
        .addNewMockedResponse("/rest-api/programs/" + getDefaultUser().getFacilityId(), 200, "OK",
            fetchProgramsJson);
    lmisRestManager
        .addNewMockedResponse("/rest-api/report-types/mapping/" + getDefaultUser().getFacilityId(),
            200, "OK", fetchReportTypesMapping);
    lmisRestManager.addNewMockedResponse(
        "/api/siglusapi/android/me/facility/requisitions?" + "startDate=" + getStartDateWithDB_DATE_FORMAT(), 200,
        "OK", fetchRequisitionsData);
    lmisRestManager.addNewMockedResponse(
        "/rest-api/programData/facilities/" + getDefaultUser().getFacilityId(), 200, "OK",
        fetchProgramDataFacilities);
    lmisRestManager.addNewMockedResponse(
        "/api/siglusapi/android/me/facility/stockCards?" + "startTime="
            + startDateStr + "&endTime=" + endDateStr, 200, "OK", fetchMovementDate);
    lmisRestManager.addNewMockedResponse(
        "/rest-api/temp86-notice-kit-change?afterUpdatedTime=" + sharedPreferenceMgr
            .getLastSyncProductTime(), 200, "OK", syncDownKitChagneResponseJson);
    lmisRestManager.addNewMockedResponse("/rest-api/latest-products", 200, "OK", json);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/me/facility/products", 200, "OK",
        V3ProductsResponseAdapterResponse);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/regimens", 200, "OK", regimenJson);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/me/facility/pods?shippedOnly=false", 200, "OK",
        podJson);
  }

  @Test
  public void shouldSyncDownFacilityInfo() throws LMISException {
    // given
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/api/siglusapi/android/me/facility", 200, "OK",
            facilityInfoJson, RuntimeEnvironment.application);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    List<Program> programs = programRepository.list();
    List<ReportTypeForm> reportTypeForms = reportTypeFormRepository.listAll();

    // then
    assertEquals(4, programs.size());
    assertEquals(4, reportTypeForms.size());
  }

  @Test
  public void shouldSyncDownRegimens() throws Exception {
    // given
    String regimenJson = JsonFileReader.readJson(getClass(), "fetchRegimenResponse.json");
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/api/siglusapi/android/regimens", 200, "OK", regimenJson,
            RuntimeEnvironment.application);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/me/facility", 200, "OK",
        facilityInfoJson);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    List<Regimen> regimenList = regimenRepository.listDefaultRegime();

    // then
    assertEquals(93, regimenList.size());
  }

  @Test
  public void shouldSyncDownPods() throws LMISException {
    // given
    String podJson = JsonFileReader.readJson(getClass(), "fetchPodsData.json");
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/api/siglusapi/android/me/facility/pods?shippedOnly=false", 200, "OK", podJson,
            RuntimeEnvironment.application);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/me/facility", 200, "OK",
        facilityInfoJson);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    List<Pod> pods = podRepository.list();

    // then
    assertEquals("RNR-EM010809070000006", pods.get(0).getRequisitionNumber());
  }

  @Test
  public void shouldSyncDownRequisitions() throws LMISException {
    // given
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");
    String requisitionsJson = JsonFileReader.readJson(getClass(), "fetchRequisitionsData.json");
    LMISRestManagerMock lmisRestManagerMock = LMISRestManagerMock.getRestManagerWithMockClient(
        "/api/siglusapi/android/me/facility/requisitions?" + "startDate=" + getStartDateWithDB_DATE_FORMAT(), 200,
        "OK", requisitionsJson, RuntimeEnvironment.application);
    lmisRestManagerMock.addNewMockedResponse("/api/siglusapi/android/me/facility", 200, "OK",
        facilityInfoJson);
    mockResponse(lmisRestManagerMock);
    syncDownManager.lmisRestApi = lmisRestManagerMock.getLmisRestApi();
    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    List<RnRForm> rnRForms = rnrFormRepository.list();
    // then

    assertEquals(3, rnRForms.size());


  }

  @Test
  public void shouldLoginFailedWhenUserIsAndroidFalse() {
    // given
    String facilityInfoJsonWithIsAndroidFalse = JsonFileReader
        .readJson(getClass(), "facilityInfoResponseWithIsAndroidFalse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/api/siglusapi/android/me/facility", 403, "Forbidden",
            facilityInfoJsonWithIsAndroidFalse, RuntimeEnvironment.application);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();
    LMISException exception = new LMISException(errorMessage(R.string.msg_isAndroid_False));

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();

    // then
    subscriber.assertError(exception);

  }

  @Test
  public void shouldSyncDownLatestProductWithArchivedStatus() throws Exception {
    // given
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");
    String json = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", json, RuntimeEnvironment.application);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/me/facility", 200, "OK",
        facilityInfoJson);
    mockResponse(lmisRestManager);

    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    // then
    checkShouldSyncDownLatestProductWithArchivedStatus();
  }

  private Date getStartDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return DateUtil.dateMinusMonth(calendar.getTime(),
        sharedPreferenceMgr.getMonthOffsetThatDefinedOldData());
  }

  private String getStartDateWithDB_DATE_FORMAT() {
    return DateUtil.formatDate(getStartDate(), DateUtil.DB_DATE_FORMAT);
  }

  private void checkShouldSyncDownLatestProductWithArchivedStatus() throws LMISException {
    Product product = productRepository.getByCode("08O05");
    assertFalse(product.isArchived());
    assertEquals("Artemeter+Lumefantrina; 120mg+20mg 1x6; Comp testyby", product.getPrimaryName());

    ProductProgram productProgram = productProgramRepository.queryByCode("08O05", "VC");
    assertTrue(productProgram.isActive());
  }

  @Test
  public void shouldSyncDownStockCardsWithMovements() throws Exception {
    // set shared preferences to have synced all historical data already
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.YEAR, -1);
    sharedPreferenceMgr.getPreference().edit()
        .putLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, cal.getTimeInMillis()).apply();
    sharedPreferenceMgr.setRapidTestsDataSynced(true);

    // given
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");
    String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", productJson, RuntimeEnvironment.application);
    lmisRestManager.addNewMockedResponse("/api/siglusapi/android/me/facility", 200, "OK",
        facilityInfoJson);
    mockResponse(lmisRestManager);

    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
    syncDownManager.syncDownServerData(subscriber);

    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    List<StockCard> stockCards = stockRepository.list();
    assertEquals(1, stockCards.size());
    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .queryStockMovementHistory(stockCards.get(0).getId(), 0L, 1000L);
    assertEquals(1, stockMovementItems.size());

    Product product = productRepository.getByCode("07A06");
    Lot lot = lotRepository.getLotByLotNumberAndProductId("SEM-LOTE-07A06-052022-0", product.getId());
    assertEquals("2022-05-11",
        DateUtil.formatDate(lot.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
    LotOnHand lotOnHand = lotRepository.getLotOnHandByLot(lot);
    assertEquals(100, lotOnHand.getQuantityOnHand(), 0L);
  }

  private User getDefaultUser() {
    return UserInfoMgr.getInstance().getUser();
  }

  private String errorMessage(int code) {
    return LMISApp.getContext().getResources().getString(code);
  }

  private class SyncServerDataSubscriber extends TestSubscriber<SyncDownManager.SyncProgress> {

    @Override
    public void onNext(SyncDownManager.SyncProgress syncProgress) {
      super.onNext(syncProgress);
      testSyncProgress(syncProgress);
    }
  }
}
