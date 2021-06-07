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

import org.junit.Before;
import org.junit.Ignore;
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
import org.openlmis.core.model.Program;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.UserBuilder;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManagerMock;
import org.openlmis.core.network.model.UserResponse;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncDownManagerIT;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.JsonFileReader;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import roboguice.RoboGuice;
import rx.Subscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.service.SyncDownManager.SyncProgress.ShouldGoToInitialInventory;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


@RunWith(LMISTestRunner.class)
public class LoginPresenterTest {

    UserRepository userRepository;
    RnrFormRepository rnrFormRepository;
    LoginActivity mockActivity;
    LoginPresenter presenter;
    SyncService syncService;

    @Captor
    private ArgumentCaptor<Callback<UserResponse>> loginCB;
    @Captor
    private ArgumentCaptor<InternetCheck.Callback> internetCheckCallBack;

    private LMISTestApp appInject;
    private Subscriber<SyncProgress> syncSubscriber;
    private SyncDownManager syncDownManager;
    private ProgramRepository programRepository;
    private LMISRestApi mockedApi;
    private Response retrofitResponse;
    private UserResponse userResponse;
    private InternetCheck internetCheck1;

    @Before
    public void setup() {
        appInject = (LMISTestApp) RuntimeEnvironment.application;

        userRepository = mock(UserRepository.class);
        programRepository = mock(ProgramRepository.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        mockActivity = mock(LoginActivity.class);
        syncService = mock(SyncService.class);
        syncDownManager = mock(SyncDownManager.class);

        mockedApi = mock(LMISRestApi.class);
        internetCheck1 = mock(InternetCheck.class);
        appInject.setRestApi(mockedApi);


        retrofitResponse = LMISRestManagerMock.createDummyJsonResponse("http://unknown.com", 200, "", "");
        userResponse = new UserResponse();
        userResponse.setAccess_token("e771dc4c-3df0-40be-a963-f5d4d1a201a6");
        userResponse.setExpires_in(43199);
        userResponse.setToken_type("bearer");
        userResponse.setReferenceDataUserId("eaed4b29-0ece-457f-b64f-5d49a929d13d");
        userResponse.setUsername("CS_Role1");


        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(LoginPresenter.class);
        presenter.attachView(mockActivity);
        syncSubscriber = presenter.getSyncSubscriber();
        presenter = spy(presenter);
    }

    @Test
    public void shouldSaveUserToLocalDBWhenSuccess() {
        presenter.startLogin("username", "password", false);
        verify(internetCheck1).execute(internetCheckCallBack.capture());
        internetCheckCallBack.getValue().launchResponse(true);
        verify(mockActivity).loading();
        verify(mockedApi).authorizeUser(eq("password"),eq("username"),eq("password"), loginCB.capture());

        loginCB.getValue().success(userResponse, retrofitResponse);

        verify(userRepository).createOrUpdate(any(User.class));
    }

    @Ignore
    public void shouldSaveUserSupportedProgramsToLocalDBWhenSuccess() {
        presenter.startLogin("user", "password", false);
        verify(internetCheck1).execute(internetCheckCallBack.capture());
        internetCheckCallBack.getValue().launchResponse(true);
        verify(mockActivity).loading();

        verify(mockedApi).authorizeUser(eq("password"),eq("username"),eq("password"), loginCB.capture());

        List<Program> supportedPrograms = newArrayList(new ProgramBuilder().build(), new ProgramBuilder().build());

//        userResponse.setFacilitySupportedPrograms(supportedPrograms);
        loginCB.getValue().success(userResponse, retrofitResponse);

        verify(userRepository).createOrUpdate(any(User.class));
    }

    @Test
    public void shouldCreateSyncAccountWhenLoginSuccess() {
        // Given
        User user = User
                .builder()
                .username(userResponse.getUsername())
                .password("password1")
                .accessToken(userResponse.getAccess_token())
                .tokenType(userResponse.getToken_type())
                .referenceDataUserId(userResponse.getReferenceDataUserId())
                .isTokenExpired(false)
                .build();

        // When
        presenter.startLogin("CS_Role1", "password1", false);
        // Then
        verify(internetCheck1).execute(internetCheckCallBack.capture());

        // When
        internetCheckCallBack.getValue().launchResponse(true);
        // Then
        verify(mockedApi).authorizeUser(eq("password"),eq("CS_Role1"),eq("password1"), loginCB.capture());

        // When
        loginCB.getValue().success(userResponse, retrofitResponse);
        // Then
        verify(syncService).createSyncAccount(user);
    }

    @Test
    public void shouldSaveUserInfoWhenLoginSuccess() {
        // Given
        User user = User
                .builder()
                .username(userResponse.getUsername())
                .password("password1")
                .accessToken(userResponse.getAccess_token())
                .tokenType(userResponse.getToken_type())
                .referenceDataUserId(userResponse.getReferenceDataUserId())
                .isTokenExpired(false)
                .build();

        // When
        presenter.startLogin("CS_Role1", "password1", false);
        // Then
        verify(internetCheck1).execute(internetCheckCallBack.capture());

        // When
        internetCheckCallBack.getValue().launchResponse(true);
        // Then
        verify(mockedApi).authorizeUser(eq("password"),eq("CS_Role1"),eq("password1"), loginCB.capture());

        // When
        loginCB.getValue().success(userResponse, retrofitResponse);
        // Then
        verify(userRepository).createOrUpdate(user);
        assertThat(UserInfoMgr.getInstance().getUser()).isEqualTo(user);
        verify(mockActivity).clearErrorAlerts();
    }

    @Test
    public void shouldSyncServerDataWhenLoginSuccessFromNet() {
        presenter.startLogin("username", "password", false);
        verify(internetCheck1).execute(internetCheckCallBack.capture());
        internetCheckCallBack.getValue().launchResponse(true);
        verify(mockedApi).authorizeUser(eq("password"),eq("username"),eq("password"), loginCB.capture());
        loginCB.getValue().success(userResponse, retrofitResponse);

        verify(syncDownManager).syncDownServerData(any(Subscriber.class));
    }

    @Test
    public void shouldGoToInventoryPageAndKickOffPeriodicSyncIfSyncServerDataSuccess() {
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
    public void shouldDoOfflineLoginWhenNoConnectionAndHasSyncedData() {
        when(userRepository.mapUserFromLocal(any(User.class))).thenReturn(new User("user", "password"));
        when(mockActivity.needInitInventory()).thenReturn(false);

        SharedPreferenceMgr.getInstance().setLastMonthStockCardDataSynced(true);
        SharedPreferenceMgr.getInstance().setRequisitionDataSynced(true);
        SharedPreferenceMgr.getInstance().setLastSyncProductTime("time");

        presenter.startLogin("user", "password", false);

        verify(internetCheck1).execute(internetCheckCallBack.capture());
        internetCheckCallBack.getValue().launchResponse(false);
        verify(userRepository).mapUserFromLocal(any(User.class));
        assertThat(UserInfoMgr.getInstance().getUser().getUsername()).isEqualTo("user");

    }

    @Test
    public void shouldShowMessageWhenNoConnectionAndHasNotGetProducts() {

        when(userRepository.mapUserFromLocal(any(User.class))).thenReturn(new User("user", "password"));
        when(mockActivity.needInitInventory()).thenReturn(false);

        presenter.startLogin("user", "password", false);
        verify(internetCheck1).execute(internetCheckCallBack.capture());
        internetCheckCallBack.getValue().launchResponse(false);
        verify(userRepository).mapUserFromLocal(any(User.class));
        assertThat(UserInfoMgr.getInstance().getUser().getUsername()).isEqualTo("user");

    }

    @Test
    public void shouldShowLoginFailErrorMsgWhenNoConnectionAndNoLocalCache() {
        when(userRepository.mapUserFromLocal(any(User.class))).thenReturn(null);

        presenter.startLogin("user", "password", false);
        InternetCheck internetCheck = mock(InternetCheck.class);
        verify(internetCheck1).execute(internetCheckCallBack.capture());
        verify(internetCheck1).execute(internetCheckCallBack.capture());
        internetCheckCallBack.getValue().launchResponse(false);
        verify(userRepository).mapUserFromLocal(any(User.class));

        verify(mockActivity).loaded();
        verify(mockActivity).showInvalidAlert();
        verify(mockActivity).clearPassword();
    }

    @Test
    public void shouldShowUserNameEmptyErrorMessage() {
        presenter.startLogin("", "password1", false);
        verify(mockActivity).showUserNameEmpty();
    }

    @Test
    public void shouldShowPasswordEmptyErrorMessage() {
        presenter.startLogin("user", "", false);
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
        syncSubscriber.onNext(ShouldGoToInitialInventory);

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

    @Ignore
    public void shouldSaveUserDataAndSupportedFacilityCodeToDBWhenMultipleProgramToggleON() throws Exception {
        User user = UserBuilder.defaultUser();
        UserResponse userResponse = new UserResponse();
//        userResponse.setUserInformation(user);
//        userResponse.setFacilitySupportedPrograms(Arrays.asList(
//                new ProgramBuilder().setProgramCode("via_code").setProgramName("VIA name").build(),
//                new ProgramBuilder().setProgramCode("mmia_code").setProgramName("MMIA name").build(),
//                new ProgramBuilder().setProgramCode("nutrition_code").setProgramName("Nutrition name").setParentCode("via_code").build()
//        ));

//        presenter.saveUserDataToLocalDatabase(userResponse);

        verify(userRepository).createOrUpdate(user);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserRepository.class).toInstance(userRepository);
            bind(SyncService.class).toInstance(syncService);
            bind(SyncDownManager.class).toInstance(syncDownManager);
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(ProgramRepository.class).toInstance(programRepository);
            bind(InternetCheck.class).toInstance(internetCheck1);
        }
    }
}
