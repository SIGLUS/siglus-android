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



import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.service.SyncSubscriber;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.View;


import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginPresenter implements Presenter {

    LoginView view;

    @Inject
    UserRepository userRepository;

    @Inject
    SyncManager syncManager;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) {
        this.view = (LoginView) v;
    }

    public void startLogin(String userName, String password) {

        if (StringUtils.EMPTY.equals(userName.trim())) {
            view.showEmptyAlert(0);
            return;
        }
        if (StringUtils.EMPTY.equals(password)) {
            view.showEmptyAlert(1);
            return;
        }
        view.loading();

        User user = new User(userName.trim(), password);
        if (view.isConnectionAvailable()) {
            authorizeUserRemote(user);
        } else {
            authorizeUserLocal(user);
        }
    }

    private void authorizeUserLocal(User user) {
        User localUser = userRepository.getUserForLocalDatabase(user);

        if (localUser == null) {
            view.showInvalidAlert();
        } else {
            user = localUser;
            UserInfoMgr.getInstance().setUser(user);
            goToNextPage();
        }
        view.loaded();
    }

    private void authorizeUserRemote(final User user) {
        userRepository.authorizeUser(user, new Callback<User>() {
            @Override
            public void success(User remoteUser, Response response) {
                remoteUser.setUsername(user.getUsername());
                remoteUser.setPassword(user.getPassword());
                onLoginSuccess(remoteUser);
            }

            @Override
            public void failure(RetrofitError error) {
                view.loaded();
                onLoginFailed();
            }
        });
    }

    public void saveUserToLocalDatabase(User user) {
        userRepository.save(user);
    }

    public void onLoginSuccess(User user) {
        syncManager.createSyncAccount(user);
        syncManager.kickOff();

        saveUserToLocalDatabase(user);
        UserInfoMgr.getInstance().setUser(user);
        view.clearErrorAlerts();

        getProgramWithProducts();
    }

    public void goToNextPage() {
        if (view.needInitInventory()) {
            view.goToInitInventory();
        } else {
            view.goToHomePage();
        }
    }

    public void onLoginFailed() {
        view.loaded();
        view.showInvalidAlert();
        view.clearPassword();
    }


    public void getProgramWithProducts() {
        if (!view.hasGetProducts()) {
            view.loading();
            syncManager.syncProductsWithProgramAsync(new SyncSubscriber<Void>() {
                @Override
                public void onCompleted() {
                    view.loaded();
                    view.setHasGetProducts(true);
                    goToNextPage();
                }

                @Override
                public void onError(Throwable e) {
                    ToastUtil.show(R.string.msg_user_not_facility);
                    view.loaded();
                }
            });

        } else {
            view.loaded();
            goToNextPage();
        }
    }

    public interface LoginView extends View {

        void clearPassword();

        void goToHomePage();

        void goToInitInventory();

        boolean needInitInventory();

        void showInvalidAlert();

        void showEmptyAlert(int position);

        boolean isConnectionAvailable();

        boolean hasGetProducts();

        void setHasGetProducts(boolean hasGetProducts);

        void clearErrorAlerts();
    }
}
