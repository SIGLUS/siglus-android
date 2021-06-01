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

import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NetWorkException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.model.UserResponse;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncDownManager.SyncLocalUserProgress;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.training.TrainingEnvironmentHelper;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;


import java.util.List;


import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginPresenter extends Presenter {

    private static final String TAG = LoginPresenter.class.getSimpleName();

    LoginView view;


    @Inject
    LotRepository lotRepository;

    @Inject
    ProgramDataFormRepository programDataFormRepository;

    @Inject
    StockRepository stockRepository;

    @Inject
    ReportTypeFormRepository reportTypeFormRepository;

    @Inject
    SyncStockCardsLastYearSilently syncStockCardsLastYearSilently;

    @Inject
    UserRepository userRepository;

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    SyncService syncService;

    @Inject
    SyncDownManager syncDownManager;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    private boolean hasGoneToNextPage;

    @Inject
    private ProgramRepository programRepository;

    @Inject
    private DirtyDataRepository dirtyDataRepository;

    @Inject
    private DirtyDataManager dirtyDataManager;

    @Inject
    InternetCheck internetCheck;

    @Override
    public void attachView(BaseView v) {
        this.view = (LoginView) v;
    }

    public void startLogin(String userName, String password, boolean fromReSync) {
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
        if (!isRoboUniTest()) {
            new InternetCheck().execute(checkNetworkConnected(user, fromReSync));
        } else {
            internetCheck.execute(checkNetworkConnected(user, fromReSync));
        }
    }

    private boolean isRoboUniTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }

    private InternetCheck.Callback checkNetworkConnected(User user, boolean fromReSync) {
        return internet -> {
            if (internet && !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
                authorizeAndLoginUserRemote(user, fromReSync);
            } else {
                authorizeAndLoginUserLocal(user);
            }
        };
    }


    private void authorizeAndLoginUserLocal(User user) {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
//            if (userRepository.getLocalUser() == null) {
                TrainingEnvironmentHelper.getInstance().setUpData();
//            }
        }
        setDefaultReportType();
        User localUser = userRepository.mapUserFromLocal(user);

        if (localUser == null) {
            onLoginFailed();
            return;
        }

        UserInfoMgr.getInstance().setUser(localUser);
        syncLocalUserData(getSyncLocalUserDataSubscriber());
    }

    private void setDefaultReportType() {
        if (SharedPreferenceMgr.getInstance().getReportTypesData() == null) {
            try {
                List<ReportTypeForm> reportTypeForms = reportTypeFormRepository.listAll();
                SharedPreferenceMgr.getInstance().setReportTypesData(reportTypeForms);
            } catch (LMISException e) {
                new LMISException(e, "setDefaultReportType").reportToFabric();
            }
        }
    }

    private void authorizeAndLoginUserRemote(final User user, final boolean fromReSync) {
        String grantType = "password";
        LMISApp.getInstance().getRestApi().authorizeUser(grantType, user.getUsername(),user.getPassword(), new Callback<UserResponse>() {
            @Override
            public void success(UserResponse userResponse, Response response) {
                if (userResponse == null || userResponse.getAccess_token() == null) {
                    onLoginFailed();
                } else {
                    user.setAccess_token(userResponse.getAccess_token());
                    user.setToken_type(userResponse.getToken_type());

                    onLoginSuccess(user, fromReSync);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getCause() instanceof NetWorkException) {
                    authorizeAndLoginUserLocal(user);
                } else {
                    onLoginFailed();
                }
            }
        });
    }

    protected void saveUserDataToLocalDatabase(final User user) throws LMISException {
        userRepository.createOrUpdate(user);
    }

    private void onLoginSuccess(final User user, final boolean fromReSync) {
        Log.d(TAG, "Log in successful, setting up sync account");
        syncService.createSyncAccount(user);

        UserInfoMgr.getInstance().setUser(user);
        view.clearErrorAlerts();

        syncDownManager.syncDownServerData(getSyncSubscriber());

        view.sendScreenToGoogleAnalyticsAfterLogin();
        archiveOldData();

        try {
            saveUserDataToLocalDatabase(user);
        } catch (LMISException e) {
            e.printStackTrace();
        }

        if (fromReSync) {
            Observable.create(subscriber -> {
                try {
                    LMISApp.getInstance().getRestApi().recordReSyncAction();
                } catch (LMISException e) {
                    e.printStackTrace();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(System.out::println, Throwable::printStackTrace);
        }
    }

    private void archiveOldData() {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_archive_old_data)) {
            return;
        }

        Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
            if (stockRepository.hasOldDate()) {
                stockRepository.deleteOldData();
                SharedPreferenceMgr.getInstance().setHasDeletedOldStockMovement(true);
            }
            if (rnrFormRepository.hasOldDate()) {
                rnrFormRepository.deleteOldData();
                SharedPreferenceMgr.getInstance().setHasDeletedOldRnr(true);
            }
            if (dirtyDataRepository.hasOldDate()) {
                dirtyDataRepository.deleteOldData();
            }
            if (programDataFormRepository.hasOldDate()){
                programDataFormRepository.deleteOldData();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(System.out::println, Throwable::printStackTrace);

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
                    case SyncingPrograms:
                    case SyncingServiceList:
                    case SyncingReportType:
                    case SyncingProduct:
                    case SyncingStockCardsLastMonth:
                    case SyncingRequisition:
                        view.loading(LMISApp.getInstance().getString(progress.getMessageCode()));
                        break;

                    case ProductSynced:
                        break;
                    case StockCardsLastMonthSynced:
                        syncStockCards();
                        break;

                    case ShouldGoToInitialInventory:
                        if (!view.needInitInventory()) {
                            ToastUtil.showForLongTime(R.string.msg_initial_sync_success);
                        }
                        goToNextPage();
                        break;
                }
            }
        };
    }

    private void syncStockCards() {
        if (sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
            sharedPreferenceMgr.setIsSyncingLastYearStockCards(true);
            view.sendSyncStartBroadcast();
            syncStockCardsLastYearSilently.performSync().subscribe(getSyncLastYearStockCardSubscriber());
        } else {
            view.sendSyncFinishedBroadcast();
            sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        }
    }

    @NonNull
    private Subscriber<List<StockCard>> getSyncLastYearStockCardSubscriber() {
        return new Subscriber<List<StockCard>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "getSyncLastYearStockCardSubscriber onCompleted");
                try {
                    syncDownManager.fetchKitChangeProduct();
                } catch (LMISException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
                sharedPreferenceMgr.setStockCardLastYearSyncError(true);
                view.sendSyncErrorBroadcast();
                sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                new LMISException(e).reportToFabric();
            }

            @Override
            public void onNext(List<StockCard> stockCards) {
                syncDownManager.saveStockCardsFromLastYear(stockCards).subscribe(getSaveStockCardsSubscriber());
            }
        };
    }

    public void syncLocalUserData(Subscriber<SyncLocalUserProgress> subscriber) {
        Observable.create((Observable.OnSubscribe<SyncLocalUserProgress>) subscriber1 -> {
            if (SharedPreferenceMgr.getInstance().getLastSyncProductTime() == null) {
                subscriber1.onNext(SyncLocalUserProgress.SyncLastSyncProductFail);
                return;
            }

            if (!SharedPreferenceMgr.getInstance().isLastMonthStockDataSynced()) {
                subscriber1.onNext(SyncLocalUserProgress.SyncLastMonthStockDataFail);
                return;
            }
            if (!SharedPreferenceMgr.getInstance().isRequisitionDataSynced()) {
                subscriber1.onNext(SyncLocalUserProgress.SyncRequisitionDataFail);
                return;
            }
            subscriber1.onNext(SyncLocalUserProgress.SyncLastDataSuccess);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

    @NonNull
    public Subscriber<SyncLocalUserProgress> getSyncLocalUserDataSubscriber() {
        return new Subscriber<SyncLocalUserProgress>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(SyncLocalUserProgress progress) {
                switch (progress) {
                    case SyncLastSyncProductFail:
                        view.loaded();
                        ToastUtil.show(R.string.msg_sync_products_list_failed);
                        break;
                    case SyncLastMonthStockDataFail:
                        view.loaded();
                        ToastUtil.show(R.string.msg_sync_stock_movement_failed);
                        break;
                    case SyncRequisitionDataFail:
                        view.loaded();
                        ToastUtil.show(R.string.msg_sync_requisition_failed);
                        break;
                    case SyncLastDataSuccess:
                        dirtyDataManager.initialDirtyDataCheck();
                        goToNextPage();
                        break;
                }
            }
        };
    }

    @NonNull
    private Subscriber<Void> getSaveStockCardsSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(false);
                sharedPreferenceMgr.setStockCardLastYearSyncError(false);
                sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                sharedPreferenceMgr.setStockLastSyncTime();
                dirtyDataManager.initialDirtyDataCheck();
                view.sendSyncFinishedBroadcast();
            }

            @Override
            public void onError(Throwable e) {
                sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
                sharedPreferenceMgr.setStockCardLastYearSyncError(true);
                sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
                view.sendSyncErrorBroadcast();
            }

            @Override
            public void onNext(Void aVoid) {

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

    public interface LoginView extends BaseView {

        void clearPassword();

        void goToHomePage();

        void goToInitInventory();

        boolean needInitInventory();

        void showInvalidAlert();

        void showUserNameEmpty();

        void showPasswordEmpty();

        void clearErrorAlerts();

        void sendScreenToGoogleAnalyticsAfterLogin();

        void sendSyncStartBroadcast();

        void sendSyncFinishedBroadcast();

        void sendSyncErrorBroadcast();
    }
}
