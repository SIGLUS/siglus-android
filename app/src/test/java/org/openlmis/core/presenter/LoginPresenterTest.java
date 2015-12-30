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

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.repository.UserRepository.NewCallback;
import org.openlmis.core.network.model.SyncDownProductsResponse;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import roboguice.RoboGuice;
import rx.Subscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.RequisitionSynced;

@RunWith(LMISTestRunner.class)
public class LoginPresenterTest {

    UserRepository userRepository;
    RnrFormRepository rnrFormRepository;
    LoginActivity mockActivity;
    LoginPresenter presenter;
    SyncDownProductsResponse mockSyncDownProductsResponse;
    SyncService syncService;

    @Captor
    private ArgumentCaptor<NewCallback<User>> loginCB;

    private LMISTestApp appInject;
    private Subscriber<SyncProgress> syncSubscriber;
    private SyncDownManager syncDownManager;

    @Before
    public void setup() {
        appInject = (LMISTestApp) RuntimeEnvironment.application;

        userRepository = mock(UserRepository.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        mockActivity = mock(LoginActivity.class);
        mockSyncDownProductsResponse = mock(SyncDownProductsResponse.class);
        syncService = mock(SyncService.class);
        syncDownManager = mock(SyncDownManager.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(LoginPresenter.class);
        presenter.attachView(mockActivity);
        syncSubscriber = presenter.getSyncSubscriber();
        presenter = spy(presenter);
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldSaveUserToLocalDBWhenSuccess() throws InterruptedException {
        appInject.setNetworkConnection(true);

        presenter.startLogin("user", "password");

        verify(mockActivity).loading();

        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());
        loginCB.getValue().success(new User("user", "password"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void shouldCreateSyncAccountWhenLoginSuccess() {
        appInject.setNetworkConnection(true);

        presenter.startLogin("user", "password");
        User user = new User("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());

        loginCB.getValue().success(user);

        verify(syncService).createSyncAccount(user);
    }

    @Test
    public void shouldSaveUserInfoWhenLoginSuccess() {
        appInject.setNetworkConnection(true);

        presenter.startLogin("user", "password");
        User user = new User("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());

        loginCB.getValue().success(user);

        verify(userRepository).save(user);
        assertThat(UserInfoMgr.getInstance().getUser()).isEqualTo(user);
        verify(mockActivity).clearErrorAlerts();
    }

    @Test
    public void shouldSyncServerDataWhenLoginSuccessFromNet() {
        appInject.setNetworkConnection(true);

        presenter.startLogin("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());
        loginCB.getValue().success(new User("user", "password"));

        verify(syncDownManager).syncDownServerData(any(Subscriber.class));
    }

    @Test
    public void shouldGoToInventoryPageAndKickOffPeriodicSyncIfSyncServerDataSuccess() throws InterruptedException {
        //given
        when(mockActivity.needInitInventory()).thenReturn(true);

        //when
        syncSubscriber.onCompleted();

        //then
        verify(mockActivity).loaded();
        verify(mockActivity).goToInitInventory();
        verify(syncService).kickOff();
    }

    @Test
    public void shouldDoOfflineLoginWhenNoConnectionAndHasSynedData() {
        appInject.setNetworkConnection(false);
        when(userRepository.getUserFromLocal(any(User.class))).thenReturn(new User("user", "password"));
        when(mockActivity.needInitInventory()).thenReturn(false);

        SharedPreferenceMgr.getInstance().setHasGetProducts(true);
        SharedPreferenceMgr.getInstance().setLastMonthStockCardDataSynced(true);
        SharedPreferenceMgr.getInstance().setRequisitionDataSynced(true);

        presenter.startLogin("user", "password");

        verify(userRepository).getUserFromLocal(any(User.class));
        assertThat(UserInfoMgr.getInstance().getUser().getUsername()).isEqualTo("user");

        verify(mockActivity).loaded();
        verify(mockActivity).goToHomePage();
    }

    @Test
    public void shouldShowMessageWhenNoConnectionAndHasNotGetProducts() {
        appInject.setNetworkConnection(false);
        when(userRepository.getUserFromLocal(any(User.class))).thenReturn(new User("user", "password"));
        when(mockActivity.needInitInventory()).thenReturn(false);

        presenter.startLogin("user", "password");

        verify(userRepository).getUserFromLocal(any(User.class));
        assertThat(UserInfoMgr.getInstance().getUser().getUsername()).isEqualTo("user");

        verify(mockActivity).loaded();
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(RuntimeEnvironment.application.getString(R.string.msg_sync_products_list_failed));
    }

    @Test
    public void shouldShowLoginFailErrorMsgWhenNoConnectionAndNoLocalCache() {
        appInject.setNetworkConnection(false);
        when(userRepository.getUserFromLocal(any(User.class))).thenReturn(null);

        presenter.startLogin("user", "password");

        verify(userRepository).getUserFromLocal(any(User.class));

        verify(mockActivity).loaded();
        verify(mockActivity).showInvalidAlert();
        verify(mockActivity).clearPassword();
    }

    @Test
    public void shouldShowUserNameEmptyErrorMessage() {
        presenter.startLogin("", "password1");
        verify(mockActivity).showUserNameEmpty();
    }

    @Test
    public void shouldShowPasswordEmptyErrorMessage() {
        presenter.startLogin("user", "");
        verify(mockActivity).showPasswordEmpty();
    }

    @Test
    public void shouldShowSyncingStockCardsMessage() {
        syncSubscriber.onNext(SyncProgress.SyncingStockCardsLastMonth);
        verify(mockActivity).loading(RuntimeEnvironment.application.getString(R.string.msg_sync_stock_movements_data));
    }

    @Test
    public void shouldGoToInitInventoryWhenRequisitionDataSynced() {
        when(mockActivity.needInitInventory()).thenReturn(true);
        syncSubscriber.onNext(RequisitionSynced);

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

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserRepository.class).toInstance(userRepository);
            bind(SyncService.class).toInstance(syncService);
            bind(SyncDownManager.class).toInstance(syncDownManager);
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
        }
    }
}
