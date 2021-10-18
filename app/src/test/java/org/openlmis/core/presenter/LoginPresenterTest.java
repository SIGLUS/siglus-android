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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.SHOULD_GO_TO_INITIAL_INVENTORY;

import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.LoginErrorType;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.Subscriber;

@RunWith(LMISTestRunner.class)
public class LoginPresenterTest {

  UserRepository userRepository;
  RnrFormRepository rnrFormRepository;
  LoginActivity mockActivity;
  LoginPresenter presenter;
  SyncService syncService;

  private Subscriber<SyncProgress> syncSubscriber;
  private SyncDownManager syncDownManager;
  private ProgramRepository programRepository;

  @Before
  public void setup() {
    userRepository = mock(UserRepository.class);
    programRepository = mock(ProgramRepository.class);
    rnrFormRepository = mock(RnrFormRepository.class);
    mockActivity = mock(LoginActivity.class);
    syncService = mock(SyncService.class);
    syncDownManager = mock(SyncDownManager.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(LoginPresenter.class);
    presenter.attachView(mockActivity);
    syncSubscriber = presenter.getSyncSubscriber();
    presenter = spy(presenter);
  }

  @Test
  public void shouldShowUserNameEmptyWhenUserNameIsEmpty() {

    // when
    presenter.startLogin("", "password", false);

    // then
    verify(mockActivity).showUserNameEmpty();
  }

  @Test
  public void shouldShowPasswordEmptyWhenPasswordIsEmpty() {

    // when
    presenter.startLogin("123", "", false);

    // then
    verify(mockActivity).showPasswordEmpty();
  }

  @Test
  public void shouldShowInvalidAlertAfterLoginFailed() {
    // when
    presenter.onLoginFailed(LoginErrorType.NO_INTERNET);

    // then
    verify(mockActivity, times(1)).loaded();
    verify(mockActivity, times(1)).showInvalidAlert(LoginErrorType.NO_INTERNET);
  }

  @Test
  public void shouldToastSyncProductFailWhenNotSyncProduct() {
    // given
    SharedPreferenceMgr.getInstance().setLastSyncProductTime(null);

    // when
    presenter.syncLocalUserData();

    // then
    verify(mockActivity, times(1)).loaded();
    assertEquals(LMISTestApp.getInstance().getString(R.string.msg_sync_products_list_failed),
        ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void shouldToastSyncStockFailWhenNotSyncStock() {
    // given
    SharedPreferenceMgr.getInstance().setLastSyncProductTime("1");
    SharedPreferenceMgr.getInstance().setIsSyncingLastYearStockCards(false);

    // when
    presenter.syncLocalUserData();

    // then
    verify(mockActivity, times(1)).loaded();
    assertEquals(LMISTestApp.getInstance().getString(R.string.msg_sync_stock_movement_failed),
        ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void shouldToastSyncRequisitionFailWhenNotSyncRequisition() {
    // given
    SharedPreferenceMgr.getInstance().setLastSyncProductTime("1");
    SharedPreferenceMgr.getInstance().setLastMonthStockCardDataSynced(true);
    SharedPreferenceMgr.getInstance().setRequisitionDataSynced(false);

    // when
    presenter.syncLocalUserData();

    // then
    verify(mockActivity, times(1)).loaded();
    assertEquals(LMISTestApp.getInstance().getString(R.string.msg_sync_requisition_failed),
        ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void shouldGotoNextPageWithLocalLogin() {
    // given
    SharedPreferenceMgr.getInstance().setLastSyncProductTime("1");
    SharedPreferenceMgr.getInstance().setLastMonthStockCardDataSynced(true);
    SharedPreferenceMgr.getInstance().setRequisitionDataSynced(true);

    // when
    presenter.syncLocalUserData();

    // then
    verify(mockActivity, times(1)).loaded();
    verify(mockActivity, times(1)).goToHomePage();
  }

  @Test
  public void shouldGoToInventoryPageAndKickOffPeriodicSyncWhenSyncComplete() {
    // given
    when(mockActivity.needInitInventory()).thenReturn(true);

    // when
    syncSubscriber.onCompleted();

    // then
    verify(mockActivity).loaded();
    verify(mockActivity).goToInitInventory();
    verify(syncService).kickOff();
  }

  @Test
  public void shouldShowSyncingStockCardsMessage() {
    // when
    syncSubscriber.onNext(SyncProgress.SYNCING_STOCK_CARDS_LAST_MONTH);

    // then
    verify(mockActivity).loading(RuntimeEnvironment.application.getString(R.string.msg_sync_stock_movements_data));
  }

  @Test
  public void shouldGoToInitInventoryWhenRequisitionDataSynced() {
    when(mockActivity.needInitInventory()).thenReturn(true);
    syncSubscriber.onNext(SHOULD_GO_TO_INITIAL_INVENTORY);

    verify(mockActivity).loaded();
    verify(mockActivity).goToInitInventory();
  }

  @Test
  public void shouldLoginFailedWhenSyncDataFailed() {
    when(mockActivity.needInitInventory()).thenReturn(true);
    syncSubscriber.onError(new Exception("error"));

    verify(mockActivity, times(1)).loaded();
    verify(mockActivity, times(0)).goToInitInventory();
    verify(mockActivity, times(0)).goToHomePage();
  }

  @Test
  public void shouldCorrectLoginRemote() {
    // given
    User user = new User("CS_Role1", "password1");

    // when
    presenter.onLoginSuccess(user, true);

    // then
    verify(syncService, times(1)).createSyncAccount(user);
    verify(syncDownManager, times(1)).syncDownServerData(any());
    verify(mockActivity, times(1)).clearErrorAlerts();
    verify(mockActivity, times(1)).sendScreenToGoogleAnalyticsAfterLogin();
  }

  @Test
  public void shouldCorrectLoginLocal() {
    // given
    User user = new User("CS_Role1", "password1");
    when(userRepository.getLocalUser()).thenReturn(user);
    InternetCheckListener internetCheckListener = presenter.checkNetworkConnected(user, false);

    // when
    internetCheckListener.onResult(false);

    // then
    verify(userRepository, times(1)).mapUserFromLocal(any());
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(UserRepository.class).toInstance(userRepository);
      bind(SyncService.class).toInstance(syncService);
      bind(SyncDownManager.class).toInstance(syncDownManager);
      bind(RnrFormRepository.class).toInstance(rnrFormRepository);
      bind(ProgramRepository.class).toInstance(programRepository);
    }
  }
}
