package org.openlmis.core.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManagerMock;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class SyncDownManagerIT {

  private SyncDownManager syncDownManager;
  private ProgramRepository programRepository;
  private ReportTypeFormRepository reportTypeFormRepository;
  private ProductRepository productRepository;
  private ProductProgramRepository productProgramRepository;
  private UserRepository userRepository;
  private StockRepository stockRepository;
  private LotRepository lotRepository;
  private RegimenRepository regimenRepository;
  private User defaultUser1;
  private SharedPreferenceMgr sharedPreferenceMgr;
  private ProgramDataFormRepository programDataFormRepository;
  private StockMovementRepository stockMovementRepository;
  private static final int DAYS_OF_MONTH = 30;
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
    programDataFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramDataFormRepository.class);
    stockMovementRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockMovementRepository.class);
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

  private class SyncServerDataSubscriber extends TestSubscriber<SyncDownManager.SyncProgress> {

    @Override
    public void onNext(SyncDownManager.SyncProgress syncProgress) {
      super.onNext(syncProgress);
      testSyncProgress(syncProgress);
    }
  }

  private void mockResponse(LMISRestManagerMock lmisRestManager) {
    Date now = new Date();
    Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
    String startDateStr = DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);

    Date endDate = DateUtil.addDayOfMonth(now, 1);
    String endDateStr = DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);

    String fetchProgramsJson = JsonFileReader.readJson(getClass(), "fetchProgramsDown.json");
    String fetchPTVServiceJson = JsonFileReader.readJson(getClass(), "fetchfetchPTVService.json");
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
    String facilityInfoJson = JsonFileReader.readJson(getClass(), "fetchFacilityInfoResponse.json");

    lmisRestManager.addNewMockedResponse(
        "/api/oauth/token?grant_type=password&username=cs_gelo&password=password", 200, "OK", authSuccessResponse);
    lmisRestManager.addNewMockedResponse(
        "/rest-api/programData/facilities/" + getDefaultUser().getFacilityId(), 200, "OK",
        rapidTestsResponseJson);
    lmisRestManager
        .addNewMockedResponse("/rest-api/programs/" + getDefaultUser().getFacilityId(), 200, "OK",
            fetchProgramsJson);
    lmisRestManager.addNewMockedResponse("/rest-api/services?" + "programCode=PTV", 200, "OK",
        fetchPTVServiceJson);
    lmisRestManager
        .addNewMockedResponse("/rest-api/report-types/mapping/" + getDefaultUser().getFacilityId(),
            200, "OK", fetchReportTypesMapping);
    lmisRestManager.addNewMockedResponse(
        "/rest-api/requisitions?" + "facilityCode=" + getDefaultUser().getFacilityCode()
            + "&startDate=" + getStartDateWithDB_DATE_FORMAT(), 200, "OK", fetchRequisitionsData);
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
    lmisRestManager.addNewMockedResponse(
        "/api/siglusapi/android/me/facility", 200, "OK", facilityInfoJson);
  }

  // TODO: fix the test later as a teach card
  @Ignore
  @Test
  public void shouldRefreshTokenWhenTokenExpired() {
    // given
    String tokenExpiredResponse = JsonFileReader.readJson(getClass(), "TokenExpiredResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/api/siglusapi/android/me/facility", 401, "Unauthorized",
            tokenExpiredResponse, RuntimeEnvironment.application);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();
    UserInfoMgr.getInstance().getUser().setIsTokenExpired(false);

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    // then
    verify(lmisRestManager.getLmisRestApi(), times(1)).login(any(), any(), any(), any());

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
    subscriber.assertNoErrors();
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
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/api/siglusapi/android/regimens", 200, "OK", regimenJson,
            RuntimeEnvironment.application);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();
    List<Regimen> regimenList = regimenRepository.listDefaultRegime();

    // then
    assertEquals(93, regimenList.size());
  }


  @Ignore
  @Test
  public void shouldSyncDownLatestProductWithArchivedStatus() throws Exception {
    // given
    String json = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", json, RuntimeEnvironment.application);
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
    Product product = productRepository.getByCode("08A12");
    assertFalse(product.isArchived());
//        assertEquals("Amoxicilina+Acido clavulânico250mg + 62,5mgSuspensão", product.getPrimaryName());
    assertEquals("Suspensão", product.getType());
    assertEquals("250mg + 62,5mg", product.getStrength());

    ProductProgram productProgram = productProgramRepository.queryByCode("08A12", "ESS_MEDS");
    assertTrue(productProgram.isActive());
  }


  @Ignore
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
    String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", productJson, RuntimeEnvironment.application);
    mockResponse(lmisRestManager);

    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
    syncDownManager.syncDownServerData(subscriber);

    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    List<StockCard> stockCards = stockRepository.list();
    assertEquals(118, stockCards.size());
    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .queryStockMovementHistory(stockCards.get(0).getId(), 0L, 1000L);
    assertEquals(0, stockMovementItems.size());

    Product product = productRepository.getByCode("08N04Z");
    Lot lot = lotRepository.getLotByLotNumberAndProductId("6MK07", product.getId());
    assertEquals("2019-10-30",
        DateUtil.formatDate(lot.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
    LotOnHand lotOnHand = lotRepository.getLotOnHandByLot(lot);
    assertEquals(0, lotOnHand.getQuantityOnHand(), 0L);
  }

  @Ignore
  @Test
  public void shouldSyncDownRapidTests() throws Exception {
    // given
    String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", productJson, RuntimeEnvironment.application);

    sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
    sharedPreferenceMgr.setRequisitionDataSynced(true);
    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
    syncDownManager.syncDownServerData(subscriber);

    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();
    Product product26A02 = productRepository.getByCode("26A02");

    assertFalse(product26A02.isKit());
  }

  @Ignore
  @Test
  public void shouldSyncDownLastYearSilently() throws LMISException {
    // given
    String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", productJson, RuntimeEnvironment.application);

    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();
    sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
    sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
    syncDownManager.syncDownServerData();

//        /rest-api/latest-products
    String syncDownKitChagneResponseJson = JsonFileReader
        .readJson(getClass(), "fetchKitChangeReponse.json");
    lmisRestManager
        .addNewMockedResponse("/rest-api/temp86-notice-kit-change?afterUpdatedTime=1578289583857",
            200, "OK", syncDownKitChagneResponseJson);

    List<ProgramDataForm> programDataForms = programDataFormRepository
        .listByProgramCode(Constants.RAPID_TEST_CODE);
    assertEquals(0, programDataForms.size());

  }

  @Test
  @Ignore
  public void shouldSyncDownKitChange() throws LMISException {
    // given
    String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
    LMISRestManagerMock lmisRestManager = LMISRestManagerMock
        .getRestManagerWithMockClient("/rest-api/latest-products?afterUpdatedTime=1578289583857",
            200, "OK", productJson, RuntimeEnvironment.application);

    mockResponse(lmisRestManager);
    syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

    // when
    TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    Product product26A02 = productRepository.getByCode("26A02");
    assertFalse(product26A02.isKit());

    sharedPreferenceMgr.setShouldSyncLastYearStockCardData(false);
    sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
    syncDownManager.syncDownServerData();

//        /rest-api/latest-products
    String syncDownKitChangeResponseJson = JsonFileReader
        .readJson(getClass(), "fetchKitChangeReponse.json");
    lmisRestManager
        .addNewMockedResponse("/rest-api/temp86-notice-kit-change?afterUpdatedTime=1578289583857",
            200, "OK", syncDownKitChangeResponseJson);

    List<ProgramDataForm> programDataForms = programDataFormRepository
        .listByProgramCode(Constants.RAPID_TEST_CODE);
    assertEquals(16, programDataForms.size());

  }

  private User getDefaultUser() {
    return UserInfoMgr.getInstance().getUser();
  }

  private String errorMessage(int code) {
    return LMISApp.getContext().getResources().getString(code);
  }
}
