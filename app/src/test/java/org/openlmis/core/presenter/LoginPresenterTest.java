package org.openlmis.core.presenter;



import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.Robolectric;

import retrofit.Callback;
import roboguice.RoboGuice;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class LoginPresenterTest {

    UserRepository userRepository;
    LoginActivity mockActivity;
    LoginPresenter presenter;

    @Captor
    private ArgumentCaptor<Callback<UserRepository.UserResponse>> cb;

    @Before
    public void setup() {
        userRepository = mock(UserRepository.class);
        mockActivity = mock(LoginActivity.class);
        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(Robolectric.application).getInstance(LoginPresenter.class);
        presenter.attachView(mockActivity);
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }


    @Test
    public void shouldSaveUserToLocalDBWhenSuccess() throws InterruptedException {
        when(mockActivity.isConnectionAvailable()).thenReturn(true);
        presenter.startLogin("user", "password");

        verify(mockActivity).startLoading();

        verify(userRepository).authorizeUser(any(User.class), cb.capture());
        UserRepository.UserResponse userResponse = userRepository.new UserResponse();
        userResponse.setUserInformation(new User("user", "password"));

        cb.getValue().success(userResponse, null);
        verify(mockActivity).stopLoading();
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void shouldGoToInventoryPageIfFirstLogin() throws InterruptedException {
        when(mockActivity.needInitInventory()).thenReturn(true);
        when(mockActivity.isConnectionAvailable()).thenReturn(true);

        presenter.startLogin("user", "password");
        verify(userRepository).authorizeUser(any(User.class), cb.capture());
        UserRepository.UserResponse userResponse = userRepository.new UserResponse();
        userResponse.setUserInformation(new User("user", "password"));

        cb.getValue().success(userResponse, null);

        verify(mockActivity).goToInitInventory();
    }

    @Test
    public void shouldDoOfflineLoginWhenNoConnection () {
        when(mockActivity.isConnectionAvailable()).thenReturn(false);
        presenter.startLogin("user", "password");
        verify(userRepository).getUserForLocalDatabase(anyString(), anyString());
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserRepository.class).toInstance(userRepository);
        }
    }
}
