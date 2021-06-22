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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenItemRepository;
import org.openlmis.core.service.SyncUpManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class MMIARequisitionPresenterTest {

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
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    MockitoAnnotations.initMocks(this);

    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(MMIARequisitionPresenter.class);
    presenter.attachView(mockMMIAformView);
    rnRForm = new RnRForm();
    rnRForm.setStatus(Status.DRAFT);

    when(mmiaRepository.initNormalRnrForm(null)).thenReturn(rnRForm);
    when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);

    presenter.loadDataOnNextAction.call(rnRForm);
  }

  @Test
  public void shouldGetInitMMIAForm() throws LMISException, SQLException {
    presenter.rnRForm = null;
    when(mmiaRepository.queryUnAuthorized()).thenReturn(null);
    presenter.getRnrForm(0);
    verify(mmiaRepository).queryUnAuthorized();
    verify(mmiaRepository).initNormalRnrForm(null);
  }

  @Test
  public void shouldGetDraftMMIAForm() throws LMISException {
    presenter.rnRForm = null;
    when(mmiaRepository.queryUnAuthorized()).thenReturn(new RnRForm());
    presenter.getRnrForm(0);
    verify(mmiaRepository).queryUnAuthorized();
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

    verify(mmiaRepository).queryUnAuthorized();
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

  private void waitObservableToExecute() {
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private ArrayList<RegimenItem> generateRegimenItems() {
    ArrayList<RegimenItem> regimenItems = new ArrayList<>();
    RegimenItem regimenItem = new RegimenItem();
    regimenItem.setAmount(100L);
    regimenItems.add(regimenItem);

    return regimenItems;
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
