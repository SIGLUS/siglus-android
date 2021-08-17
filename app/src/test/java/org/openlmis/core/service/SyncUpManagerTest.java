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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.SyncServerException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.CmmEntry;
import org.openlmis.core.network.model.SyncUpDeletedMovementResponse;
import org.openlmis.core.network.model.SyncUpRequisitionResponse;
import org.openlmis.core.network.model.SyncUpStockMovementDataSplitResponse;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

@RunWith(LMISTestRunner.class)
public class SyncUpManagerTest {

  private RnrFormRepository mockedRnrFormRepository;
  private SyncErrorsRepository mockedSyncErrorsRepository;
  private ProductRepository mockedProductRepository;
  private CmmRepository mockedCmmRepository;
  private SharedPreferenceMgr mockedSharedPreferenceMgr;
  private LMISRestApi mockedLmisRestApi;
  private StockRepository stockRepository;
  private SyncUpManager syncUpManager;
  private ProgramDataFormRepository mockedProgramDataFormRepository;
  private DirtyDataRepository mockedDirtyDataRepository;
  private final String facilityID = "1";
  private final String productCode = "08N04Z";

  @Before
  public void setup() throws LMISException {
    mockedRnrFormRepository = mock(RnrFormRepository.class);
    mockedSyncErrorsRepository = mock(SyncErrorsRepository.class);
    mockedProductRepository = mock(ProductRepository.class);
    mockedCmmRepository = mock(CmmRepository.class);
    mockedProgramDataFormRepository = mock(ProgramDataFormRepository.class);
    mockedSharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    mockedLmisRestApi = mock(LMISRestApi.class);
    mockedDirtyDataRepository = mock(DirtyDataRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

    syncUpManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncUpManager.class);
    syncUpManager.lmisRestApi = mockedLmisRestApi;

    stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockRepository.class);

    User user = new User("user", "123");
    user.setFacilityCode("FC1");
    user.setFacilityId(facilityID);
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
  public void shouldSubmitAllUnsyncedRequisitions() throws LMISException, SQLException {
    List<RnRForm> unSyncedList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      RnRForm form = new RnRForm();
      unSyncedList.add(form);
    }

    when(mockedRnrFormRepository.queryAllUnsyncedForms()).thenReturn(unSyncedList);

    SyncUpRequisitionResponse response = new SyncUpRequisitionResponse();
    response.setRequisitionId("1");
    when(mockedLmisRestApi.submitRequisition(any(RnRForm.class))).thenReturn(null);

    syncUpManager.syncRnr();
    verify(mockedLmisRestApi, times(10)).submitRequisition(any(RnRForm.class));
    verify(mockedRnrFormRepository, times(10)).createOrUpdateWithItems(any(RnRForm.class));
    verify(mockedSyncErrorsRepository, times(10))
        .deleteBySyncTypeAndObjectId(any(SyncType.class), anyLong());
  }

  @Test
  public void shouldCallEmergencyRequisition() throws Exception {
    RnRForm form = new RnRForm();
    form.setEmergency(true);
    when(mockedRnrFormRepository.queryAllUnsyncedForms()).thenReturn(newArrayList(form));

    syncUpManager.syncRnr();
    verify(mockedLmisRestApi).submitEmergencyRequisition(any(RnRForm.class));
  }

  @Test
  public void shouldSyncUnSyncedStockMovementData() throws LMISException, ParseException {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2021-07-20").getMillis());
    StockCard stockCard = createTestStockCardData();

    SyncUpStockMovementDataSplitResponse response = new SyncUpStockMovementDataSplitResponse();
    response.setErrorProductCodes(newArrayList());

    when(mockedLmisRestApi.syncUpStockMovementDataSplit(any(List.class)))
        .thenReturn(response);

    // when
    syncUpManager.syncStockCards();
    stockRepository.refresh(stockCard);
    List<StockMovementItem> items = newArrayList(stockCard.getForeignStockMovementItems());

    // then
    assertEquals(2, items.size());
    assertTrue(items.get(0).isSynced());
    assertTrue(items.get(1).isSynced());
    verify(mockedSyncErrorsRepository).deleteBySyncTypeAndObjectId(any(SyncType.class), anyLong());
  }

  @Test
  public void shouldSaveSyncErrorWhenUnSyncedStockMovementDataFail() throws LMISException, ParseException {
    createTestStockCardData();
    doThrow(new LMISException("mocked exception")).when(mockedLmisRestApi).syncUpStockMovementDataSplit(anyList());

    syncUpManager.syncStockCards();

    verify(mockedSyncErrorsRepository).save(any(SyncError.class));
  }

  @Test
  public void shouldNotMarkAsSyncedWhenStockMovementSyncFailed()
      throws LMISException, ParseException {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2021-07-20").getMillis());
    StockCard stockCard = createTestStockCardData();
    doThrow(new RuntimeException("Sync Failed")).when(mockedLmisRestApi)
        .syncUpStockMovementDataSplit(anyList());

    // when
    try {
      syncUpManager.syncStockCards();
    } catch (RuntimeException e) {
      Log.i("test", String.valueOf(e));
    }
    stockRepository.refresh(stockCard);
    List<StockMovementItem> items = newArrayList(stockCard.getForeignStockMovementItems());

    // then
    assertEquals(2, items.size());
    assertFalse(items.get(0).isSynced());
    assertFalse(items.get(1).isSynced());
  }

  @NonNull
  private StockCard createTestStockCardData() throws LMISException, ParseException {
    ProductRepository productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    StockCard stockCard = StockCardBuilder
        .saveStockCardWithOneMovement(stockRepository, productRepository);

    Product product = new Product();
    product.setCode("PD1");
    productRepository.createOrUpdate(product);

    stockCard.setProduct(product);
    stockRepository.createOrUpdate(stockCard);

    //ready to sync
    StockMovementItem item = new StockMovementItem();
    item.setMovementQuantity(100L);
    item.setStockOnHand(-1);
    item.setMovementDate(DateUtil.today());
    item.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    item.setReason("DISTRICT_DDM");
    item.setStockCard(stockCard);
    item.setSynced(false);

    stockRepository.addStockMovementAndUpdateStockCard(item);
    stockRepository.refresh(stockCard);
    return stockCard;
  }

  @Test
  public void shouldSyncAppVersion() throws Exception {
    when(mockedSharedPreferenceMgr.hasSyncedVersion()).thenReturn(false);
    User user = new User();
    UserInfoMgr.getInstance().setUser(user);
    syncUpManager.syncAppVersion();
    verify(mockedLmisRestApi).updateAppVersion(any(AppInfoRequest.class));
  }

  @Test
  public void shouldSyncArchivedProducts() throws Exception {
    List<String> productCodes = new ArrayList<>();
    syncUpManager.productRepository = mockedProductRepository;
    when(mockedProductRepository.listArchivedProductCodes()).thenReturn(productCodes);
    syncUpManager.syncArchivedProducts();
    verify(mockedLmisRestApi).syncUpArchivedProducts(productCodes);
  }

  @Test
  public void shouldSaveErrorMessageWhenSyncRnRFormFail() throws Exception {
    List<RnRForm> unSyncedList = new ArrayList<>();
    RnRForm form = new RnRForm();
    unSyncedList.add(form);

    when(mockedRnrFormRepository.queryAllUnsyncedForms()).thenReturn(unSyncedList);

    doThrow(new LMISException("mocked exception")).when(mockedLmisRestApi)
        .submitRequisition(any(RnRForm.class));
    syncUpManager.syncRnr();

    verify(mockedSyncErrorsRepository).save(any(SyncError.class));
  }

  @Test
  public void shouldNotSyncAppVersion() throws Exception {
    when(mockedSharedPreferenceMgr.hasSyncedVersion()).thenReturn(true);
    syncUpManager.syncAppVersion();
    verify(mockedLmisRestApi, never()).updateAppVersion(any(AppInfoRequest.class));
  }

  @Test
  public void shouldRemoveInvalidItemsFromForms() throws LMISException {
    List<RnRForm> unSyncedList = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      RnRForm form = new RnRForm();
      unSyncedList.add(form);
    }

    when(mockedRnrFormRepository.queryAllUnsyncedForms()).thenReturn(unSyncedList);

    SyncUpRequisitionResponse response = new SyncUpRequisitionResponse();
    response.setRequisitionId("1");
    when(mockedLmisRestApi.submitRequisition(any(RnRForm.class))).thenReturn(null);

    syncUpManager.syncRnr();
    verify(mockedRnrFormRepository, times(1)).queryAllUnsyncedForms();
    verify(mockedLmisRestApi, times(2)).submitRequisition(any(RnRForm.class));
    verify(mockedRnrFormRepository, times(2)).createOrUpdateWithItems(any(RnRForm.class));
    verify(mockedSyncErrorsRepository, times(2))
        .deleteBySyncTypeAndObjectId(any(SyncType.class), anyLong());
  }

  @Test
  public void shouldSyncUpUnSyncedStockCardListWhenHasNotSyncedUpLatestMovementLastDay()
      throws Exception {
    when(mockedSharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()).thenReturn(false);
    syncUpManager.syncUpUnSyncedStockCardCodes();
    verify(mockedLmisRestApi).syncUpUnSyncedStockCards(facilityID, new ArrayList<String>());
    verify(mockedSharedPreferenceMgr).setLastMovementHandShakeDateToToday();
  }

  @Test
  public void shouldRefreshLastSyncStockCardDateWhenHasNoUnSyncedStockCard() throws Exception {
    when(mockedSharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()).thenReturn(false);
    syncUpManager.syncUpUnSyncedStockCardCodes();
    verify(mockedLmisRestApi).syncUpUnSyncedStockCards(facilityID, new ArrayList<String>());
    verify(mockedSharedPreferenceMgr).setStockLastSyncTime();
  }

  @Test
  public void shouldNotSyncUpWhenHasSyncedUpLastDay() throws LMISException {
    when(mockedSharedPreferenceMgr.hasSyncedUpLatestMovementLastDay()).thenReturn(true);
    syncUpManager.syncUpUnSyncedStockCardCodes();
    verify(mockedLmisRestApi, never()).syncUpUnSyncedStockCards(facilityID, new ArrayList<String>());
    verify(mockedSharedPreferenceMgr, never()).setLastMovementHandShakeDateToToday();
  }

  @Test
  public void shouldSyncUpCmmsAndMarkThemAsSynced() throws Exception {
    //given
    List<Cmm> cmms = createCmmsData();

    Cmm cmm = cmms.get(0);
    when(mockedCmmRepository.listUnsynced()).thenReturn(cmms);

    assertThat(cmm.isSynced(), is(false));

    //when
    syncUpManager.syncUpCmms();

    //then
    verify(mockedLmisRestApi, times(1)).syncUpCmms(anyListOf(CmmEntry.class));
    assertThat(cmm.isSynced(), is(true));
    verify(mockedCmmRepository).save(cmm);
  }

  @Test
  public void shouldNotInvokeNetworkWhenNoUnsyncedCmmPresent() throws Exception {
    //given
    List<Cmm> emptyCmmsList = new ArrayList<>();
    when(mockedCmmRepository.listUnsynced()).thenReturn(emptyCmmsList);

    //when
    syncUpManager.syncUpCmms();

    //then
    verify(mockedLmisRestApi, never()).syncUpCmms(anyList());
  }

  @Test
  public void shouldNotMarkCmmsAsSyncedWhenSyncUpFails() throws Exception {
    //given sync up encounters network failure
    List<Cmm> cmms = createCmmsData();

    Cmm cmm = cmms.get(0);
    when(mockedCmmRepository.listUnsynced()).thenReturn(cmms);
    when(mockedLmisRestApi.syncUpCmms(anyList()))
        .thenThrow(new LMISException("some error"));

    assertThat(cmm.isSynced(), is(false));

    //when
    syncUpManager.syncUpCmms();

    //then
    verify(mockedLmisRestApi, times(1)).syncUpCmms(anyListOf(CmmEntry.class));
    assertThat(cmm.isSynced(), is(false));
    verify(mockedCmmRepository, never()).save(cmm);
  }

  @Test
  public void shouldSyncDeletedProductToWeb() throws Exception {
    String unSyncedProductCode = productCode;
    SyncUpDeletedMovementResponse response = new SyncUpDeletedMovementResponse();
    response.setErrorCodes(newArrayList(unSyncedProductCode));

    String unSyncedProductCode1 = productCode;
    SyncUpDeletedMovementResponse response1 = new SyncUpDeletedMovementResponse();
    response1.setErrorCodes(newArrayList(unSyncedProductCode1));

    List<DirtyDataItemInfo> dirtyDataItems = createDirtyDateItem();
    DirtyDataItemInfo item = dirtyDataItems.get(0);
    when(mockedDirtyDataRepository.listunSyced()).thenReturn(dirtyDataItems);
    when(mockedLmisRestApi.syncUpDeletedData(any(Long.class), anyList())).thenReturn(response);

    assertThat(item.isSynced(), is(false));

    syncUpManager.syncDeleteMovement();

    for (int i = 0; i < dirtyDataItems.size(); i++) {
      assertThat(dirtyDataItems.get(i).isSynced(), is(true));
    }

    assertEquals(response, response1);
  }

  @Test
  public void shouldKeepNotSyncedWhenSyncDeletedProductToWebError() throws Exception {
    String unSyncedProductCode = productCode;
    SyncUpDeletedMovementResponse response = new SyncUpDeletedMovementResponse();
    response.setErrorCodes(newArrayList(unSyncedProductCode));

    String unSyncedProductCode1 = productCode;
    SyncUpDeletedMovementResponse response1 = new SyncUpDeletedMovementResponse();
    response1.setErrorCodes(newArrayList(unSyncedProductCode1));

    List<DirtyDataItemInfo> dirtyDataItems = createDirtyDateItem();
    DirtyDataItemInfo item = dirtyDataItems.get(0);
    when(mockedDirtyDataRepository.listunSyced()).thenReturn(dirtyDataItems);
    doThrow(new SyncServerException(LMISApp.getContext().getString(R.string.sync_server_error)))
        .when(mockedLmisRestApi).syncUpDeletedData(any(Long.class), anyList());

    assertThat(item.isSynced(), is(false));

    syncUpManager.syncDeleteMovement();

    for (int i = 0; i < dirtyDataItems.size(); i++) {
      assertThat(dirtyDataItems.get(i).isSynced(), is(false));
    }

    assertEquals(response, response1);
  }

  private List<DirtyDataItemInfo> createDirtyDateItem() {
    List<DirtyDataItemInfo> list = new ArrayList<>();
    DirtyDataItemInfo dirtyDataItemInfo = new DirtyDataItemInfo();
    dirtyDataItemInfo.setSynced(false);
    dirtyDataItemInfo.setProductCode(productCode);
    String json08N04Z = JsonFileReader.readString(getClass(), "delete_08N04Z.json");
    dirtyDataItemInfo.setJsonData(json08N04Z);
    list.add(dirtyDataItemInfo);

    DirtyDataItemInfo dirtyDataItemInfo1 = new DirtyDataItemInfo();
    dirtyDataItemInfo1.setSynced(false);
    dirtyDataItemInfo1.setProductCode("04F07");
    String json04F07 = JsonFileReader.readString(getClass(), "delete_04F07.json");
    dirtyDataItemInfo1.setJsonData(json04F07);
    list.add(dirtyDataItemInfo1);

    DirtyDataItemInfo dirtyDataItemInfo2 = new DirtyDataItemInfo();
    dirtyDataItemInfo2.setSynced(false);
    dirtyDataItemInfo2.setProductCode("02C02");
    String json02C02 = JsonFileReader.readString(getClass(), "delete_02C02.json");
    dirtyDataItemInfo2.setJsonData(json02C02);
    list.add(dirtyDataItemInfo2);

    DirtyDataItemInfo dirtyDataItemInfo3 = new DirtyDataItemInfo();
    dirtyDataItemInfo3.setSynced(false);
    dirtyDataItemInfo3.setProductCode("02C01");
    String json02C01 = JsonFileReader.readString(getClass(), "delete_02C01.json");
    dirtyDataItemInfo3.setJsonData(json02C01);
    list.add(dirtyDataItemInfo3);

    return list;
  }

  private List<Cmm> createCmmsData() throws LMISException, ParseException {
    Cmm cmm = new Cmm();
    cmm.setStockCard(createTestStockCardData());
    cmm.setPeriodBegin(new Date());
    cmm.setPeriodEnd(new Date());

    List<Cmm> cmms = new ArrayList<>();
    cmms.add(cmm);
    return cmms;
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(RnrFormRepository.class).toInstance(mockedRnrFormRepository);
      bind(SharedPreferenceMgr.class).toInstance(mockedSharedPreferenceMgr);
      bind(SyncErrorsRepository.class).toInstance(mockedSyncErrorsRepository);
      bind(CmmRepository.class).toInstance(mockedCmmRepository);
      bind(ProgramDataFormRepository.class).toInstance(mockedProgramDataFormRepository);
      bind(DirtyDataRepository.class).toInstance(mockedDirtyDataRepository);
    }
  }
}
