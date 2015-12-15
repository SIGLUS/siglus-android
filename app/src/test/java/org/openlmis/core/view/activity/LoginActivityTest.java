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

import android.content.Intent;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowImageView;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class LoginActivityTest {

    private LoginActivity loginActivity;

    private LoginPresenter mockedPresenter;

    @Before
    public void setUp() {
        mockedPresenter = mock(LoginPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LoginPresenter.class).toInstance(mockedPresenter);
            }
        });
        loginActivity = Robolectric.buildActivity(LoginActivity.class).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldLoginWithFilledUsernameAndPassword() {
        loginActivity.userName.setText("superuser");
        loginActivity.password.setText("super@password");

        loginActivity.btnLogin.performClick();

        verify(mockedPresenter).startLogin("superuser", "super@password");
    }

    @Test
    public void shouldFilledLastLoginUser() {
        loginActivity.saveString(SharedPreferenceMgr.KEY_LAST_LOGIN_USER, "superuser");

        LoginActivity secondLoginActivity = Robolectric.buildActivity(LoginActivity.class).create().get();

        assertThat(secondLoginActivity.userName.getText().toString()).isEqualTo("superuser");
    }

    @Test
    public void shouldClearPasswordAfterMethodInvoked() {
        loginActivity.userName.setText("superuser");
        loginActivity.password.setText("password");

        loginActivity.clearPassword();

        assertThat(loginActivity.password.getText().toString()).isEqualTo("");
    }

    @Test
    public void shouldGoToHomePageAfterMethodInvoked() {
        loginActivity.goToHomePage();

        Intent intent = shadowOf(loginActivity).getNextStartedActivity();

        assertThat(intent).isNotNull();
        assertThat(intent.getComponent().getClassName()).isEqualTo(HomeActivity.class.getName());
        assertThat(loginActivity.isFinishing());
    }

    @Test
    public void shouldGoToInitInventoryAfterMethodInvoked() {
        loginActivity.goToInitInventory();

        Intent intent = shadowOf(loginActivity).getNextStartedActivity();

        assertThat(intent).isNotNull();
        assertThat(intent.getComponent().getClassName()).isEqualTo(InventoryActivity.class.getName());
        assertThat(loginActivity.isFinishing());
    }

    @Test
    public void shouldShowInvalidAlertAfterMethodInvoked() {
        loginActivity.showInvalidAlert();

        String invalidUserMessage = loginActivity.getResources().getString(R.string.msg_invalid_user);

        TextView usernameErrorView = RobolectricUtils.getErrorTextView(loginActivity.lyUserName);
        TextView passwordErrorView = RobolectricUtils.getErrorTextView(loginActivity.lyPassword);

        assertThat(usernameErrorView).isNotNull();
        assertThat(usernameErrorView.getText().toString()).isEqualTo(invalidUserMessage);
        assertThat(passwordErrorView).isNull();
    }

    @Test
    public void shouldShowUsernameEmptyMessageAfterMethodInvoked() {
        loginActivity.showUserNameEmpty();

        String emptyErrorMessage = loginActivity.getResources().getString(R.string.msg_empty_user);

        TextView usernameErrorView = RobolectricUtils.getErrorTextView(loginActivity.lyUserName);
        TextView passwordErrorView = RobolectricUtils.getErrorTextView(loginActivity.lyPassword);

        assertThat(usernameErrorView).isNotNull();
        assertThat(usernameErrorView.getText().toString()).isEqualTo(emptyErrorMessage);
        assertThat(passwordErrorView).isNull();
    }

    @Test
    public void shouldShowPasswordEmptyMessageAfterMethodInvoked() {
        loginActivity.showPasswordEmpty();

        String emptyErrorMessage = loginActivity.getResources().getString(R.string.msg_empty_user);

        TextView usernameErrorView = RobolectricUtils.getErrorTextView(loginActivity.lyUserName);
        TextView passwordErrorView = RobolectricUtils.getErrorTextView(loginActivity.lyPassword);

        assertThat(usernameErrorView).isNull();
        assertThat(passwordErrorView).isNotNull();
        assertThat(passwordErrorView.getText().toString()).isEqualTo(emptyErrorMessage);
    }

    @Test
    public void shouldSetHasGetProducts() {
        loginActivity.saveBoolean(SharedPreferenceMgr.KEY_HAS_GET_PRODUCTS, false);

        loginActivity.setHasGetProducts(true);

        assertThat(loginActivity.getPreferences().getBoolean(SharedPreferenceMgr.KEY_HAS_GET_PRODUCTS, false)).isTrue();
    }

    @Test
    public void shouldClearErrorAlertsAfterMethodInvoked() {
        loginActivity.showInvalidAlert();
        loginActivity.clearErrorAlerts();

        assertThat(RobolectricUtils.getErrorTextView(loginActivity.lyUserName)).isNull();
        assertThat(RobolectricUtils.getErrorTextView(loginActivity.lyPassword)).isNull();

        loginActivity.showPasswordEmpty();
        loginActivity.clearErrorAlerts();

        assertThat(RobolectricUtils.getErrorTextView(loginActivity.lyUserName)).isNull();
        assertThat(RobolectricUtils.getErrorTextView(loginActivity.lyPassword)).isNull();
    }

    @Test
    public void shouldSetPasswordVisibility() {
        ShadowImageView shadowPwdImageView = shadowOf(loginActivity.ivVisibilityPwd);

        assertThat(shadowPwdImageView.getImageResourceId()).isEqualTo(R.drawable.ic_visibility_off);
        assertThat(loginActivity.password.getInputType()).isEqualTo(InputType.TYPE_CLASS_TEXT
                | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);

        loginActivity.ivVisibilityPwd.performClick();

        assertThat(shadowPwdImageView.getImageResourceId()).isEqualTo(R.drawable.ic_visibility);
        assertThat(loginActivity.password.getInputType()).isEqualTo(InputType.TYPE_CLASS_TEXT
                | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }
}