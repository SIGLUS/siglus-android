package org.openlmis.core.service;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
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
import org.openlmis.core.model.builder.ProductProgramBuilder;
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
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.SupportedProgram;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
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
  }

  @Test
  public void shouldSyncDownServerData() throws Exception {
    // given
    mockFacilityInfoResponse();
    mockRegimenResponse();
    mockSyncDownLatestProductResponse();
    mockRequisitionResponse();
    mockStockCardsResponse();
    mockFetchPodsResponse();

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
    mockSyncDownLatestProductResponse();
    mockRequisitionResponse();
    mockStockCardsResponse();
    mockFetchPodsResponse();

    // when
    CountOnNextSubscriber firstEnterSubscriber = new CountOnNextSubscriber();
    CountOnNextSubscriber laterEnterSubscriber = new CountOnNextSubscriber();
    syncDownManager.syncDownServerData(firstEnterSubscriber);
    syncDownManager.syncDownServerData(laterEnterSubscriber);

    firstEnterSubscriber.awaitTerminalEvent();
    firstEnterSubscriber.assertNoErrors();
    laterEnterSubscriber.assertNoTerminalEvent();

    // then
    assertEquals(12, firstEnterSubscriber.syncProgresses.size());
    assertEquals(0, laterEnterSubscriber.syncProgresses.size());
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

  @Test
  public void shouldSetNewShippedProgramNamesWhenNewPodsContainShippedPods() throws LMISException {
    // given
    String ivProgramCode = "IV";
    String mmiaProgramCode = "MMIA";

    Pod shippedIVPods = createMockedPod(OrderStatus.SHIPPED, ivProgramCode);
    Pod shippedIVPods2 = createMockedPod(OrderStatus.SHIPPED, ivProgramCode);
    Pod shippedMMIAPods = createMockedPod(OrderStatus.SHIPPED, mmiaProgramCode);
    Pod receivedPods = createMockedPod(OrderStatus.RECEIVED, "MMTB");

    String ivProgramName = "IV NAME";
    Program mockedIvProgram = createMockedProgram(ivProgramCode, ivProgramName);
    String mmiaProgramName = "MMIA NAME";
    Program mockedMMIAProgram = createMockedProgram(mmiaProgramCode, mmiaProgramName);
    when(programRepository.list()).thenReturn(newArrayList(mockedIvProgram, mockedMMIAProgram));

    // when
    syncDownManager.saveNewShippedProgramNames(
        newArrayList(shippedIVPods, receivedPods, shippedIVPods2, shippedMMIAPods)
    );
    // then
    verify(sharedPreferenceMgr).setNewShippedProgramNames(ivProgramName + ", " + mmiaProgramName);
  }

  @Test
  public void shouldAddNewShippedProgramNamesWhenNewPodsContainShippedPodsAndHasExistingPods()
      throws LMISException {
    // given
    String ivProgramCode = "IV";
    String mmiaProgramCode = "MMIA";
    String mmtbProgramName = "MMTB NAME";

    Pod shippedIVPods = createMockedPod(OrderStatus.SHIPPED, ivProgramCode);
    Pod shippedMMIAPods = createMockedPod(OrderStatus.SHIPPED, mmiaProgramCode);

    String ivProgramName = "IV NAME";
    Program mockedIvProgram = createMockedProgram(ivProgramCode, ivProgramName);
    String mmiaProgramName = "MMIA NAME";
    Program mockedMMIAProgram = createMockedProgram(mmiaProgramCode, mmiaProgramName);
    when(programRepository.list()).thenReturn(newArrayList(mockedIvProgram, mockedMMIAProgram));

    when(sharedPreferenceMgr.getNewShippedProgramNames()).thenReturn(mmtbProgramName);

    // when
    syncDownManager.saveNewShippedProgramNames(newArrayList(shippedIVPods, shippedMMIAPods));
    // then
    verify(sharedPreferenceMgr).setNewShippedProgramNames(
        ivProgramName + ", " + mmiaProgramName + ", " + mmtbProgramName
    );
  }

  @NonNull
  private Pod createMockedPod(OrderStatus orderStatus, String ivProgramCode) {
    Pod shippedIVPods = mock(Pod.class);
    when(shippedIVPods.getOrderStatus()).thenReturn(orderStatus);
    when(shippedIVPods.getRequisitionProgramCode()).thenReturn(ivProgramCode);
    return shippedIVPods;
  }

  @NonNull
  private static Program createMockedProgram(String ivProgramCode, String ivProgramName) {
    Program mockedProgram = mock(Program.class);
    when(mockedProgram.getProgramName()).thenReturn(ivProgramName);
    when(mockedProgram.getProgramCode()).thenReturn(ivProgramCode);
    return mockedProgram;
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

  private void mockFacilityInfoResponse() throws LMISException {
    FacilityInfoResponse facilityInfoResponse = getFacilityInfoResponse();
    when(lmisRestApi.fetchFacilityInfo()).thenReturn(facilityInfoResponse);
  }

  private void mockRegimenResponse() throws LMISException {
    SyncDownRegimensResponse syncDownRegimensResponse = getRegimenResponse();
    when(lmisRestApi.fetchRegimens()).thenReturn(syncDownRegimensResponse);
  }

  private void mockFetchPodsResponse() throws LMISException {
    List<Pod> pods = new ArrayList<>();
    when(lmisRestApi.fetchPods(false)).thenReturn(pods);
    when(lmisRestApi.fetchPods(true)).thenReturn(pods);
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
      bind(StockService.class).toInstance(stockService);
    }
  }
}
