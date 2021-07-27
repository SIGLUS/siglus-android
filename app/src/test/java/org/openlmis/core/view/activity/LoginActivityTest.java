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

package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.enums.LoginErrorType;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class LoginActivityTest {

  private LoginActivity loginActivity;

  private LoginPresenter mockedPresenter;
  private ActivityController<LoginActivity> activityController;

  @Before
  public void setUp() {
    mockedPresenter = mock(LoginPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(LoginPresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(LoginActivity.class);
    loginActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldLoginWithFilledUsernameAndPassword() {
    loginActivity.etUsername.setText("superuser");
    loginActivity.etPassword.setText("super@password");

    loginActivity.btnLogin.performClick();

    verify(mockedPresenter).startLogin("superuser", "super@password", false);
  }

  @Test
  public void shouldFilledLastLoginUser() {
    loginActivity.saveString(SharedPreferenceMgr.KEY_LAST_LOGIN_USER, "superuser");

    LoginActivity secondLoginActivity = Robolectric.buildActivity(LoginActivity.class).create()
        .get();

    assertThat(secondLoginActivity.etUsername.getText().toString()).hasToString("superuser");
  }

  @Test
  public void shouldGoToHomePageAfterMethodInvoked() {
    loginActivity.goToHomePage();

    Intent intent = shadowOf(loginActivity).getNextStartedActivity();

    assertThat(intent).isNotNull();
    assertThat(intent.getComponent().getClassName()).isEqualTo(HomeActivity.class.getName());
    assertThat(loginActivity.isFinishing()).isTrue();
  }

  @Test
  public void shouldGoToInitInventoryAfterMethodInvoked() {
    loginActivity.goToInitInventory();

    Intent intent = shadowOf(loginActivity).getNextStartedActivity();

    assertThat(intent).isNotNull();
    assertThat(intent.getComponent().getClassName())
        .isEqualTo(InitialInventoryActivity.class.getName());
    assertThat(loginActivity.isFinishing()).isTrue();
  }

  @Test
  public void shouldShowInvalidAlertAfterMethodInvoked() {
    loginActivity.showInvalidAlert(LoginErrorType.WRONG_PASSWORD);

    String invalidUserMessage = loginActivity.getResources().getString(R.string.msg_invalid_user);

    String usernameErrorText = RobolectricUtils.getErrorText(loginActivity.lyUserName);
    String passwordErrorText = RobolectricUtils.getErrorText(loginActivity.lyPassword);

    assertEquals(invalidUserMessage, passwordErrorText);
    assertEquals(" ", usernameErrorText);
  }

  @Test
  public void shouldShowUsernameEmptyMessageAfterMethodInvoked() {
    loginActivity.showUserNameEmpty();

    String emptyErrorMessage = loginActivity.getResources().getString(R.string.msg_empty_user);

    String usernameErrorText = RobolectricUtils.getErrorText(loginActivity.lyUserName);
    String passwordErrorText = RobolectricUtils.getErrorText(loginActivity.lyPassword);

    assertThat(usernameErrorText).isNotNull().isEqualTo(emptyErrorMessage);
    assertThat(passwordErrorText).isNull();
  }

  @Test
  public void shouldShowPasswordEmptyMessageAfterMethodInvoked() {
    loginActivity.showPasswordEmpty();

    String emptyErrorMessage = loginActivity.getResources().getString(R.string.msg_empty_user);

    String usernameErrorText = RobolectricUtils.getErrorText(loginActivity.lyUserName);
    String passwordErrorText = RobolectricUtils.getErrorText(loginActivity.lyPassword);

    assertThat(usernameErrorText).isNull();
    assertThat(passwordErrorText).isNotNull().isEqualTo(emptyErrorMessage);
  }

  @Test
  public void shouldClearErrorAlertsAfterMethodInvoked() {
    loginActivity.showInvalidAlert(LoginErrorType.WRONG_PASSWORD);
    loginActivity.clearErrorAlerts();

    assertThat(RobolectricUtils.getErrorText(loginActivity.lyUserName)).isNull();
    assertThat(RobolectricUtils.getErrorText(loginActivity.lyPassword)).isNull();

    loginActivity.showPasswordEmpty();
    loginActivity.clearErrorAlerts();

    assertThat(RobolectricUtils.getErrorText(loginActivity.lyUserName)).isNull();
    assertThat(RobolectricUtils.getErrorText(loginActivity.lyPassword)).isNull();
  }

}