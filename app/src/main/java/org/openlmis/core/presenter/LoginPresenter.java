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
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.LoginErrorType;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NetWorkException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.network.model.UserResponse;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncDownManager.SyncLocalUserProgress;
import org.openlmis.core.service.SyncDownManager.SyncProgress;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.service.sync.SyncStockCardsLastYearSilently;
import org.openlmis.core.training.TrainingEnvironmentHelper;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings("PMD")
public class LoginPresenter extends Presenter {

  private static final String TAG = LoginPresenter.class.getSimpleName();

  @Nullable
  LoginView view;


  @Inject
  LotRepository lotRepository;

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
  private PodRepository podRepository;

  @Inject
  private DirtyDataManager dirtyDataManager;

  @Override
  public void attachView(BaseView v) {
    this.view = (LoginView) v;
  }

  @Override
  public void onStop() {
    super.onStop();
    if (view != null) {
      view = null;
    }
  }

  private static final int TIMEOUT = 10 * 1000;

  private User user;

  public void startLogin(String userName, String password, boolean fromReSync) {
    hasGoneToNextPage = false;

    if (view == null) {
      return;
    }

    if (StringUtils.EMPTY.equals(userName.trim())) {
      view.showUserNameEmpty();
      return;
    }
    if (StringUtils.EMPTY.equals(password)) {
      view.showPasswordEmpty();
      return;
    }
    view.loading(LMISApp.getInstance().getString(R.string.msg_logging_in));
    user = new User(userName.trim(), password);
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      offlineLogin(user);
    } else {
      internetCheck.check(checkNetworkConnected(fromReSync));
    }
  }

  protected void onLoginFailed(LoginErrorType loginErrorType) {
    if (view != null) {
      view.loaded();
      view.showInvalidAlert(loginErrorType);
    }
  }

  protected void syncLocalUserData() {
    Observable.create((Observable.OnSubscribe<SyncLocalUserProgress>) subscriber1 -> {
      if (SharedPreferenceMgr.getInstance().getLastSyncProductTime() == null) {
        subscriber1.onNext(SyncLocalUserProgress.SYNC_LAST_SYNC_PRODUCT_FAIL);
        return;
      }

      if (!SharedPreferenceMgr.getInstance().isLastMonthStockDataSynced()) {
        subscriber1.onNext(SyncLocalUserProgress.SYNC_LAST_MONTH_STOCK_DATA_FAIL);
        return;
      }
      if (!SharedPreferenceMgr.getInstance().isRequisitionDataSynced()) {
        subscriber1.onNext(SyncLocalUserProgress.SYNC_REQUISITION_DATA_FAIL);
        return;
      }
      subscriber1.onNext(SyncLocalUserProgress.SYNC_LAST_DATA_SUCCESS);
    }).subscribe(getSyncLocalUserDataSubscriber());
  }

  private Subscriber<SyncLocalUserProgress> getSyncLocalUserDataSubscriber() {
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
            if (view != null) {
              view.loaded();
            }
            ToastUtil.show(R.string.msg_sync_products_list_failed);
            break;
          case SYNC_LAST_MONTH_STOCK_DATA_FAIL:
            if (view != null) {
              view.loaded();
            }
            ToastUtil.show(R.string.msg_sync_stock_movement_failed);
            break;
          case SYNC_REQUISITION_DATA_FAIL:
            if (view != null) {
              view.loaded();
            }
            ToastUtil.show(R.string.msg_sync_requisition_failed);
            break;
          case SYNC_LAST_DATA_SUCCESS:
            initialDirtyDataCheck();
            goToNextPage();
            break;
          default:
            // do nothing
        }
      }
    };
  }

  private void initialDirtyDataCheck() {
    try {
      dirtyDataManager.initialDirtyDataCheck();
    } catch (LMISException e) {
      e.reportToFabric();
    }
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
          ToastUtil.show(e.getMessage());
        }
        if (view != null) {
          view.loaded();
        }
      }

      @Override
      public void onNext(SyncProgress progress) {
        switch (progress) {
          case SYNCING_FACILITY_INFO:
          case SYNCING_SERVICE_LIST:
          case SYNCING_PRODUCT:
          case SYNCING_PODS:
          case SYNCING_STOCK_CARDS_LAST_MONTH:
          case SYNCING_REQUISITION:
            if (view != null) {
              view.loading(LMISApp.getInstance().getString(progress.getMessageCode()));
            }
            break;

          case PRODUCT_SYNCED:
            break;
          case STOCK_CARDS_LAST_MONTH_SYNCED:
            syncStockCards();
            break;

          case SHOULD_GO_TO_INITIAL_INVENTORY:
            if (view != null && !view.needInitInventory()) {
              ToastUtil.show(R.string.msg_initial_sync_success);
            }
            goToNextPage();
            break;

          default:
            // do nothing
        }
      }
    };
  }

  private void saveUserDataToLocalDatabase(final User user) {
    userRepository.createOrUpdate(user);
  }

  protected Observer<Boolean> resultObserver = new Observer<Boolean>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable throwable) {
      Log.w(TAG, "timeout, offlineLogin");
      offlineLogin(user);
    }

    @Override
    public void onNext(Boolean result) {
      // do nothing
    }
  };

  protected InternetCheckListener checkNetworkConnected(boolean fromReSync) {
    return internet -> {
      if (internet) {
        Observable.create(
                (OnSubscribe<Boolean>) subscriber -> loginRemote(user, fromReSync, (Subscriber<Boolean>) subscriber))
            .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultObserver);
      } else {
        offlineLogin(user);
      }
    };
  }

  private void offlineLogin(User user) {
    User localUser = userRepository.getLocalUser();
    if (localUser == null && !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      onLoginFailed(LoginErrorType.NO_INTERNET);
    } else {
      loginLocal(user);
    }
  }


  private void loginLocal(User user) {
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      if (sharedPreferenceMgr.getLastLoginTrainingTime() == null) {
        sharedPreferenceMgr.setLastLoginTrainingTime(DateUtil.formatDate(new Date(), DateUtil.SIMPLE_DATE_FORMAT));
      }
      if (userRepository.getLocalUser() == null) {
        TrainingEnvironmentHelper.getInstance().setUpData();
      }
    }
    setDefaultReportType();
    User localUser = userRepository.mapUserFromLocal(user);

    if (localUser == null) {
      onLoginFailed(LoginErrorType.WRONG_PASSWORD);
      return;
    }
    UserInfoMgr.getInstance().setUser(localUser);
    syncLocalUserData();
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

  private void loginRemote(final User user, final boolean fromReSync, Subscriber<Boolean> subscriber) {
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
              subscriber.onCompleted();
              onLoginSuccess(user, fromReSync);
            }
          }

          @Override
          public void failure(RetrofitError error) {
            subscriber.onCompleted();
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

  protected void onLoginSuccess(final User user, final boolean fromReSync) {
    Log.i(TAG, "Log in successful, setting up sync account");
    syncService.createSyncAccount(user);

    UserInfoMgr.getInstance().setUser(user);
    if (view != null) {
      view.clearErrorAlerts();
    }

    syncDownManager.syncDownServerData(getSyncSubscriber());

    if (view != null) {
      view.sendScreenToGoogleAnalyticsAfterLogin();
    }
    archiveOldData();
    saveUserDataToLocalDatabase(user);

    if (fromReSync) {
      Observable.create(subscriber -> {
        try {
          LMISApp.getInstance().getRestApi().recordReSyncAction();
        } catch (LMISException e) {
          Log.w(TAG, e);
        }
      }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(msg -> Log.i(TAG, msg.toString()), Throwable::printStackTrace);
    }
  }

  @SuppressWarnings("squid:S1905")
  private void archiveOldData() {
    if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_archive_old_data)) {
      return;
    }
    Observable.create((OnSubscribe<Void>) subscriber -> {
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
      if (podRepository.hasOldData()) {
        podRepository.deleteOldData();
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(msg -> Log.d(TAG, msg.toString()), Throwable::printStackTrace);
  }

  private void syncStockCards() {
    if (sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr
        .isSyncingLastYearStockCards()) {
      sharedPreferenceMgr.setIsSyncingLastYearStockCards(true);
      if (view != null) {
        view.sendSyncStartBroadcast();
      }
      syncStockCardsLastYearSilently.performSync().subscribe(getSyncLastYearStockCardSubscriber());
    } else {
      if (view != null) {
        view.sendSyncFinishedBroadcast();
      }
      sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
    }
  }

  @NonNull
  private Subscriber<List<StockCard>> getSyncLastYearStockCardSubscriber() {
    return new Subscriber<List<StockCard>>() {
      @Override
      public void onCompleted() {
        Log.i(TAG, "getSyncLastYearStockCardSubscriber onCompleted");
      }

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
        sharedPreferenceMgr.setStockCardLastYearSyncError(true);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        if (view != null) {
          view.sendSyncErrorBroadcast();
        }
        new LMISException(e).reportToFabric();
      }

      @Override
      public void onNext(List<StockCard> stockCards) {
        syncDownManager.saveStockCardsFromLastYear(stockCards).subscribe(getSaveStockCardsSubscriber());
      }
    };
  }

  @NonNull
  private Subscriber<Object> getSaveStockCardsSubscriber() {
    return new Subscriber<Object>() {
      @Override
      public void onCompleted() {
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(false);
        sharedPreferenceMgr.setStockCardLastYearSyncError(false);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        sharedPreferenceMgr.setStockLastSyncTime();
        initialDirtyDataCheck();
        if (view != null) {
          view.sendSyncFinishedBroadcast();
        }
      }

      @Override
      public void onError(Throwable e) {
        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(true);
        sharedPreferenceMgr.setStockCardLastYearSyncError(true);
        sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
        if (view != null) {
          view.sendSyncErrorBroadcast();
        }
      }

      @Override
      public void onNext(Object aVoid) {
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
    if (view == null) {
      return;
    }

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

    void sendSyncErrorBroadcast();
  }
}
