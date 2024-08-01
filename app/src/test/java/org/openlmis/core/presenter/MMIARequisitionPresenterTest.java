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

package org.openlmis.core.presenter;


import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;
import com.google.inject.AbstractModule;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenItemRepository;
import org.openlmis.core.service.SyncUpManager;
import org.openlmis.core.utils.Constants;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class MMIARequisitionPresenterTest {
  private static String TABLE_DISPENSED_KEY = "table_dispensed_key";
  private static String DEFAULT_DISPENSED_VALUE = "0";

  private SyncUpManager syncUpManager;
  private MMIARequisitionPresenter presenter;
  private MMIARepository mmiaRepository;
  private ProgramRepository programRepository;
  private MMIARequisitionPresenter.MMIARequisitionView mockMMIAformView;
  private RnRForm rnRForm;
  private RegimenItemRepository regimenItemRepository;

  @Before
  public void setup() throws Exception {
    mmiaRepository = mock(MMIARepository.class);
    programRepository = mock(ProgramRepository.class);
    mockMMIAformView = mock(MMIARequisitionPresenter.MMIARequisitionView.class);
    syncUpManager = mock(SyncUpManager.class);
    regimenItemRepository = mock(RegimenItemRepository.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new MyTestModule());
    MockitoAnnotations.initMocks(this);

    presenter = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(MMIARequisitionPresenter.class);
    presenter.attachView(mockMMIAformView);
    rnRForm = new RnRForm();
    rnRForm.setStatus(Status.DRAFT);

    when(mmiaRepository.initNormalRnrForm(null)).thenReturn(rnRForm);

    presenter.loadDataOnNextAction.call(rnRForm);
  }

  @Test
  public void shouldGetInitMMIAForm() throws LMISException, SQLException {
    presenter.rnRForm = null;
    when(mmiaRepository.queryLastDraftOrSubmittedForm()).thenReturn(null);
    presenter.getRnrForm(0);
    verify(mmiaRepository).queryLastDraftOrSubmittedForm();
    verify(mmiaRepository).initNormalRnrForm(null);
  }

  @Test
  public void shouldGetDraftMMIAForm() throws LMISException {
    presenter.rnRForm = null;
    when(mmiaRepository.queryLastDraftOrSubmittedForm()).thenReturn(new RnRForm());
    presenter.getRnrForm(0);
    verify(mmiaRepository).queryLastDraftOrSubmittedForm();
    verify(mmiaRepository, never()).initNormalRnrForm(null);
  }

  @Test
  public void shouldShowErrorWhenLoadRnRFormOnError() {
    reset(mockMMIAformView);
    presenter.loadDataOnErrorAction.call(new Exception("I am testing the onError action"));
    verify(mockMMIAformView).loaded();
    assertEquals("I am testing the onError action", ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void shouldRefreshViewWhenLoadRnRFormOnNext() {
    reset(mockMMIAformView);
    RnRForm rnRForm = new RnRForm();
    presenter.loadDataOnNextAction.call(rnRForm);

    verify(mockMMIAformView).refreshRequisitionForm(rnRForm);
    verify(mockMMIAformView).loaded();
  }

  @Test
  public void shouldQueryRnRFormWhenFormIdIsValid() throws LMISException {
    presenter.rnRForm = null;
    presenter.getRnrForm(100L);
    verify(mmiaRepository).queryRnRForm(100L);
  }

  @Test
  public void shouldUnAuthorizedWhenFormIdIsInvalid() throws Exception {
    presenter.rnRForm = null;
    presenter.getRnrForm(0);

    verify(mmiaRepository).queryLastDraftOrSubmittedForm();
  }

  @Test
  public void shouldSubmitFormWhenTheStatusIsDraft() throws LMISException {
    RnRForm form = new RnRForm();
    form.setStatus(Status.DRAFT);

    String signature = "signature";
    presenter.rnRForm = form;
    presenter.processSign(signature);
    waitObservableToExecute();

    assertThat(Status.SUBMITTED, is(form.getStatus()));
    verify(mmiaRepository).createOrUpdateWithItems(form);
    verify(mockMMIAformView).setProcessButtonName(
        LMISTestApp.getContext().getResources().getString(R.string.btn_complete));
    verify(mockMMIAformView).showMessageNotifyDialog();
  }

  @Test
  public void shouldAuthorizeFormWhenStatusIsSubmitted() throws LMISException {
    RnRForm form = new RnRForm();
    form.setStatus(Status.SUBMITTED);

    String signature = "signature";
    presenter.rnRForm = form;
    presenter.processSign(signature);

    waitObservableToExecute();

    assertThat(Status.AUTHORIZED, is(form.getStatus()));
    verify(mmiaRepository).createOrUpdateWithItems(form);
  }

  @Test
  public void shouldCreateCustomRegimenItem() throws Exception {
    List<RegimenItem> regimenItemListWrapper = rnRForm.getRegimenItemListWrapper();
    int size = regimenItemListWrapper.size();
    Regimen regimen = new Regimen();
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    presenter.addCustomRegimenItem(regimen).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    verify(regimenItemRepository).create(any(RegimenItem.class));
    assertThat(regimenItemListWrapper.size(), is(size + 1));
  }

  @Test
  public void shouldNotCreateCustomRegimenItemWhenExists() throws Exception {
    List<RegimenItem> regimenItemListWrapper = rnRForm.getRegimenItemListWrapper();
    Regimen regimen = new Regimen();
    regimen.setId(12);
    regimen.setType(Regimen.RegimeType.Paediatrics);
    regimen.setName("test");
    RegimenItem regimenItem = new RegimenItem();
    regimenItem.setRegimen(regimen);
    regimenItemListWrapper.add(regimenItem);
    int size = regimenItemListWrapper.size();

    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    presenter.addCustomRegimenItem(regimen).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    verify(regimenItemRepository, never()).create(any(RegimenItem.class));
    assertThat(regimenItemListWrapper.size(), is(size));
  }

  @Test
  public void shouldDeleteCustomRegimenItem() throws Exception {
    List<RegimenItem> regimenItemListWrapper = rnRForm.getRegimenItemListWrapper();
    RegimenItem item = new RegimenItem();
    regimenItemListWrapper.add(item);
    int size = regimenItemListWrapper.size();

    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    presenter.deleteRegimeItem(item).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    verify(regimenItemRepository).deleteRegimeItem(item);
    assertThat(regimenItemListWrapper.size(), is(size - 1));
  }

  @Test
  public void shouldReturnTrueWhenHasExist() throws Exception {
    Regimen regimen = new Regimen();
    regimen.setId(100L);
    regimen.setType(Regimen.RegimeType.Adults);
    regimen.setName("test");
    RegimenItem regimenItem = new RegimenItem();
    regimenItem.setRegimen(regimen);
    presenter.getRnRForm().getRegimenItemListWrapper().add(regimenItem);

    assertTrue(presenter.isRegimeItemExists(regimen));
  }

  @Test
  public void shouldLoadData() {
    presenter.loadData(10L, null);
    Assert.assertNotNull(presenter.subscriptions);
    Assert.assertEquals(1, presenter.subscriptions.size());
  }

  @Test
  public void shouldGetRnrFormObservable() {
    Observable<RnRForm> rnrFormObservableValidFormId = presenter.getRnrFormObservable(100L);
    assertNotNull(rnrFormObservableValidFormId);
  }

  @Test
  public void getRnrFormObservable_shouldAddDBDataWithoutValueWhenTemplateIsOldV2AndIsDraft() {
    mockForGetRnrFormObservable(true, true);
    // action
    Observable<RnRForm> rnrFormObservable = presenter.getRnrFormObservable(100);
    // assertion
    TestSubscriber<RnRForm> rnRFormTestSubscriber = new TestSubscriber<>();
    rnrFormObservable.subscribe(rnRFormTestSubscriber);
    rnRFormTestSubscriber.awaitTerminalEvent();
    List<RnRForm> onNextEvents = rnRFormTestSubscriber.getOnNextEvents();

    assertEquals(1, onNextEvents.size());
    RnRForm rnRForm = onNextEvents.get(0);

    List<BaseInfoItem> baseInfoItemListWrapper = rnRForm.getBaseInfoItemListWrapper();
    assertEquals(25, baseInfoItemListWrapper.size());
    String tableDispensedKey = "table_dispensed_key";

    BaseInfoItem db1Item = new BaseInfoItem(
            "dispensed_db1",
            BaseInfoItem.TYPE.INT,
            rnRForm,
            tableDispensedKey,
            32
    );
    BaseInfoItem actualDb1Item = baseInfoItemListWrapper.get(23);
    assertEquals(db1Item, actualDb1Item);
    assertEquals(null, actualDb1Item.getValue());
    BaseInfoItem dbItem = new BaseInfoItem(
            "dispensed_db",
            BaseInfoItem.TYPE.INT,
            rnRForm,
            tableDispensedKey,
            33
    );
    BaseInfoItem actualDbItem = baseInfoItemListWrapper.get(24);
    assertEquals(dbItem, actualDbItem);
    assertEquals(null, actualDbItem.getValue());
  }

  @Test
  public void getRnrFormObservable_shouldAddDBDataWith0ValueWhenTemplateIsOldV2AndIsNotDraft() {
    mockForGetRnrFormObservable(true, false);
    // action
    Observable<RnRForm> rnrFormObservable = presenter.getRnrFormObservable(100);
    // assertion
    TestSubscriber<RnRForm> rnRFormTestSubscriber = new TestSubscriber<>();
    rnrFormObservable.subscribe(rnRFormTestSubscriber);
    rnRFormTestSubscriber.awaitTerminalEvent();
    List<RnRForm> onNextEvents = rnRFormTestSubscriber.getOnNextEvents();

    assertEquals(1, onNextEvents.size());
    RnRForm rnRForm = onNextEvents.get(0);

    List<BaseInfoItem> baseInfoItemListWrapper = rnRForm.getBaseInfoItemListWrapper();
    assertEquals(25, baseInfoItemListWrapper.size());

    BaseInfoItem db1Item = new BaseInfoItem(
            "dispensed_db1",
            BaseInfoItem.TYPE.INT,
            rnRForm,
            TABLE_DISPENSED_KEY,
            32
    );
    BaseInfoItem actualDb1Item = baseInfoItemListWrapper.get(23);
    assertEquals(db1Item, actualDb1Item);
    assertEquals(DEFAULT_DISPENSED_VALUE, actualDb1Item.getValue());

    BaseInfoItem dbItem = new BaseInfoItem(
            "dispensed_db",
            BaseInfoItem.TYPE.INT,
            rnRForm,
            TABLE_DISPENSED_KEY,
            33
    );

    BaseInfoItem actualDbItem = baseInfoItemListWrapper.get(24);
    assertEquals(dbItem, actualDbItem);
    assertEquals(DEFAULT_DISPENSED_VALUE, actualDbItem.getValue());
  }

  @Test
  public void getRnrFormObservable_shouldNotAddDBDataWith0ValueWhenTemplateIsNotOldV2() {
    // Given
    mockForGetRnrFormObservable(false, false);
    // action
    Observable<RnRForm> rnrFormObservable = presenter.getRnrFormObservable(100);
    // assertion
    TestSubscriber<RnRForm> rnRFormTestSubscriber = new TestSubscriber<>();
    rnrFormObservable.subscribe(rnRFormTestSubscriber);
    rnRFormTestSubscriber.awaitTerminalEvent();
    List<RnRForm> onNextEvents = rnRFormTestSubscriber.getOnNextEvents();

    assertEquals(1, onNextEvents.size());
    RnRForm rnRForm = onNextEvents.get(0);

    assertEquals(23, rnRForm.getBaseInfoItemListWrapper().size());
  }

  private void mockForGetRnrFormObservable(boolean isOldMMIALayoutV2, boolean isDraft) {
    RnRForm mockedRnRForm = mock(RnRForm.class);
    when(mockedRnRForm.isOldMMIALayoutV2()).thenReturn(isOldMMIALayoutV2);
    when(mockedRnRForm.isDraft()).thenReturn(isDraft);

    ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();
    for (int i = 0; i < 23; i++) {
      baseInfoItems.add(mock(BaseInfoItem.class));
    }
    when(mockedRnRForm.getBaseInfoItemListWrapper()).thenReturn(baseInfoItems);

    presenter.rnRForm = mockedRnRForm;
  }

  @Test
  public void shouldGetSaveFormObservable() {
    Observable<Void> saveFormObservable = presenter.getSaveFormObservable(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);
    assertNotNull(saveFormObservable);
  }

  @Test
  public void shouldSetViewModels() {
    //given
    List<RnrFormItem> rnrFormItems = new ArrayList<>();
    rnrFormItems.add(new RnrFormItem());
    List<RegimenItem> regimenItems = new ArrayList<>();
    regimenItems.add(new RegimenItem());
    List<BaseInfoItem> baseInfoItems = new ArrayList<>();
    baseInfoItems.add(new BaseInfoItem());
    List<RegimenItemThreeLines> threeLinesList = new ArrayList<>();
    threeLinesList.add(new RegimenItemThreeLines());

    //when
    presenter.setViewModels(rnrFormItems, regimenItems, baseInfoItems, threeLinesList, "comment");

    //then
    assertEquals(1, rnRForm.getRnrFormItemListWrapper().size());
    assertEquals(1, rnRForm.getRegimenItemListWrapper().size());
    assertEquals(1, rnRForm.getBaseInfoItemListWrapper().size());
    assertEquals(1, rnRForm.getRegimenThreeLinesWrapper().size());
    assertEquals("comment", rnRForm.getComments());
  }

  @Test
  public void getLastRnrForm_shouldNotUpdateBaseInfoItemsDataWhenIsNotOldTemplate() {
    RnRForm mockedRnrForm = mock(RnRForm.class);
    List<RnRForm> rnrForms = new ArrayList<>();
    rnrForms.add(mockedRnrForm);
    rnrForms.add(mockedRnrForm);

    when(mmiaRepository.listInclude(RnRForm.Emergency.NO, Constants.MMIA_PROGRAM_CODE)).thenReturn(rnrForms);
    when(mockedRnrForm.getPeriodBegin()).thenReturn(mock(Date.class));
    when(mockedRnrForm.isOldMMIALayoutV2()).thenReturn(false);
    List mockBaseInfoItemListWrapper = mock(List.class);
    when(mockedRnrForm.getBaseInfoItemListWrapper()).thenReturn(mockBaseInfoItemListWrapper);
    // action
    RnRForm actualRnrForm = presenter.getLastRnrForm();
    // assertion
    assertEquals(mockBaseInfoItemListWrapper, actualRnrForm.getBaseInfoItemListWrapper());
  }

  @Test
  public void getLastRnrForm_shouldUpdateBaseInfoItemsDataWhenIsOldTemplate() {
    RnRForm mockedRnrForm = mock(RnRForm.class);
    List<RnRForm> rnrForms = new ArrayList<>();
    rnrForms.add(mockedRnrForm);
    rnrForms.add(mockedRnrForm);

    when(mmiaRepository.listInclude(RnRForm.Emergency.NO, Constants.MMIA_PROGRAM_CODE)).thenReturn(rnrForms);
    when(mockedRnrForm.getPeriodBegin()).thenReturn(mock(Date.class));
    when(mockedRnrForm.isOldMMIALayoutV2()).thenReturn(true);
    List<BaseInfoItem> baseInfoItems = new ArrayList();
    for (int i = 0; i < 23;i++) {
      baseInfoItems.add(mock(BaseInfoItem.class));
    }
    when(mockedRnrForm.getBaseInfoItemListWrapper()).thenReturn(baseInfoItems);
    // action
    RnRForm actualRnrForm = presenter.getLastRnrForm();
    // assertion
    List<BaseInfoItem> actualBaseInfoItemListWrapper = actualRnrForm.getBaseInfoItemListWrapper();
    assertEquals(24, actualBaseInfoItemListWrapper.size());

    BaseInfoItem dbItem = new BaseInfoItem(
            "dispensed_db",
            BaseInfoItem.TYPE.INT,
            mockedRnrForm,
            TABLE_DISPENSED_KEY,
            33
    );
    dbItem.setValue(DEFAULT_DISPENSED_VALUE);
    assertEquals(dbItem, actualBaseInfoItemListWrapper.get(23));
  }

  private void waitObservableToExecute() {
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      Log.w("waitObservableToExecute", e);
    }
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(MMIARepository.class).toInstance(mmiaRepository);
      bind(ProgramRepository.class).toInstance(programRepository);
      bind(MMIARequisitionPresenter.MMIARequisitionView.class).toInstance(mockMMIAformView);
      bind(SyncUpManager.class).toInstance(syncUpManager);
      bind(RegimenItemRepository.class).toInstance(regimenItemRepository);
    }
  }
}
