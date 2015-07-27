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


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.LoginPresenter;
import org.openlmis.core.presenter.Presenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity{

    @InjectView(R.id.tx_username)
    public EditText userName;

    @InjectView(R.id.tx_password)
    public EditText password;

    @InjectView(R.id.btn_login)
    public Button btnLogin;

    @Inject
    LoginPresenter presenter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    void initUI(){

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startLogin(userName.getText().toString(), password.getText().toString());
            }
        });
    }


    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    public  void goToInitInventory(){
        Intent intent = new Intent();
        intent.setClass(this, InventoryActitivy.class);
        startActivity(intent);
    }

    public void clearPassword(){
        password.setText("");
    }

}
