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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.common.Constants;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.presenter.Presenter;

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
    @Inject
    LoginPresenter presenter;

    public static boolean isActive;

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

        String lastLoginUser = getPreferences().getString(Constants.KEY_LAST_LOGIN_USER, StringUtils.EMPTY);
        if (StringUtils.isNotBlank(lastLoginUser)) {
            userName.setText(lastLoginUser);
            password.requestFocus();
        }
    }


    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    public void goToInitInventory() {
        startActivity(InventoryActivity.getIntentToMe(this));
    }

    public void goToHomePage() {
        startActivity(HomeActivity.class);
    }

    public void startActivity(Class activityName) {
        saveString(Constants.KEY_LAST_LOGIN_USER, userName.getText().toString().trim());
        super.startActivity(activityName);
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
        return getPreferences().getBoolean(Constants.KEY_INIT_INVENTORY, true);
    }

    @Override
    public boolean hasGetProducts() {
        return getPreferences().getBoolean(Constants.KEY_HAS_GET_PRODUCTS, false);
    }

    @Override
    public void setHasGetProducts(boolean hasGetProducts) {
        saveBoolean(Constants.KEY_HAS_GET_PRODUCTS, hasGetProducts);
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

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                presenter.startLogin(userName.getText().toString(), password.getText().toString());
                break;
            case R.id.iv_visibility_pwd:
                setPwdVisibility();
                break;
            default:
                break;
        }
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
