package org.openlmis.core.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
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
import static junit.framework.Assert.assertTrue;

@Ignore
@RunWith(LMISTestRunner.class)
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
        defaultUser.setUsername("username");
        defaultUser.setPassword("password");
        defaultUser.setFacilityId("10");
        defaultUser.setFacilityName("facility");
        defaultUser.setFacilityCode("F1");
        userRepository.createOrUpdate(defaultUser);
        UserInfoMgr.getInstance().setUser(defaultUser);
    }

    @Test
    public void shouldSyncDownLatestProductWithArchivedStatus() throws Exception {
        //given
        String json = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
        LMISRestManagerMock lmisRestManager = LMISRestManagerMock.getRestManagerWithMockClient("/rest-api/latest-products", 200, "OK", json, RuntimeEnvironment.application);
        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

        //when
        syncDownManager.syncDownServerData();

        //then
        Product product = productRepository.getByCode("01A01");
        assertTrue(product.isArchived());
        assertEquals("Estavudina+Lamivudina+Nevirapi 6mg + 30mg +50mg, 60 Cps (BabyEmbalagem", product.getPrimaryName());
        assertEquals("Embalagem", product.getType());
        assertEquals("6mg + 30mg +50mg, 60 Cps (Baby", product.getStrength());

        ProductProgram productProgram = productProgramRepository.queryByCode("01A01", "ESS_MEDS");
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

        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

        //when
        TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
        syncDownManager.syncDownServerData(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<StockCard> stockCards = stockRepository.list();
        assertEquals(1, stockCards.size());
        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockMovementHistory(stockCards.get(0).getId(), 0L, 1000L);
        assertEquals(1, stockMovementItems.size());

        Product product = productRepository.getByCode("01A01");
        Lot lot = lotRepository.getLotByLotNumberAndProductId("TEST5", product.getId());
        assertEquals("2016-10-30", DateUtil.formatDate(lot.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
        LotOnHand lotOnHand = lotRepository.getLotOnHandByLot(lot);
        assertEquals(5, lotOnHand.getQuantityOnHand(), 0L);
    }

    @Test
    public void shouldSyncDownRapidTests() throws Exception {
        //set shared preferences to have synced all historical data already
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.YEAR, -1);

        sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, cal.getTimeInMillis()).apply();

        //given
        String productJson = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
        LMISRestManagerMock lmisRestManager = LMISRestManagerMock.getRestManagerWithMockClient("/rest-api/latest-products", 200, "OK", productJson, RuntimeEnvironment.application);

        sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
        sharedPreferenceMgr.setRequisitionDataSynced(true);

        String rapidTestsResponseJson = JsonFileReader.readJson(getClass(), "SyncDownRapidTestsResponse.json");
        lmisRestManager.addNewMockedResponse("/rest-api/programData/facilities/" + defaultUser.getFacilityId(), 200, "OK", rapidTestsResponseJson);

        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();

        //when
        TestSubscriber<SyncDownManager.SyncProgress> subscriber = new TestSubscriber<>();
        syncDownManager.syncDownServerData(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        List<ProgramDataForm> programDataForms = programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE);
        assertEquals(1, programDataForms.size());
        assertEquals("2016-02-21", DateUtil.formatDate(programDataForms.get(0).getPeriodBegin(), DateUtil.DB_DATE_FORMAT));
        assertEquals("2016-03-20", DateUtil.formatDate(programDataForms.get(0).getPeriodEnd(), DateUtil.DB_DATE_FORMAT));
        assertEquals(8, programDataForms.get(0).getProgramDataFormItemListWrapper().size());
    }
}
