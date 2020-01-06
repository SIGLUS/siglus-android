package org.openlmis.core.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.LMISRestManagerMock;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
@Ignore
public class SyncDownManagerIT {
    private SyncDownManager syncDownManager;
    private ProductRepository productRepository;
    private ProductProgramRepository productProgramRepository;
    private UserRepository userRepository;
    private StockRepository stockRepository;
    private LotRepository lotRepository;
    private User defaultUser;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private ProgramDataFormRepository programDataFormRepository;
    private StockMovementRepository stockMovementRepository;
    private static final int DAYS_OF_MONTH = 30;

    @Before
    public void setup() {
        userRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(UserRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductProgramRepository.class);
        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        lotRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(LotRepository.class);
        programDataFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramDataFormRepository.class);
        stockMovementRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockMovementRepository.class);
        syncDownManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncDownManager.class);
        sharedPreferenceMgr = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SharedPreferenceMgr.class);

        defaultUser = new User();
        defaultUser.setUsername("cs_gelo");
        defaultUser.setPassword("password");
        defaultUser.setFacilityId("808");
        defaultUser.setFacilityName("CS Gelo");
        defaultUser.setFacilityCode("HF615");
        userRepository.createOrUpdate(defaultUser);
        UserInfoMgr.getInstance().setUser(defaultUser);
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


    @Test
    public void shouldSyncDownLatestProductWithArchivedStatus() throws Exception {
        //given
        String json = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
        LMISRestManagerMock lmisRestManager = LMISRestManagerMock.getRestManagerWithMockClient("/rest-api/latest-products", 200, "OK", json, RuntimeEnvironment.application);

        Date now = new Date();
        Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
        String startDateStr = DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);

        Date endDate = DateUtil.addDayOfMonth(now, 1);
        String endDateStr = DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);

        String fetchProgramsJson = JsonFileReader.readJson(getClass(), "fetchProgramsDown.json");
        String fetchPTVServiceJson = JsonFileReader.readJson(getClass(), "fetchfetchPTVService.json");
        String fetchReportTypesMapping = JsonFileReader.readJson(getClass(), "fetchReportTypesMapping.json");
        String fetchMovementDate = JsonFileReader.readJson(getClass(), "fetchStockMovementDate.json");
        String fetchRequisitionsData = JsonFileReader.readJson(getClass(), "fetchRequisitionsData.json");
        String fetchProgramDataFacilities = JsonFileReader.readJson(getClass(), "fetchProgramDataFacilities.json");
        lmisRestManager.addNewMockedResponse("/rest-api/programs/" + defaultUser.getFacilityId(), 200, "OK", fetchProgramsJson);
        lmisRestManager.addNewMockedResponse("/rest-api/services?" + "programCode=PTV", 200, "OK", fetchPTVServiceJson);
        lmisRestManager.addNewMockedResponse("/rest-api/report-types/mapping/" + defaultUser.getFacilityId(), 200, "OK", fetchReportTypesMapping);
        lmisRestManager.addNewMockedResponse("/rest-api/requisitions?" + "facilityCode=" + defaultUser.getFacilityCode(), 200, "OK", fetchRequisitionsData);
        lmisRestManager.addNewMockedResponse("/rest-api/programData/facilities/" + defaultUser.getFacilityId(), 200, "OK", fetchProgramDataFacilities);
        lmisRestManager.addNewMockedResponse("/rest-api/facilities/" + defaultUser.getFacilityId() + "/stockCards?" + "startTime=" + startDateStr + "&endTime=" + endDateStr, 200, "OK", fetchMovementDate);
        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

        //When
        SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
        syncDownManager.syncDownServerData(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        //Then
        checkShouldSyncDownLatestProductWithArchivedStatus();
    }

    private void checkShouldSyncDownLatestProductWithArchivedStatus() throws LMISException {
        Product product = productRepository.getByCode("08A12");
        assertFalse(product.isArchived());
        assertEquals("Amoxicilina+Acido clavulânico250mg + 62,5mgSuspensão", product.getPrimaryName());
        assertEquals("Suspensão", product.getType());
        assertEquals("250mg + 62,5mg", product.getStrength());

        ProductProgram productProgram = productProgramRepository.queryByCode("08A12", "ESS_MEDS");
        assertTrue(productProgram.isActive());
    }


    @Test
    public void shouldSyncDownStockCardsWithMovements() throws Exception {
        //set shared preferences to have synced all historical data already
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.YEAR, -1);
        sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, cal.getTimeInMillis()).apply();
        sharedPreferenceMgr.getPreference().edit().putBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_DOWN_RAPID_TESTS, true).apply();

        //given
        String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
        LMISRestManagerMock lmisRestManager = LMISRestManagerMock.getRestManagerWithMockClient("/rest-api/latest-products", 200, "OK", productJson, RuntimeEnvironment.application);

        Date now = new Date();
        Date startDate = DateUtil.minusDayOfMonth(now, 30);
        String startDateStr = DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);

        Date endDate = DateUtil.addDayOfMonth(now, 1);
        String endDateStr = DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);

        String stockMovementJson = JsonFileReader.readJson(getClass(), "SyncDownStockMovementsResponse.json");
        lmisRestManager.addNewMockedResponse("/rest-api/facilities/" + defaultUser.getFacilityId()
                + "/stockCards?startTime=" + startDateStr + "&endTime=" + endDateStr, 200, "OK", stockMovementJson);

        String emptyRequisitions = "{\"requisitions\": []}";
        lmisRestManager.addNewMockedResponse("/rest-api/requisitions?facilityCode=" + defaultUser.getFacilityCode(), 200, "OK", emptyRequisitions);
        String fetchProgramsJson = JsonFileReader.readJson(getClass(), "fetchProgramsDown.json");
        String fetchPTVServiceJson = JsonFileReader.readJson(getClass(), "fetchfetchPTVService.json");
        String fetchReportTypesMapping = JsonFileReader.readJson(getClass(), "fetchReportTypesMapping.json");
        String fetchRequisitionsData = JsonFileReader.readJson(getClass(), "fetchRequisitionsData.json");
        String fetchProgramDataFacilities = JsonFileReader.readJson(getClass(), "fetchProgramDataFacilities.json");
        lmisRestManager.addNewMockedResponse("/rest-api/programs/" + defaultUser.getFacilityId(), 200, "OK", fetchProgramsJson);
        lmisRestManager.addNewMockedResponse("/rest-api/services?" + "programCode=PTV", 200, "OK", fetchPTVServiceJson);
        lmisRestManager.addNewMockedResponse("/rest-api/report-types/mapping/" + defaultUser.getFacilityId(), 200, "OK", fetchReportTypesMapping);
        lmisRestManager.addNewMockedResponse("/rest-api/requisitions?" + "facilityCode=" + defaultUser.getFacilityCode(), 200, "OK", fetchRequisitionsData);
        lmisRestManager.addNewMockedResponse("/rest-api/programData/facilities/" + defaultUser.getFacilityId(), 200, "OK", fetchProgramDataFacilities);


        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

        //when
        TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
        syncDownManager.syncDownServerData(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<StockCard> stockCards = stockRepository.list();
        assertEquals(118, stockCards.size());
        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockMovementHistory(stockCards.get(0).getId(), 0L, 1000L);
        assertEquals(0, stockMovementItems.size());

        Product product = productRepository.getByCode("08N04Z");
        Lot lot = lotRepository.getLotByLotNumberAndProductId("6MK07", product.getId());
        assertEquals("2019-10-30", DateUtil.formatDate(lot.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
        LotOnHand lotOnHand = lotRepository.getLotOnHandByLot(lot);
        assertEquals(5, lotOnHand.getQuantityOnHand(), 0L);
    }

    @Test
    public void shouldSyncDownRapidTests() throws Exception {
        //set shared preferences to have synced all historical data already
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//        cal.add(Calendar.YEAR, -1);

        Date now = new Date();
        Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_MONTH);
        String startDateStr = DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);

        Date endDate = DateUtil.addDayOfMonth(now, 1);
        String endDateStr = DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);

//        sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, cal.getTimeInMillis()).apply();

        //given
        String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
        LMISRestManagerMock lmisRestManager = LMISRestManagerMock.getRestManagerWithMockClient("/rest-api/latest-products", 200, "OK", productJson, RuntimeEnvironment.application);

        sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
        sharedPreferenceMgr.setRequisitionDataSynced(true);
        String fetchProgramsJson = JsonFileReader.readJson(getClass(), "fetchProgramsDown.json");
        String fetchPTVServiceJson = JsonFileReader.readJson(getClass(), "fetchfetchPTVService.json");
        String fetchReportTypesMapping = JsonFileReader.readJson(getClass(), "fetchReportTypesMapping.json");
        String fetchMovementDate = JsonFileReader.readJson(getClass(), "fetchStockMovementDate.json");
        String fetchRequisitionsData = JsonFileReader.readJson(getClass(), "fetchRequisitionsData.json");
        String fetchProgramDataFacilities = JsonFileReader.readJson(getClass(), "fetchProgramDataFacilities.json");
        lmisRestManager.addNewMockedResponse("/rest-api/programs/" + defaultUser.getFacilityId(), 200, "OK", fetchProgramsJson);
        lmisRestManager.addNewMockedResponse("/rest-api/services?" + "programCode=PTV", 200, "OK", fetchPTVServiceJson);
        lmisRestManager.addNewMockedResponse("/rest-api/report-types/mapping/" + defaultUser.getFacilityId(), 200, "OK", fetchReportTypesMapping);
        lmisRestManager.addNewMockedResponse("/rest-api/requisitions?" + "facilityCode=" + defaultUser.getFacilityCode(), 200, "OK", fetchRequisitionsData);
        lmisRestManager.addNewMockedResponse("/rest-api/programData/facilities/" + defaultUser.getFacilityId(), 200, "OK", fetchProgramDataFacilities);
        lmisRestManager.addNewMockedResponse("/rest-api/facilities/" + defaultUser.getFacilityId() + "/stockCards?" + "startTime=" + startDateStr + "&endTime=" + endDateStr, 200, "OK", fetchMovementDate);
        String rapidTestsResponseJson = JsonFileReader.readJson(getClass(), "SyncDownRapidTestsResponse.json");
        lmisRestManager.addNewMockedResponse("/rest-api/programData/facilities/" + defaultUser.getFacilityId(), 200, "OK", rapidTestsResponseJson);

        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

        //when
        TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
        syncDownManager.syncDownServerData(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<ProgramDataForm> programDataForms = programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE);
        assertEquals(0, programDataForms.size());
    }
}
