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


import android.util.Log;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.model.repository.UserRepository.NewCallback;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;

import rx.Subscriber;

public class LoginPresenter extends Presenter {

    LoginView view;

    @Inject
    UserRepository userRepository;

    @Inject
    SyncService syncService;

    @Inject
    SyncDownManager syncDownManager;
    private boolean hasGoneToNextPage;

    @Inject
    private ProgramRepository programRepository;

    @Override
    public void attachView(BaseView v) {
        this.view = (LoginView) v;
    }

    public void startLogin(String userName, String password) {
        hasGoneToNextPage = false;
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
            authorizeAndLoginUserRemote(user);
        } else {
            authorizeAndLoginUserLocal(user);
        }
    }

    private void authorizeAndLoginUserLocal(User user) {
        User localUser = userRepository.mapUserFromLocal(user);

        if (localUser == null) {
            onLoginFailed();
            return;
        }

        user = localUser;
        UserInfoMgr.getInstance().setUser(user);

        if (SharedPreferenceMgr.getInstance().getLastSyncProductTime() == null) {
            view.loaded();
            ToastUtil.show(R.string.msg_sync_products_list_failed);
            return;
        }

        if (!SharedPreferenceMgr.getInstance().isLastMonthStockDataSynced()) {
            view.loaded();
            ToastUtil.show(R.string.msg_sync_stockmovement_failed);
            return;
        }
        if (!SharedPreferenceMgr.getInstance().isRequisitionDataSynced()) {
            view.loaded();
            ToastUtil.show(R.string.msg_sync_requisition_failed);
            return;
        }

        goToNextPage();
    }

    private void authorizeAndLoginUserRemote(final User user) {
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
                authorizeAndLoginUserLocal(user);
            }
        });
    }

    private void saveUserDataToLocalDatabase(User user) throws LMISException {
        userRepository.createOrUpdate(user);

        if (user.getFacilitySupportedPrograms() != null) {
            for (String programCode : user.getFacilitySupportedPrograms()) {
                Program program = new Program();
                program.setProgramCode(programCode);
                programRepository.createOrUpdate(program);
            }
        }
    }

    protected void onLoginSuccess(User user) {
        Log.d("Login Presenter", "Log in successful, setting up sync account");
        syncService.createSyncAccount(user);

        try {
            saveUserDataToLocalDatabase(user);
        } catch (LMISException e) {
            e.reportToFabric();
        }
        UserInfoMgr.getInstance().setUser(user);
        view.clearErrorAlerts();

        syncDownManager.syncDownServerData(getSyncSubscriber());
    }

    public void onLoginFailed() {
        view.loaded();
        view.showInvalidAlert();
        view.clearPassword();
    }

    protected Subscriber<SyncProgress> getSyncSubscriber() {
        return new Subscriber<SyncProgress>() {
            @Override
            public void onCompleted() {
                syncService.kickOff();
                tryGoToNextPage();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(SyncProgress progress) {
                switch (progress) {
                    case SyncingProduct:
                    case SyncingStockCardsLastMonth:
                    case SyncingRequisition:
                        view.loading(LMISApp.getInstance().getString(progress.getMessageCode()));
                        break;

                    case ProductSynced:
                    case StockCardsLastMonthSynced:
                        view.loaded();
                        break;

                    case RequisitionSynced:
                        if (!view.needInitInventory()) {
                            ToastUtil.showLongTimeAsOfficialWay(R.string.msg_initial_sync_success);
                        }
                        goToNextPage();
                        break;
                    case SyncingStockCardsLastYear:
                        tryGoToNextPage();
                        break;
                }
            }
        };
    }

    private void tryGoToNextPage() {
        if (!hasGoneToNextPage) {
            goToNextPage();
        }
    }

    private void goToNextPage() {
        view.loaded();

        if (view.needInitInventory()) {
            view.goToInitInventory();
        } else {
            view.goToHomePage();
        }
        hasGoneToNextPage = true;
    }

    public User getLatestUser() {
        return userRepository.getLocalUser();
    }

    public interface LoginView extends BaseView {

        void clearPassword();

        void goToHomePage();

        void goToInitInventory();

        boolean needInitInventory();

        void showInvalidAlert();

        void showUserNameEmpty();

        void showPasswordEmpty();

        void clearErrorAlerts();
    }
}
