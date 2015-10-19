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
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.repository.UserRepository.NewCallback;
import org.openlmis.core.network.model.ProductsResponse;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;
import rx.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class LoginPresenterTest {

    UserRepository userRepository;
    LoginActivity mockActivity;
    LoginPresenter presenter;
    ProductsResponse mockProductsResponse;
    SyncManager syncManager;

    @Captor
    private ArgumentCaptor<NewCallback<User>> loginCB;
    @Captor
    private ArgumentCaptor<Observer<Void>> getProductsCB;

    private LMISTestApp appInject;

    @Before
    public void setup() {

        appInject = (LMISTestApp) RuntimeEnvironment.application;


        userRepository = mock(UserRepository.class);
        mockActivity = mock(LoginActivity.class);
        mockProductsResponse = mock(ProductsResponse.class);
        syncManager = mock(SyncManager.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(LoginPresenter.class);
        presenter.attachView(mockActivity);
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

        when(mockActivity.hasGetProducts()).thenReturn(false);

        presenter.startLogin("user", "password");
        User user = new User("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());

        loginCB.getValue().success(user);

        verify(syncManager).createSyncAccount(user);
        verify(syncManager).kickOff();
    }

    @Test
    public void shouldSaveUserInfoWhenLoginSuccess() {
        appInject.setNetworkConnection(true);

        when(mockActivity.hasGetProducts()).thenReturn(false);

        presenter.startLogin("user", "password");
        User user = new User("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());

        loginCB.getValue().success(user);

        verify(userRepository).save(user);
        assertThat(UserInfoMgr.getInstance().getUser()).isEqualTo(user);
        verify(mockActivity).clearErrorAlerts();
    }

    @Test
    public void shouldGetProductsWhenLoginSuccFromNet() {
        appInject.setNetworkConnection(true);

        when(mockActivity.hasGetProducts()).thenReturn(false);

        presenter.startLogin("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());
        loginCB.getValue().success(new User("user", "password"));

        verify(syncManager).syncProductsWithProgramAsync(getProductsCB.capture());
        getProductsCB.getValue().onCompleted();
    }

    @Test
    public void shouldGoToInventoryPageIfGetProductsSuccess() throws InterruptedException {
        when(mockActivity.needInitInventory()).thenReturn(true);
        appInject.setNetworkConnection(true);
        when(mockActivity.hasGetProducts()).thenReturn(false);

        presenter.startLogin("user", "password");
        verify(userRepository).authorizeUser(any(User.class), loginCB.capture());

        loginCB.getValue().success(new User("user", "password"));

        verify(syncManager).syncProductsWithProgramAsync(getProductsCB.capture());
        getProductsCB.getValue().onCompleted();

        verify(mockActivity).loaded();
        verify(mockActivity).goToInitInventory();
    }

    @Test
    public void shouldDoOfflineLoginWhenNoConnectionAndHasLocalCache() {
        appInject.setNetworkConnection(false);
        when(userRepository.getUserFromLocal(any(User.class))).thenReturn(new User("user", "password"));
        when(mockActivity.needInitInventory()).thenReturn(false);

        presenter.startLogin("user", "password");

        verify(userRepository).getUserFromLocal(any(User.class));
        assertThat(UserInfoMgr.getInstance().getUser().getUsername()).isEqualTo("user");

        verify(mockActivity).loaded();
        verify(mockActivity).goToHomePage();
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
    public void shouldCallGetProductOnlyOnceWhenClickLoginButtonTwice() {
        appInject.setNetworkConnection(true);

        when(mockActivity.hasGetProducts()).thenReturn(false);

        presenter.startLogin("user", "password");
        presenter.startLogin("user", "password");

        verify(userRepository, times(2)).authorizeUser(any(User.class), loginCB.capture());
        loginCB.getValue().success(new User("user", "password"));

        verify(syncManager, times(1)).syncProductsWithProgramAsync(any(Observer.class));
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

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserRepository.class).toInstance(userRepository);
            bind(SyncManager.class).toInstance(syncManager);
        }
    }
}
