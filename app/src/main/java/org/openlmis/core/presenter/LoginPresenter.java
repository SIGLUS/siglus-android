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
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NoFacilityForUserException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.repository.UserRepository.NewCallback;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.service.SyncSubscriber;
import org.openlmis.core.utils.FeatureToggle;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.View;

public class LoginPresenter implements Presenter {

    LoginView view;

    boolean isLoadingProducts = false;
    boolean isSyncingRequisitionData = false;

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
            view.showUserNameEmpty();
            return;
        }
        if (StringUtils.EMPTY.equals(password)) {
            view.showPasswordEmpty();
            return;
        }
        view.loading();

        User user = new User(userName.trim(), password);
        if (LMISApp.getInstance().isConnectionAvailable()) {
            authorizeUserRemote(user);
        } else {
            authorizeUserLocal(user);
        }
    }

    private void authorizeUserLocal(User user) {
        User localUser = userRepository.getUserFromLocal(user);

        if (localUser == null) {
            onLoginFailed();
        } else {
            user = localUser;
            UserInfoMgr.getInstance().setUser(user);
            goToNextPage();
        }
    }

    private void authorizeUserRemote(final User user) {
        userRepository.authorizeUser(user, new NewCallback<User>() {
            @Override
            public void success(User remoteUser) {
                remoteUser.setUsername(user.getUsername());
                remoteUser.setPassword(user.getPassword());
                onLoginSuccess(remoteUser);
            }

            @Override
            public void failure(String error) {
                onLoginFailed();
            }

            @Override
            public void timeout(String error) {
                authorizeUserLocal(user);
            }
        });
    }

    private void saveUserToLocalDatabase(User user) {
        userRepository.save(user);
    }

    protected void onLoginSuccess(User user) {
        syncManager.createSyncAccount(user);
        syncManager.kickOff();

        saveUserToLocalDatabase(user);
        UserInfoMgr.getInstance().setUser(user);
        view.clearErrorAlerts();

        serverDataSyncNeeded();
    }

    public void onLoginFailed() {
        view.loaded();
        view.showInvalidAlert();
        view.clearPassword();
    }

    private void serverDataSyncNeeded() {
        if (view.hasGetProducts()) {
            checkRequisitionData();
        } else {
            checkProductsWithProgram();
        }
    }

    private void checkRequisitionData() {
        if (view.isRequisitionDataSynced()) {
            goToNextPage();
        } else {
            syncRequisitionData();
        }
    }

    private void checkProductsWithProgram() {
        if (!isLoadingProducts) {
            isLoadingProducts = true;
            view.loading(LMISApp.getInstance().getString(R.string.msg_fetching_products));
            syncManager.syncProductsWithProgramAsync(new ProductSyncSubscriber());
        }
    }

    protected void goToNextPage() {
        view.loaded();
        if (view.needInitInventory()) {
            view.goToInitInventory();
        } else {
            view.goToHomePage();
        }
    }

    class ProductSyncSubscriber extends SyncSubscriber<Void> {
        @Override
        public void onCompleted() {
            isLoadingProducts = false;
            view.setHasGetProducts(true);
            view.loaded();

            syncRequisitionData();
        }

        @Override
        public void onError(Throwable e) {
            isLoadingProducts = false;
            view.setHasGetProducts(false);
            if (e instanceof NoFacilityForUserException) {
                ToastUtil.show(R.string.msg_user_not_facility);
            } else if (e instanceof LMISException) {
                ToastUtil.show(R.string.msg_save_products_failed);
            } else {
                ToastUtil.show(R.string.msg_sync_products_list_failed);
            }
            view.loaded();
        }
    }

    private void syncRequisitionData() {
        if (!FeatureToggle.isOpen(R.bool.feature_sync_back_rnr_186)){
            goToNextPage();
            return;
        }

        if (!isSyncingRequisitionData) {
            isSyncingRequisitionData = true;
            view.loading(LMISApp.getInstance().getString(R.string.msg_sync_requisition_data));
            syncManager.syncRequisitionData(syncRequisitionDataSubscriber);
        }
    }

    protected SyncSubscriber<Void> syncRequisitionDataSubscriber = new SyncSubscriber<Void>() {
        @Override
        public void onCompleted() {
            isSyncingRequisitionData = false;
            view.setRequisitionDataSynced(true);
            goToNextPage();
        }

        @Override
        public void onError(Throwable throwable) {
            isSyncingRequisitionData = false;
            view.setRequisitionDataSynced(false);
            ToastUtil.show(R.string.msg_sync_data_failed);
            goToNextPage();
        }
    };

    public interface LoginView extends View {

        void clearPassword();

        void goToHomePage();

        void goToInitInventory();

        boolean needInitInventory();

        void showInvalidAlert();

        void showUserNameEmpty();

        void showPasswordEmpty();

        boolean hasGetProducts();

        void setHasGetProducts(boolean hasGetProducts);

        boolean isRequisitionDataSynced();

        void setRequisitionDataSynced(boolean isBackDataSynced);

        void clearErrorAlerts();
    }
}
