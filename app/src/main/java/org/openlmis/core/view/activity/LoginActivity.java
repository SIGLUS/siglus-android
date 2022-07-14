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

import static org.openlmis.core.utils.Constants.SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.enumeration.LoginErrorType;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.keyboard.KeyboardUtil;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@SuppressWarnings({"squid:S1874", "squid:S110"})
@BindEventBus
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity implements LoginPresenter.LoginView, View.OnClickListener {

  @InjectView(R.id.tx_username)
  public EditText etUsername;

  @InjectView(R.id.tx_password)
  public EditText etPassword;

  @InjectView(R.id.btn_login)
  public Button btnLogin;

  @InjectView(R.id.ly_username)
  public TextInputLayout lyUserName;

  @InjectView(R.id.ly_password)
  public TextInputLayout lyPassword;

  @InjectView(R.id.iv_visibility_pwd)
  public ImageView ivVisibilityPwd;

  @InjectView(R.id.iv_logo)
  public ImageView ivLogo;

  @InjectView(R.id.tv_version)
  public TextView tvVersion;

  @InjectView(R.id.tv_error_alert)
  TextView errorAlert;

  @InjectPresenter(LoginPresenter.class)
  LoginPresenter presenter;

  boolean isPwdVisible = false;

  @Override
  public void sendScreenToGoogleAnalyticsAfterLogin() {
    AnalyticsTracker.getInstance().trackScreen(ScreenName.LOGIN_SCREEN);
  }

  public void goToInitInventory() {
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_basic_products_in_inventory)) {
      startActivity(new Intent(this, BulkInitialInventoryActivity.class));
      finish();
    } else {
      launchActivity(InitialInventoryActivity.getIntentToMe(this, false));
    }
  }

  public void goToHomePage() {
    launchActivity(HomeActivity.getIntentToMe(this));
  }

  public void launchActivity(Intent intent) {
    startActivity(intent);
    finish();
  }

  public void clearErrorAlerts() {
    lyUserName.setErrorEnabled(false);
    lyPassword.setErrorEnabled(false);
  }

  @Override
  public boolean needInitInventory() {
    return !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training) && preferencesMgr
        .isNeedsInventory();
  }

  @Override
  public void showInvalidAlert(LoginErrorType loginErrorType) {
    clearErrorAlerts();
    if (loginErrorType == LoginErrorType.NO_INTERNET) {
      errorAlert.setText(getResources().getText(R.string.message_wipe_no_connection));
      errorAlert.setVisibility(View.VISIBLE);
    }
    if (loginErrorType == LoginErrorType.WRONG_PASSWORD) {
      errorAlert.setVisibility(View.GONE);
      if (etUsername.isEnabled()) {
        lyUserName.setError(" ");
      }
      lyPassword.setError(getResources().getText(R.string.msg_invalid_user));
    }
    if (loginErrorType == LoginErrorType.NON_MOBILE_USER) {
      etUsername.setEnabled(true);
      errorAlert.setVisibility(View.GONE);
      lyUserName.setError(" ");
      lyPassword.setError(getResources().getText(R.string.msg_isAndroid_False));
    }
    if (loginErrorType == LoginErrorType.NON_SAME_DEVICE) {
      etUsername.setEnabled(true);
      errorAlert.setVisibility(View.GONE);
      lyUserName.setError(" ");
      lyPassword.setError(getResources().getText(R.string.msg_is_same_device_false));
    }
    etUsername.getBackground()
        .setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    etPassword.getBackground()
        .setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
  }

  @Override
  public void showPasswordEmpty() {
    clearErrorAlerts();
    errorAlert.setVisibility(View.GONE);
    lyPassword.setError(getResources().getString(R.string.msg_empty_user));
    etPassword.getBackground()
        .setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
  }

  @Override
  public void showUserNameEmpty() {
    clearErrorAlerts();
    errorAlert.setVisibility(View.GONE);
    lyUserName.setError(getResources().getString(R.string.msg_empty_user));
    etUsername.getBackground()
        .setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_login:
        startLogin(false);
        KeyboardUtil.hideKeyboard(btnLogin);
        break;
      case R.id.iv_visibility_pwd:
        setPwdVisibility();
        break;
      default:
        break;
    }
  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveSyncStatusEvent(LoginErrorType loginErrorType) {
    if (loginErrorType.toString().equals(LoginErrorType.NON_MOBILE_USER.toString())) {
      showInvalidAlert(LoginErrorType.NON_MOBILE_USER);
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveSyncStatusEvent(String message) {
    if (SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE.equals(message)) {
      showInvalidAlert(LoginErrorType.NON_SAME_DEVICE);
    }
  }

  public void sendSyncStartBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.START));
  }

  public void sendSyncFinishedBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.FINISH));
  }

  @Override
  public void sendSyncErrorBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.ERROR));
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
        && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
      finish();
      return;
    }

    try {
      initUI();
    } catch (PackageManager.NameNotFoundException e) {
      Log.w("LoginActivity", e);
    }

    restoreFromResync();
  }

  @Override
  protected ScreenName getScreenName() {
    return null;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_NoActionBar;
  }

  private void restoreFromResync() {
    String strUsername = getIntent().getStringExtra(Constants.PARAM_USERNAME);
    String strPassword = getIntent().getStringExtra(Constants.PARAM_PASSWORD);

    if (TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strPassword)) {
      return;
    }

    etUsername.setText(strUsername);
    etPassword.setText(strPassword);
    startLogin(true);
  }

  private void initUI() throws PackageManager.NameNotFoundException {
    String versionNumber = LMISApp.getInstance().getPackageManager()
        .getPackageInfo(LMISApp.getContext().getApplicationContext().getPackageName(), 0).versionName;
    tvVersion.setText(Html.fromHtml(getResources().getString(R.string.version_number, versionNumber)));

    ivVisibilityPwd.setOnClickListener(this);
    btnLogin.setOnClickListener(this);

    String lastLoginUser = SharedPreferenceMgr.getInstance().getLastLoginUser();
    etUsername.setEnabled(true);
    if (StringUtils.isNotBlank(lastLoginUser)) {
      etUsername.setText(lastLoginUser);
      etUsername.setEnabled(false);
      etPassword.requestFocus();
    }

    etPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
    etPassword.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        KeyboardUtil.hideKeyboard(this);
        startLogin(false);
        return true;
      }
      return false;
    });

    if (BuildConfig.DEBUG) {
      setDeveloperMode();
    }
  }

  private void startLogin(boolean fromReSync) {
    presenter.startLogin(etUsername.getText().toString(), etPassword.getText().toString(), fromReSync);
  }

  private void setPwdVisibility() {
    if (isPwdVisible) {
      etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
      ivVisibilityPwd.setImageResource(R.drawable.ic_visibility_off);
      isPwdVisible = false;
    } else {
      etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
      ivVisibilityPwd.setImageResource(R.drawable.ic_visibility);
      isPwdVisible = true;
    }
    etPassword.setSelection(etPassword.getText().length());
  }

  // This code is purely for enable some hacker ways for tester
  private int clickTimes;

  private void setDeveloperMode() {
    clickTimes = 0;
    final int developerTimes = 7;
    ivLogo.setOnClickListener(v -> {
      if (clickTimes == developerTimes) {
        return;
      }
      if (++clickTimes == developerTimes) {
        SharedPreferenceMgr.getInstance().setEnableQaDebug(true);
        ToastUtil.show("Woohoo! You are Cong or Wei now, please test me");
      } else if (clickTimes > 3) {
        ToastUtil.show("Tap it " + (developerTimes - clickTimes) + " times to be Cong or Wei");
      }
    });
  }
}
