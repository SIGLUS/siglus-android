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
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.persistence.ExportSqliteOpenHelper;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.FileUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;

import java.io.File;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity implements LoginPresenter.LoginView, View.OnClickListener {
    @InjectView(R.id.tx_username)
    public EditText etUsername;

    @InjectView(R.id.tx_password)
    public EditText etPassword;

    @InjectView(R.id.btn_login)
    public Button btnLogin;
    @InjectView(R.id.btn_export_data)
    public Button btnExport;

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

    @InjectPresenter(LoginPresenter.class)
    LoginPresenter presenter;

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
            e.printStackTrace();
        }

        restoreFromResync();
    }

    private void restoreFromResync() {
        String strUsername = getIntent().getStringExtra(Constants.PARAM_USERNAME);
        String strPassword = getIntent().getStringExtra(Constants.PARAM_PASSWORD);

        if (TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strPassword)) {
            return;
        }

        etUsername.setText(strUsername);
        etPassword.setText(strPassword);
        startLogin();
    }

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    public void sendScreenToGoogleAnalyticsAfterLogin() {
        LMISApp.getInstance().trackScreen(ScreenName.LoginScreen);
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_NoActionBar;
    }

    private void initUI() throws PackageManager.NameNotFoundException {
        String versionNumber = LMISApp.getInstance().getPackageManager().getPackageInfo(LMISApp.getContext().getApplicationContext().getPackageName(), 0).versionName;
        tvVersion.setText(Html.fromHtml(getResources().getString(R.string.version_number, versionNumber)));

        ivVisibilityPwd.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnExport.setOnClickListener(this);

        String lastLoginUser = SharedPreferenceMgr.getInstance().getLastLoginUser();
        etUsername.setEnabled(true);
        if (StringUtils.isNotBlank(lastLoginUser)) {
            etUsername.setText(lastLoginUser);
            etUsername.setEnabled(false);
            etPassword.requestFocus();
        }

        etPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideImm();
                    startLogin();
                    return true;
                }
                return false;
            }
        });

        if (BuildConfig.DEBUG) {
            setDeveloperMode();
        }
    }

    public void goToInitInventory() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_basic_products_in_inventory)) {
            startActivity(new Intent(this, BulkInitialInventoryActivity.class));
            finish();
        } else {
            launchActivity(InitialInventoryActivity.getIntentToMe(this));
        }
    }

    public void goToHomePage() {
        launchActivity(HomeActivity.getIntentToMe(this));
    }

    public void launchActivity(Intent intent) {
        startActivity(intent);
        finish();
    }

    public void clearPassword() {
        etPassword.setText(StringUtils.EMPTY);
    }

    public void clearErrorAlerts() {
        lyUserName.setErrorEnabled(false);
        lyPassword.setErrorEnabled(false);
    }

    @Override
    public boolean needInitInventory() {
        return !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training) && preferencesMgr.isNeedsInventory();
    }

    @Override
    public void showInvalidAlert() {
        clearErrorAlerts();
        lyUserName.setError(getResources().getString(R.string.msg_invalid_user));
        etUsername.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
        etPassword.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void showPasswordEmpty() {
        clearErrorAlerts();
        lyPassword.setError(getResources().getString(R.string.msg_empty_user));
        etPassword.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void showUserNameEmpty() {
        clearErrorAlerts();
        lyUserName.setError(getResources().getString(R.string.msg_empty_user));
        etUsername.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    private void startLogin() {
        presenter.startLogin(etUsername.getText().toString(), etPassword.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                startLogin();
                break;
            case R.id.iv_visibility_pwd:
                setPwdVisibility();
                break;
            case R.id.btn_export_data:
                exportDB();
            default:
                break;
        }
    }


    private void exportDB() {
        File currentDB = new File(Environment.getDataDirectory(), "//data//" + LMISApp.getContext().getApplicationContext().getPackageName() + "//databases//lmis_db");
        File currentXML = new File(Environment.getDataDirectory(), "//data//" + LMISApp.getContext().getApplicationContext().getPackageName() + "//shared_prefs//LMISPreference.xml");
        File tempBackup = new File(Environment.getDataDirectory(), "//data//" + LMISApp.getContext().getApplicationContext().getPackageName() + "//databases//lmis_copy");
        File externalBackup = new File(Environment.getExternalStorageDirectory(), "lmis_backup");
        File externalBackupXML = new File(Environment.getExternalStorageDirectory(), "LMISPreference");
        try {
            FileUtil.copy(currentDB, tempBackup);
            ExportSqliteOpenHelper.removePrivateUserInfo(this);
            FileUtil.copy(tempBackup, externalBackup);
            FileUtil.copy(currentXML, externalBackupXML);
            ToastUtil.show(Html.fromHtml(getString(R.string.msg_export_data_success, externalBackup.getPath())));
        } catch (Exception e) {
            new LMISException(e).reportToFabric();
            ToastUtil.show(e.getMessage());
        } finally {
            if (tempBackup.canRead()) {
                tempBackup.delete();
            }
        }

    }
    @Override
    public void loaded() {
        super.loaded();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void setPwdVisibility() {
        if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT
                | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)) {
            ivVisibilityPwd.setImageResource(R.drawable.ic_visibility);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            ivVisibilityPwd.setImageResource(R.drawable.ic_visibility_off);
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    // This code is purely for enable some hacker ways for tester
    private static int clickTimes;

    private void setDeveloperMode() {
        clickTimes = 0;
        final int developerTimes = 7;
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickTimes == developerTimes) {
                    return;
                }
                if (++clickTimes == developerTimes) {
                    SharedPreferenceMgr.getInstance().setEnableQaDebug(true);
                    ToastUtil.show("Woohoo! You are Cong or Wei now, please test me");
                } else if (clickTimes > 3) {
                    ToastUtil.show("Tap it " + (developerTimes - clickTimes) + " times to be Cong or Wei");
                }
            }
        });
    }

    public void sendSyncStartBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_START_SYNC_DATA);
        sendBroadcast(intent);
    }

    public void sendSyncFinishedBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
        sendBroadcast(intent);
    }

    @Override
    public void sendSyncErrorBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_ERROR_SYNC_DATA);
        sendBroadcast(intent);
    }
}
