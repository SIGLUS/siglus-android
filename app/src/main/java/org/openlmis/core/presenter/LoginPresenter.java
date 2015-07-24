package org.openlmis.core.presenter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.view.activity.InventoryActitivy;
import org.openlmis.core.view.activity.LoginActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginPresenter implements  Presenter, Callback<UserRepository.UserResponse>{

    LoginActivity view;
    Context context;

    @Inject
    UserRepository userRepository;


    public void initPresenter(Context context){
        this.context = context;
    }

    @Override
    public void attachIncomingIntent(Intent intent) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(Activity v) {
       this.view = (LoginActivity)v;
    }

    public  void startLogin(String userName, String password){
        if("".equals(userName.trim()) || "".equals(password.trim())){
            view.showMessage(context.getResources().getString(R.string.msg_login_validate));
            return;
        }

        view.startLoading();
        //send login request.
        userRepository.getUser(userName, password, this);
    }

    @Override
    public void success(UserRepository.UserResponse userResponse, Response response) {
        if(userResponse.isAuthenticated()){
            view.showMessage("Login Successfully");
        }else{
            view.showMessage("Login Failed");
        }

        view.startLoading();
    }

    @Override
    public void failure(RetrofitError error) {
        view.showMessage(context.getResources().getString(R.string.msg_login_failed, error.getMessage()));
        view.stopLoading();

        Intent intent = new Intent();
        intent.setClass(context, InventoryActitivy.class);
        context.startActivity(intent);
    }
}
