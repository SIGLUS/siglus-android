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
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.utils.InjectPresenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity implements LoginPresenter.LoginView, View.OnClickListener {

    @InjectView(R.id.tx_username)
    public EditText userName;
    @InjectView(R.id.tx_password)
    public EditText password;
    @InjectView(R.id.btn_login)
    public Button btnLogin;
    @InjectView(R.id.ly_username)
    public TextInputLayout lyUserName;
    @InjectView(R.id.ly_password)
    public TextInputLayout lyPassword;
    @InjectView(R.id.iv_visibility_pwd)
    public ImageView ivVisibilityPwd;
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

        initUI();
    }

    private void initUI() {
        ivVisibilityPwd.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        String lastLoginUser = getPreferences().getString(SharedPreferenceMgr.KEY_LAST_LOGIN_USER, StringUtils.EMPTY);
        if (StringUtils.isNotBlank(lastLoginUser)) {
            userName.setText(lastLoginUser);
            password.requestFocus();
        }

        password.setImeOptions(EditorInfo.IME_ACTION_DONE);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
    }

    public void goToInitInventory() {
        launchActivity(InventoryActivity.getIntentToMe(this));
        finish();
    }

    public void goToHomePage() {
        launchActivity(HomeActivity.getIntentToMe(this));
        finish();
    }

    public void launchActivity(Intent intent) {
        saveString(SharedPreferenceMgr.KEY_LAST_LOGIN_USER, userName.getText().toString().trim());
        startActivity(intent);
        finish();
    }


    public void clearPassword() {
        password.setText(StringUtils.EMPTY);
    }

    public void clearErrorAlerts() {
        lyUserName.setErrorEnabled(false);
        lyPassword.setErrorEnabled(false);
    }

    @Override
    public boolean needInitInventory() {
        return preferencesMgr.isNeedsInventory();
    }

    @Override
    public void showInvalidAlert() {
        clearErrorAlerts();
        lyUserName.setError(getResources().getString(R.string.msg_invalid_user));
    }

    @Override
    public void showPasswordEmpty() {
        clearErrorAlerts();
        lyPassword.setError(getResources().getString(R.string.msg_empty_user));
    }

    @Override
    public void showUserNameEmpty() {
        clearErrorAlerts();
        lyUserName.setError(getResources().getString(R.string.msg_empty_user));
    }

    private void startLogin() {
        presenter.startLogin(userName.getText().toString(), password.getText().toString());
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
            default:
                break;
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
        if (password.getInputType() == (InputType.TYPE_CLASS_TEXT
                | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)) {
            ivVisibilityPwd.setImageResource(R.drawable.ic_visibility);
            password.setInputType(InputType.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            password.setInputType(InputType.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            ivVisibilityPwd.setImageResource(R.drawable.ic_visibility_off);
        }

        password.setSelection(password.getText().length());
    }

}
