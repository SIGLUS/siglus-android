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


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.common.Constants;
import org.openlmis.core.network.NetworkConnectionManager;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.presenter.Presenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity implements LoginPresenter.LoginView {

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
    @Inject
    LoginPresenter presenter;

    public static boolean isActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    void initUI() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startLogin(userName.getText().toString(), password.getText().toString());
            }
        });

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
        startActivity(InventoryActivity.class);
    }

    public void goToHomePage() {
        startActivity(HomeActivity.class);
    }

    public void startActivity(Class activityName) {
        saveString(Constants.KEY_LAST_LOGIN_USER, userName.getText().toString().trim());
        super.startActivity(activityName, true);
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
    public boolean isConnectionAvailable() {
        return NetworkConnectionManager.isConnectionAvailable(LoginActivity.this);
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
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
