package org.openlmis.core.service;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.FACILITY_INFO_SYNCED;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.REGIMENS_SYNCED;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.REQUISITION_SYNCED;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.STOCK_CARDS_LAST_MONTH_SYNCED;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.STOCK_CARDS_LAST_YEAR_SYNCED;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SYNCING_FACILITY_INFO;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SYNCING_REGIMENS;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.inject.AbstractModule;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.FacilityInfoResponse;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SupportedProgram;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownProgramDataResponse;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
import org.openlmis.core.network.model.SyncDownReportTypeResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownServiceResponse;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.SyncUpProgramResponse;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class SyncDownManagerTest {

  private SyncDownManager syncDownManager;

  private LMISRestApi lmisRestApi;
  private SharedPreferenceMgr sharedPreferenceMgr;
  private StockMovementItem stockMovementItem;

  private ProgramRepository programRepository;
  private RnrFormRepository rnrFormRepository;
  private StockRepository stockRepository;
  private SharedPreferences createdPreferences;

  private ProductRepository productRepository;
  private Product productWithKits;
  private StockService stockService;

  private StockMovementRepository stockMovementRepository;

  @Before
  public void setUp() throws Exception {
    sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    lmisRestApi = mock(LMISRestApi.class);
    rnrFormRepository = mock(RnrFormRepository.class);
    programRepository = mock(ProgramRepository.class);
    productRepository = mock(ProductRepository.class);
    stockRepository = mock(StockRepository.class);
    stockService = mock(StockService.class);
    stockMovementRepository = mock(StockMovementRepository.class);

    reset(rnrFormRepository);
    reset(lmisRestApi);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    syncDownManager = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(SyncDownManager.class);

    syncDownManager.lmisRestApi = lmisRestApi;
    User user = new User();
    user.setFacilityCode("HF XXX");
    user.setFacilityId("123");
    UserInfoMgr.getInstance().setUser(user);
    RxAndroidPlugins.getInstance().reset();
    RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
      @Override
      public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
      }
    });

    syncDownManager.stockService = stockService;
  }

  @Test
  public void shouldSyncDownServerData() throws Exception {
    // given
    mockFacilityInfoResponse();
    mockRegimenResponse();
    mockSyncDownLatestProductResponse();
    mockReportResponse();
    mockRequisitionResponse();
    mockStockCardsResponse();
    mockRapidTestsResponse();
    mockFetchProgramsResponse();
    mockFetchPTVServiceResponse();

    // when
    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    // then
    assertThat(subscriber.syncProgresses.get(0), is(SYNCING_FACILITY_INFO));
    assertThat(subscriber.syncProgresses.get(1), is(FACILITY_INFO_SYNCED));
    assertThat(subscriber.syncProgresses.get(2), is(SYNCING_REGIMENS));
    assertThat(subscriber.syncProgresses.get(3), is(REGIMENS_SYNCED));
    // To Do when the following interface was developed
    //        assertThat(subscriber.syncProgresses.get(2), is(SyncingProduct));
    //        assertThat(subscriber.syncProgresses.get(3), is(SyncingStockCardsLastMonth));
    //        assertThat(subscriber.syncProgresses.get(6), is(SyncingProduct));
    //        assertThat(subscriber.syncProgresses.get(7), is(ProductSynced));
    //        assertThat(subscriber.syncProgresses.get(8), is(SyncingStockCardsLastMonth));
    //        assertThat(subscriber.syncProgresses.get(9), is(StockCardsLastMonthSynced));
    //        assertThat(subscriber.syncProgresses.get(10), is(SyncingRequisition));
    //        assertThat(subscriber.syncProgresses.get(11), is(RequisitionSynced));
    //        assertThat(subscriber.syncProgresses.get(12), is(SyncingRapidTests));
    //        assertThat(subscriber.syncProgresses.get(13), is(RapidTestsSynced));
  }

  @Test
  public void shouldOnlySyncOnceWhenInvokedTwice() throws Exception {
    // given
    mockFacilityInfoResponse();
    mockRegimenResponse();
    mockReportResponse();
    mockSyncDownLatestProductResponse();
    mockRequisitionResponse();
    mockStockCardsResponse();
    mockRapidTestsResponse();
    mockFetchProgramsResponse();
    mockFetchPTVServiceResponse();

    // when
    CountOnNextSubscriber firstEnterSubscriber = new CountOnNextSubscriber();
    CountOnNextSubscriber laterEnterSubscriber = new CountOnNextSubscriber();
    syncDownManager.syncDownServerData(firstEnterSubscriber);
    syncDownManager.syncDownServerData(laterEnterSubscriber);

    firstEnterSubscriber.awaitTerminalEvent();
    firstEnterSubscriber.assertNoErrors();
    laterEnterSubscriber.assertNoTerminalEvent();

    // then
    assertThat(firstEnterSubscriber.syncProgresses.size(), is(8));
    assertThat(laterEnterSubscriber.syncProgresses.size(), is(0));
  }

  @Ignore
  @Test
  public void shouldSyncDownNewLatestProductList() throws Exception {
    mockFacilityInfoResponse();
    mockReportResponse();
    mockSyncDownLatestProductResponse();
    mockRequisitionResponse();
    mockStockCardsResponse();
    mockRapidTestsResponse();
    mockFetchProgramsResponse();
    mockFetchPTVServiceResponse();
    when(productRepository.getByCode(anyString())).thenReturn(new Product());

    Program program = new Program();
    when(programRepository.queryByCode("PR")).thenReturn(program);

    SyncServerDataSubscriber subscriber = new SyncServerDataSubscriber();
    syncDownManager.syncDownServerData(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    verify(lmisRestApi).fetchLatestProducts(anyString());
    verify(productRepository).batchCreateOrUpdateProducts(anyList());
    verify(sharedPreferenceMgr).setLastSyncProductTime("today");
  }

  @Test
  public void shouldUpdateNotifyBannerListWhenSOHIsZeroAndProductIsDeActive() throws Exception {
    // given
    Product product = new Product();
    product.setPrimaryName("name");
    product.setActive(false);
    product.setCode("code");
    product.setArchived(false);

    Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(true)
        .setIsArchived(true).build();
    when(productRepository.getByCode(product.getCode())).thenReturn(existingProduct);

    StockCard stockCard = new StockCard();
    stockCard.setProduct(product);
    stockCard.setStockOnHand(0);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

    // when
    syncDownManager.updateDeactivateProductNotifyList(product);

    // then
    verify(sharedPreferenceMgr).setIsNeedShowProductsUpdateBanner(true, "name");
  }

  @Test
  public void shouldNotUpdateNotifyBannerListWhenProductIsArchived() throws Exception {
    // given
    Product product = new Product();
    product.setPrimaryName("name");
    product.setActive(false);
    product.setCode("code");
    product.setArchived(true);

    Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(true)
        .setIsArchived(true).build();
    when(productRepository.getByCode(product.getCode())).thenReturn(existingProduct);

    StockCard stockCard = new StockCard();
    stockCard.setProduct(product);
    stockCard.setStockOnHand(0);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

    // when
    syncDownManager.updateDeactivateProductNotifyList(product);

    // then
    verify(sharedPreferenceMgr, never()).setIsNeedShowProductsUpdateBanner(true, "name");
  }

  @Test
  public void shouldRemoveNotifyBannerListWhenReactiveProduct() throws Exception {
    // given
    Product product = new Product();
    product.setPrimaryName("new name");
    product.setActive(true);
    product.setCode("code");

    Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(false)
        .setPrimaryName("name").build();
    when(productRepository.getByCode(product.getCode())).thenReturn(existingProduct);

    StockCard stockCard = new StockCard();
    stockCard.setStockOnHand(0);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

    // when
    syncDownManager.updateDeactivateProductNotifyList(product);

    // then
    verify(sharedPreferenceMgr).removeShowUpdateBannerTextWhenReactiveProduct("name");
  }

  @Test
  public void shouldNotUpdateBannerWhenExistingProductIsNull() throws Exception {
    Product product = new Product();
    when(productRepository.getByCode(anyString())).thenReturn(null);
    syncDownManager.updateDeactivateProductNotifyList(product);
    verify(stockRepository, times(0)).queryStockCardByProductId(anyLong());
  }

  @Test
  public void shouldEqualResponse() {
    List<ProductAndSupportedPrograms> productsAndSupportedPrograms = new ArrayList<>();
    ProductAndSupportedPrograms productAndSupportedPrograms = new ProductAndSupportedPrograms();
    productWithKits = new Product();
    productWithKits.setCode("ABC");
    productAndSupportedPrograms.setProduct(productWithKits);
    ProductProgram productProgram = new ProductProgramBuilder().setProductCode("ABC")
        .setProgramCode("PR").setActive(true).build();
    productAndSupportedPrograms.setProductPrograms(newArrayList(productProgram));

    ProductAndSupportedPrograms productAndSupportedPrograms1 = new ProductAndSupportedPrograms();
    productWithKits = new Product();
    productWithKits.setCode("ABC");
    productAndSupportedPrograms.setProduct(productWithKits);
    ProductProgram productProgram1 = new ProductProgramBuilder().setProductCode("ABC")
        .setProgramCode("PR").setActive(true).build();

    productAndSupportedPrograms1.setProductPrograms(newArrayList(productProgram1));
    productsAndSupportedPrograms.add(productAndSupportedPrograms);

    SyncDownLatestProductsResponse response = new SyncDownLatestProductsResponse();
    SyncDownLatestProductsResponse response1 = new SyncDownLatestProductsResponse();
    response.setLastSyncTime("today");
    response1.setLastSyncTime("today");
    response.setLatestProducts(productsAndSupportedPrograms);
    response1.setLatestProducts(productsAndSupportedPrograms);
    assertEquals(response1, response);
    //        assertEquals(productAndSupportedPrograms, productAndSupportedPrograms1);

  }

  private void testSyncProgress(SyncProgress progress) throws SQLException {
    try {
      if (progress == STOCK_CARDS_LAST_MONTH_SYNCED) {
        verifyLastMonthStockCardsSynced();
        verify(sharedPreferenceMgr).setLastMonthStockCardDataSynced(true);

      }
      if (progress == REQUISITION_SYNCED) {
        verify(rnrFormRepository, times(1)).createRnRsWithItems(any(ArrayList.class));
        verify(sharedPreferenceMgr).setRequisitionDataSynced(true);
      }
      if (progress == STOCK_CARDS_LAST_YEAR_SYNCED) {
        verify(lmisRestApi, times(13)).fetchStockMovementData(anyString(), anyString());
        verify(sharedPreferenceMgr).setShouldSyncLastYearStockCardData(false);
      }
    } catch (LMISException e) {
      e.printStackTrace();
    }
  }

  private void mockSyncDownLatestProductResponse() throws LMISException {
    List<ProductAndSupportedPrograms> productsAndSupportedPrograms = new ArrayList<>();
    ProductAndSupportedPrograms productAndSupportedPrograms = new ProductAndSupportedPrograms();
    productWithKits = new Product();
    productWithKits.setCode("ABC");
    productAndSupportedPrograms.setProduct(productWithKits);
    ProductProgram productProgram = new ProductProgramBuilder().setProductCode("ABC")
        .setProgramCode("PR").setActive(true).build();
    productAndSupportedPrograms.setProductPrograms(newArrayList(productProgram));
    productsAndSupportedPrograms.add(productAndSupportedPrograms);

    SyncDownLatestProductsResponse response = new SyncDownLatestProductsResponse();
    response.setLastSyncTime("today");
    response.setLatestProducts(productsAndSupportedPrograms);
    when(lmisRestApi.fetchLatestProducts(any(String.class))).thenReturn(response);
  }

  private void mockRequisitionResponse() throws LMISException {
    when(sharedPreferenceMgr.getPreference()).thenReturn(
        LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
    List<RnRForm> data = new ArrayList<>();
    data.add(new RnRForm());
    data.add(new RnRForm());

    SyncDownRequisitionsResponse syncDownRequisitionsResponse = new SyncDownRequisitionsResponse();
    syncDownRequisitionsResponse.setRequisitionResponseList(data);
    when(lmisRestApi.fetchRequisitions(anyString())).thenReturn(syncDownRequisitionsResponse);
  }

  private void mockStockCardsResponse() throws ParseException, LMISException {
    createdPreferences = LMISTestApp.getContext()
        .getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
    when(sharedPreferenceMgr.shouldSyncLastYearStockData()).thenReturn(true);
    when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);
    when(lmisRestApi.fetchStockMovementData(anyString(), anyString())).thenReturn(getStockCardResponse());
    when(stockRepository.list()).thenReturn(newArrayList(new StockCardBuilder().build()));
  }

  private void mockRapidTestsResponse() throws ParseException, LMISException {
    createdPreferences = LMISTestApp.getContext()
        .getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
    when(sharedPreferenceMgr.isRapidTestDataSynced()).thenReturn(false);
    when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);

    when(lmisRestApi.fetchProgramDataForms(anyLong())).thenReturn(getRapidTestsResponse());
  }

  private void mockReportResponse() throws ParseException, LMISException {
    createdPreferences = LMISTestApp.getContext()
        .getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
    when(sharedPreferenceMgr.getPreference()).thenReturn(createdPreferences);
    when(lmisRestApi.fetchReportTypeForms(anyLong())).thenReturn(getReportTypeResponse());
  }

  private void mockFacilityInfoResponse() throws LMISException {
    FacilityInfoResponse facilityInfoResponse = getFacilityInfoResponse();
    when(lmisRestApi.fetchFacilityInfo()).thenReturn(facilityInfoResponse);
  }

  private void mockRegimenResponse() throws LMISException {
    SyncDownRegimensResponse syncDownRegimensResponse = getRegimenResponse();
    when(lmisRestApi.fetchRegimens()).thenReturn(syncDownRegimensResponse);
  }

  private void mockFetchProgramsResponse() throws LMISException, ParseException {
    SyncUpProgramResponse response = getFetchProgramsResponse();
    when(lmisRestApi.fetchPrograms(anyLong())).thenReturn(response);
  }

  private void mockFetchPTVServiceResponse() throws LMISException {
    getSyncDownServiceResponse();
  }

  private SyncDownServiceResponse getSyncDownServiceResponse() throws LMISException {
    SyncDownServiceResponse response = new SyncDownServiceResponse();
    Service service1 = new Service();
    Service service2 = new Service();
    service1.setCode("serviceCode");
    service1.setName("serviceName");
    service1.setActive(false);
    service2.setCode("serviceCode1");
    service2.setName("serviceName1");
    service2.setActive(true);
    response.setLatestServices(newArrayList(service1, service2));
    when(lmisRestApi
        .fetchPTVService(sharedPreferenceMgr.getLastSyncServiceTime(), Constants.PTV_PROGRAM_CODE))
        .thenReturn(response);
    return response;
  }

  private SyncDownProgramDataResponse getRapidTestsResponse() {
    ProgramDataForm programDataForm1 = new ProgramDataFormBuilder()
        .setProgram(new Program())
        .setPeriod(DateUtil.parseString("2016-03-21", DateUtil.DB_DATE_FORMAT))
        .setStatus(Status.AUTHORIZED)
        .setSynced(false).build();
    ProgramDataForm programDataForm2 = new ProgramDataFormBuilder()
        .setProgram(new Program())
        .setPeriod(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT))
        .setStatus(Status.AUTHORIZED)
        .setSynced(false).build();

    SyncDownProgramDataResponse syncDownProgramDataResponse = new SyncDownProgramDataResponse();
    syncDownProgramDataResponse
        .setProgramDataForms(newArrayList(programDataForm1, programDataForm2));
    return syncDownProgramDataResponse;
  }

  private FacilityInfoResponse getFacilityInfoResponse() {
    FacilityInfoResponse facilityInfoResponse = new FacilityInfoResponse();
    List<SupportedProgram> supportedPrograms = new ArrayList<>();
    List<ReportTypeForm> supportedReportTypes = new ArrayList<>();
    facilityInfoResponse.setCode("");
    facilityInfoResponse.setName("");
    facilityInfoResponse.setSupportedPrograms(supportedPrograms);
    facilityInfoResponse.setSupportedReportTypes(supportedReportTypes);
    facilityInfoResponse.setAndroid(true);
    return facilityInfoResponse;
  }

  private SyncDownRegimensResponse getRegimenResponse() {
    SyncDownRegimensResponse syncDownRegimensResponse = new SyncDownRegimensResponse();
    List<Regimen> regimenList = new ArrayList<>();
    syncDownRegimensResponse.setRegimenList(regimenList);
    return syncDownRegimensResponse;
  }

  private SyncUpProgramResponse getFetchProgramsResponse() throws ParseException, LMISException {
    SyncUpProgramResponse syncUpProgramResponse = new SyncUpProgramResponse();
    Program program1 = new Program();
    Program program2 = new Program();
    StockMovementItemBuilder builder = new StockMovementItemBuilder();

    stockMovementItem = builder.build();
    stockMovementItem.setSynced(false);
    when(stockMovementRepository.queryFirstStockMovementByStockCardId(anyInt()))
        .thenReturn(stockMovementItem);
    syncUpProgramResponse.setPrograms(newArrayList(program1, program2));
    return syncUpProgramResponse;
  }

  private SyncDownReportTypeResponse getReportTypeResponse() {
    DateTime dateTime = new DateTime(DateUtil.getCurrentDate().getTime());
    ReportTypeForm reportTypeMMIA = new ReportTypeBuilder()
        .setActive(true)
        .setCode(Constants.MMIA_REPORT)
        .setName(Constants.MMIA_REPORT)
        .setStartTime(dateTime.toDate())
        .build();
    ReportTypeForm reportTypeVIA = new ReportTypeBuilder()
        .setActive(true)
        .setCode(Constants.VIA_REPORT)
        .setName(Constants.VIA_REPORT)
        .setStartTime(dateTime.toDate())
        .build();
    SyncDownReportTypeResponse syncDownReportTypeResponse = new SyncDownReportTypeResponse();
    syncDownReportTypeResponse.setReportTypes(newArrayList(reportTypeMMIA, reportTypeVIA));
    return syncDownReportTypeResponse;
  }

  private void verifyLastMonthStockCardsSynced() throws LMISException, SQLException {
    verify(lmisRestApi).fetchStockMovementData(anyString(), anyString());

    verify(sharedPreferenceMgr).setIsNeedsInventory(false);
    verify(stockRepository).batchCreateSyncDownStockCardsAndMovements(any(List.class));
  }

  private StockCardsLocalResponse getStockCardResponse() throws ParseException {
    StockCard stockCard1 = StockCardBuilder.buildStockCard();
    StockCard stockCard2 = StockCardBuilder.buildStockCard();

    StockMovementItemBuilder builder = new StockMovementItemBuilder();

    stockMovementItem = builder.build();
    stockMovementItem.setSynced(false);

    ArrayList<StockMovementItem> stockMovementItems1 = newArrayList(stockMovementItem,
        stockMovementItem, stockMovementItem);
    stockCard1.setStockMovementItemsWrapper(stockMovementItems1);

    ArrayList<StockMovementItem> stockMovementItems2 = newArrayList(stockMovementItem,
        stockMovementItem);
    stockCard2.setStockMovementItemsWrapper(stockMovementItems2);

    StockCardsLocalResponse stockcardLocalResponse = new StockCardsLocalResponse();
    stockcardLocalResponse.setStockCards(newArrayList(stockCard1, stockCard2));
    return stockcardLocalResponse;
  }

  private class SyncServerDataSubscriber extends CountOnNextSubscriber {

    @Override
    public void onNext(SyncProgress syncProgress) {
      super.onNext(syncProgress);
      try {
        testSyncProgress(syncProgress);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private class CountOnNextSubscriber extends TestSubscriber<SyncProgress> {

    public List<SyncProgress> syncProgresses = new ArrayList<>();

    @Override
    public void onNext(SyncProgress syncProgress) {
      syncProgresses.add(syncProgress);
    }

  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(RnrFormRepository.class).toInstance(rnrFormRepository);
      bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
      bind(ProgramRepository.class).toInstance(programRepository);
      bind(ProductRepository.class).toInstance(productRepository);
      bind(StockRepository.class).toInstance(stockRepository);
      bind(StockMovementRepository.class).toInstance(stockMovementRepository);
    }
  }
}
