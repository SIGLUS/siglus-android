package org.openlmis.core.view.activity;


import android.app.Activity;
import android.app.ProgressDialog;
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



}
