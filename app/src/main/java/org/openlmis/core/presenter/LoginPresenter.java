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

import static org.openlmis.core.utils.Constants.GRANT_TYPE;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enums.LoginErrorType;
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
import org.openlmis.core.service.SyncDownManager.SyncLocalUserProgress;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.training.TrainingEnvironmentHelper;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings("PMD")
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
  @Inject
  InternetCheck internetCheck;
  private boolean hasGoneToNextPage;
  @Inject
  private ProgramRepository programRepository;
  @Inject
  private DirtyDataRepository dirtyDataRepository;
  @Inject
  private DirtyDataManager dirtyDataManager;

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
    if (!LMISApp.getInstance().isRoboUniTest()) {
      new InternetCheck().execute(checkNetworkConnected(user, fromReSync));
    } else {
      internetCheck.execute(checkNetworkConnected(user, fromReSync));
    }
  }

  public void onLoginFailed(LoginErrorType loginErrorType) {
    view.loaded();
    view.showInvalidAlert(loginErrorType);
  }

  public void syncLocalUserData(Subscriber<SyncLocalUserProgress> subscriber) {
    Observable.create((Observable.OnSubscribe<SyncLocalUserProgress>) subscriber1 -> {
      if (SharedPreferenceMgr.getInstance().getLastSyncProductTime() == null) {
        subscriber1.onNext(SyncLocalUserProgress.SYNC_LAST_SYNC_PRODUCT_FAIL);
        return;
      }

      if (!SharedPreferenceMgr.getInstance().isLastMonthStockDataSynced()) {
        subscriber1.onNext(SyncLocalUserProgress.SYNC_LAST_MONTH_STOCK_DATA_FAIL);
        return;
      }
      // TODO: change back to the original check after end the development of sync down requisitions
      // if (!SharedPreferenceMgr.getInstance().isRequisitionDataSynced()) {
      //   subscriber1.onNext(SyncLocalUserProgress.SyncRequisitionDataFail);
      //   return;
      // }
      subscriber1.onNext(SyncLocalUserProgress.SYNC_LAST_DATA_SUCCESS);
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(subscriber);

  }

  @NonNull
  public Subscriber<SyncLocalUserProgress> getSyncLocalUserDataSubscriber() {
    return new Subscriber<SyncLocalUserProgress>() {
      @Override
      public void onCompleted() {
        // do nothing
      }

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
      }

      @Override
      public void onNext(SyncLocalUserProgress progress) {
        switch (progress) {
          case SYNC_LAST_SYNC_PRODUCT_FAIL:
            view.loaded();
            ToastUtil.show(R.string.msg_sync_products_list_failed);
            break;
          case SYNC_LAST_MONTH_STOCK_DATA_FAIL:
            view.loaded();
            ToastUtil.show(R.string.msg_sync_stock_movement_failed);
            break;
          case SYNC_REQUISITION_DATA_FAIL:
            view.loaded();
            ToastUtil.show(R.string.msg_sync_requisition_failed);
            break;
          case SYNC_LAST_DATA_SUCCESS:
            dirtyDataManager.initialDirtyDataCheck();
            goToNextPage();
            break;
          default:
            // do nothing
        }
      }
    };
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
        if (!LMISApp.getContext().getResources().getString(R.string.msg_isAndroid_False).equals(e.getMessage())) {
          ToastUtil.showForLongTime(e.getMessage());
        }
        view.loaded();
      }

      @Override
      public void onNext(SyncProgress progress) {
        switch (progress) {
          case SYNCING_FACILITY_INFO:
          case SYNCING_SERVICE_LIST:
          case SYNCING_PRODUCT:
          case SYNCING_STOCK_CARDS_LAST_MONTH:
          case SYNCING_REQUISITION:
            view.loading(LMISApp.getInstance().getString(progress.getMessageCode()));
            break;

          case PRODUCT_SYNCED:
            break;
          case STOCK_CARDS_LAST_MONTH_SYNCED:
            syncStockCards();
            break;

          case SHOULD_GO_TO_INITIAL_INVENTORY:
            if (!view.needInitInventory()) {
              ToastUtil.showForLongTime(R.string.msg_initial_sync_success);
            }
            goToNextPage();
            break;

          default:
            // do nothing
        }
      }
    };
  }

  protected void saveUserDataToLocalDatabase(final User user) throws LMISException {
    userRepository.createOrUpdate(user);
  }

  private InternetCheck.Callback checkNetworkConnected(User user, boolean fromReSync) {
    return internet -> {
      if (internet && !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
        loginRemote(user, fromReSync);
      } else {
        User localUser = userRepository.getLocalUser();
        if (localUser == null) {
          onLoginFailed(LoginErrorType.NO_INTERNET);
        } else {
          loginLocal(user);
        }
      }
    };
  }

  private void loginLocal(User user) {
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      TrainingEnvironmentHelper.getInstance().setUpData();
    }
    setDefaultReportType();
    User localUser = userRepository.mapUserFromLocal(user);

    if (localUser == null) {
      onLoginFailed(LoginErrorType.WRONG_PASSWORD);
      return;
    }

    UserInfoMgr.getInstance().setUser(localUser);
    syncLocalUserData(getSyncLocalUserDataSubscriber());
  }

  private void setDefaultReportType() {
    if (SharedPreferenceMgr.getInstance().getReportTypesData().isEmpty()) {
      try {
        List<ReportTypeForm> reportTypeForms = reportTypeFormRepository.listAll();
        SharedPreferenceMgr.getInstance().setReportTypesData(reportTypeForms);
      } catch (LMISException e) {
        new LMISException(e, "setDefaultReportType").reportToFabric();
      }
    }
  }

  private void loginRemote(final User user, final boolean fromReSync) {
    if (UserInfoMgr.getInstance().getUser() != null) {
      UserInfoMgr.getInstance().getUser().setIsTokenExpired(true);
    }
    LMISApp.getInstance().getRestApi()
        .login(GRANT_TYPE, user.getUsername(), user.getPassword(), new Callback<UserResponse>() {
          @Override
          public void success(UserResponse userResponse, Response response) {
            if (userResponse == null || userResponse.getAccessToken() == null) {
              onLoginFailed(LoginErrorType.WRONG_PASSWORD);
            } else {
              user.setAccessToken(userResponse.getAccessToken());
              user.setTokenType(userResponse.getTokenType());
              user.setReferenceDataUserId(userResponse.getReferenceDataUserId());
              user.setIsTokenExpired(false);
              onLoginSuccess(user, fromReSync);
            }
          }

          @Override
          public void failure(RetrofitError error) {
            if (error.getCause() instanceof NetWorkException) {
              User localUser = userRepository.getLocalUser();
              if (localUser == null) {
                onLoginFailed(LoginErrorType.NO_INTERNET);
              } else {
                loginLocal(user);
              }
            } else {
              onLoginFailed(LoginErrorType.WRONG_PASSWORD);
            }
          }
        });
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
      Log.w(TAG, e);
    }

    if (fromReSync) {
      Observable.create(subscriber -> {
        try {
          LMISApp.getInstance().getRestApi().recordReSyncAction();
        } catch (LMISException e) {
          Log.w(TAG, e);
        }
      }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(msg -> Log.d(TAG, msg.toString()), Throwable::printStackTrace);
    }
  }

  @SuppressWarnings("squid:S1905")
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
      if (programDataFormRepository.hasOldDate()) {
        programDataFormRepository.deleteOldData();
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(msg -> Log.d(TAG, msg.toString()), Throwable::printStackTrace);

  }

  private void syncStockCards() {
    if (sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr
        .isSyncingLastYearStockCards()) {
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
      }

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
        sharedPreferenceMgr.setStockCardLastYearSyncError(true);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        view.sendSyncErrorBroadcast(e.getMessage());
        new LMISException(e).reportToFabric();
      }

      @Override
      public void onNext(List<StockCard> stockCards) {
        syncDownManager.saveStockCardsFromLastYear(stockCards)
            .subscribe(getSaveStockCardsSubscriber());
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
        view.sendSyncErrorBroadcast("Save one year stock failed");
      }

      @Override
      public void onNext(Void aVoid) {
        // do nothing
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

    void goToHomePage();

    void goToInitInventory();

    boolean needInitInventory();

    void showInvalidAlert(LoginErrorType loginErrorType);

    void showUserNameEmpty();

    void showPasswordEmpty();

    void clearErrorAlerts();

    void sendScreenToGoogleAnalyticsAfterLogin();

    void sendSyncStartBroadcast();

    void sendSyncFinishedBroadcast();

    void sendSyncErrorBroadcast(String errorMsg);
  }
}
