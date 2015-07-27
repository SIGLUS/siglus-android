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

package org.openlmis.core.presenter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.NetworkConnectionManager;
import org.openlmis.core.view.activity.LoginActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginPresenter implements Presenter, Callback<UserRepository.UserResponse> {

    LoginActivity view;
    Context context;

    @Inject
    UserRepository userRepository;


    public void initPresenter(Context context) {
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
        this.view = (LoginActivity) v;
    }

    public void startLogin(String userName, String password) {
        if ("".equals(userName.trim()) || "".equals(password)) {
            view.showMessage(context.getResources().getString(R.string.msg_login_validate));
            return;
        }

        view.startLoading();

        if (NetworkConnectionManager.isConnectionAvaliable(context)) {
            userRepository.getUser(userName.trim(), password, this);
        } else {
            User user = userRepository.getUserForLocalDatabase(userName.trim(), password);

            if (user == null) {
                view.showMessage(context.getResources().getString(R.string.msg_login_failed));
            } else {
                view.goToInitInventory();
            }

            view.stopLoading();
        }
    }


    public void saveToLocalDatabase(User user) {
        userRepository.save(user);
    }


    @Override
    public void success(UserRepository.UserResponse userResponse, Response response) {
        view.showMessage(context.getResources().getString(R.string.msg_login_successful));
        view.stopLoading();

        saveToLocalDatabase(userResponse.getUserProfile());
        view.goToInitInventory();
    }

    @Override
    public void failure(RetrofitError error) {
        view.showMessage(context.getResources().getString(R.string.msg_login_failed, error.getMessage()));
        view.stopLoading();

        view.goToInitInventory();
    }
}
