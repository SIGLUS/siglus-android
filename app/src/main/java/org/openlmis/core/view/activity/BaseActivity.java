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

import static org.openlmis.core.utils.Constants.SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentManager;
import com.google.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.enumeration.LoginErrorType;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.presenter.DummyPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.AutoSizeUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.fragment.RetainedFragment;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.fragment.SimpleDialogFragment.MsgDialogCallBack;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.openlmis.core.view.widget.ClickIntervalChecker;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import roboguice.RoboGuice;
import roboguice.activity.RoboMigrationAndroidXActionBarActivity;
import rx.Subscription;

@SuppressWarnings("PMD")
public abstract class BaseActivity extends RoboMigrationAndroidXActionBarActivity implements BaseView {

  @Inject
  SharedPreferenceMgr preferencesMgr;
  @Inject
  WarningDialogFragmentBuilder warningDialogFragmentBuilder;
  @Inject
  StockMovementRepository stockMovementRepository;

  protected RetainedFragment dataFragment;
  protected Presenter basePresenter;
  protected List<Subscription> subscriptions = new ArrayList<>();

  protected Class<? extends Presenter> presenterClass;
  protected ProgressDialog loadingDialog;

  @Getter
  protected boolean isLoading = false;

  private long onCreateStartMili;
  private boolean isPageLoadTimerInProgress;
  private static final String TAG_LOGOUT_ALERT = "logout_alert";

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveNotAndroidUserErrorEvent(LoginErrorType loginErrorType) {
    if (loginErrorType.toString().equals(LoginErrorType.NON_MOBILE_USER.toString())) {
      popUpLogoutAlertFragment(getString(
          R.string.error_msg_sync_failed), getString(R.string.msg_sync_not_android_user));
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveNotRegisteredDeviceErrorEvent(String message) {
    if (SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE.equals(message)) {
      popUpLogoutAlertFragment(
          getString(R.string.error_msg_sync_failed), getString(R.string.msg_sync_not_same_device_user));
    }
  }

  public void injectPresenter() {
    Field[] fields = FieldUtils.getAllFields(this.getClass());

    Optional<Field> annotatedFiled = FluentIterable.from(newArrayList(fields))
        .firstMatch(field -> field.getAnnotation(InjectPresenter.class) != null);

    if (annotatedFiled.isPresent()) {
      InjectPresenter annotation = annotatedFiled.get().getAnnotation(InjectPresenter.class);
      if (!Presenter.class.isAssignableFrom(annotation.value())) {
        throw new IllegalArgumentException("Invalid InjectPresenter class :" + annotation.value());
      }

      basePresenter = initPresenter(annotation.value());
      try {
        annotatedFiled.get().setAccessible(true);
        annotatedFiled.get().set(this, basePresenter);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(
            "InjectPresenter type cast failed :" + annotation.value().getSimpleName());
      }
    }
    if (basePresenter == null) {
      basePresenter = new DummyPresenter();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    basePresenter.onStart();
    sendScreenToGoogleAnalytics();
  }

  protected abstract ScreenName getScreenName();

  protected void sendScreenToGoogleAnalytics() {
    ScreenName screenName = getScreenName();

    if (screenName != null) {
      AnalyticsTracker.getInstance().trackScreen(screenName);
    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (!isLoginActivityActive() && ClickIntervalChecker.getInstance().isAppTimeOut()) {
      ClickIntervalChecker.getInstance().resetLastOperateTime();
      logout();
      return true;
    }
    ClickIntervalChecker.setLastOperateTime(LMISApp.getInstance().getCurrentTimeMillis());
    return super.dispatchTouchEvent(ev);
  }

  protected void logout() {
    Intent toLoginIntent = new Intent(this, LoginActivity.class);
    toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(toLoginIntent);
  }

  private boolean isLoginActivityActive() {
    return this instanceof LoginActivity;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    isPageLoadTimerInProgress = true;
    onCreateStartMili = LMISApp.getInstance().getCurrentTimeMillis();

    setTheme(getThemeRes());
    super.onCreate(savedInstanceState);
    initDataFragment();
    injectPresenter();

    try {
      basePresenter.attachView(BaseActivity.this);
    } catch (ViewNotMatchException e) {
      new LMISException(e, "BaseActivity:onCreate").reportToFabric();
      ToastUtil.show(e.getMessage());
      return;
    }

    if (getSupportActionBar() != null) {
      getSupportActionBar().setHomeButtonEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
      EventBus.getDefault().register(this);
    }

  }

  @StyleRes
  protected int getThemeRes() {
    return R.style.AppTheme;
  }

  @Override
  protected void onDestroy() {
    if (basePresenter != null && presenterClass != null) {
      basePresenter.onStop();
      dataFragment.putData(presenterClass.getSimpleName(), basePresenter);
    }

    unSubscribeSubscriptions();
    super.onDestroy();
    if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
      EventBus.getDefault().unregister(this);
    }
  }

  private void unSubscribeSubscriptions() {
    for (Subscription subscription : subscriptions) {
      if (subscription != null) {
        subscription.unsubscribe();
      }
    }
  }

  private void initDataFragment() {
    FragmentManager fm = getSupportFragmentManager();
    dataFragment = (RetainedFragment) fm.findFragmentByTag("RetainedFragment");

    if (dataFragment == null) {
      dataFragment = new RetainedFragment();
      fm.beginTransaction().add(dataFragment, "RetainedFragment").commit();
    }
  }

  protected Presenter initPresenter(Class<? extends Presenter> clazz) {
    // find the retained fragment on activity restarts
    presenterClass = clazz;
    Presenter presenter = (Presenter) dataFragment.getData(presenterClass.getSimpleName());
    if (presenter == null) {
      presenter = RoboGuice.getInjector(getApplicationContext()).getInstance(presenterClass);
      dataFragment.putData(presenterClass.getSimpleName(), presenter);
    }

    return presenter;
  }

  @Override
  public void loading() {
    loading(StringUtils.EMPTY);
  }

  @Override
  public void loading(String message) {
    loaded();

    loadingDialog = new ProgressDialog(this, R.style.AlertDialog);
    loadingDialog.setMessage(message);
    loadingDialog.setIndeterminate(false);
    loadingDialog.setCanceledOnTouchOutside(false);

    if (!isFinishing()) {
      loadingDialog.show();
    }

    isLoading = true;
  }

  @Override
  public void loaded() {
    try {
      if (loadingDialog != null && !isFinishing()) {
        loadingDialog.dismiss();
        loadingDialog = null;
        isLoading = false;
        if (isPageLoadTimerInProgress) {
          Log.i(this.getTitle() + " page",
              " load time " + (LMISApp.getInstance().getCurrentTimeMillis() - onCreateStartMili)
                  + " ms" + " in " + BuildConfig.VERSION_CODE);
          isPageLoadTimerInProgress = false;
        }
      }
    } catch (IllegalArgumentException e) {
      Log.w("View", "loaded -> dialog already dismissed");
    }
  }

  public void saveString(String key, String value) {
    preferencesMgr.getPreference().edit().putString(key, value).apply();
  }

  public void startActivity(Class<?> activityName, boolean closeThis) {
    Intent intent = new Intent();
    intent.setClass(this, activityName);
    startActivity(intent);

    if (closeThis) {
      this.finish();
    }
  }

  public void startActivity(Class<? extends Activity> activityName) {
    startActivity(activityName, false);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (!ClickIntervalChecker.getInstance().isClickLongerThanInterval()) {
      return true;
    }
    ClickIntervalChecker.setLastClickItemTime(LMISApp.getInstance().getCurrentTimeMillis());
    if (android.R.id.home == item.getItemId()) {
      onBackPressed();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  public String getDeletedProductCodeList() {
    List<StockMovementItem> deleteStockMovementItems = preferencesMgr.getDeletedMovementItems();
    List<String> deleteProductCodes = preferencesMgr.getDeletedProduct();
    Set<String> deleteStockCardIds = new HashSet<>();
    Set<String> productCodes = new HashSet<>();
    if (!deleteStockMovementItems.isEmpty()) {
      for (StockMovementItem item : deleteStockMovementItems) {
        deleteStockCardIds.add(String.valueOf(item.getStockCard().getId()));
      }
    }
    Map<String, String> productCodeMap = stockMovementRepository.queryStockCardIdAndProductCode(deleteStockCardIds);
    productCodes.addAll(productCodeMap.values());
    productCodes.addAll(deleteProductCodes);
    ImmutableList<String> deletedList = FluentIterable.from(productCodes).limit(3).toList();
    return deletedList.toString();
  }

  public void showDeletedWarningDialog(WarningDialogFragment.DialogDelegate dialogDelegate) {
    WarningDialogFragment warningDialogFragment = warningDialogFragmentBuilder
        .build(dialogDelegate,
            getString(R.string.dirty_data_correct_warning,
                getDeletedProductCodeList()),
            getString(R.string.btn_del),
            getString(R.string.dialog_cancel));
    getSupportFragmentManager().beginTransaction()
        .add(warningDialogFragment, "deleteProductWarningDialogFragment").commitNow();
  }

  public void showDirtyDataWarningDialog(WarningDialogFragment.DialogDelegate dialogDelegate) {
    showDirtyDataWarningDialog(getSupportFragmentManager(), dialogDelegate);
  }

  public void showDirtyDataWarningDialog(
      FragmentManager supportFragmentManager,
      WarningDialogFragment.DialogDelegate dialogDelegate
  ) {
    WarningDialogFragment warningDialogFragment = warningDialogFragmentBuilder
        .build(dialogDelegate,
            getString(R.string.dirty_data_warning,
                getDeletedProductCodeList()),
            getString(R.string.btn_confirm),
            getString(R.string.dialog_cancel));

    String dirtyDataWarningDialogFragmentTag = "dirtyDataWarningDialogFragment";
    if (supportFragmentManager.findFragmentByTag(dirtyDataWarningDialogFragmentTag) == null) {
      supportFragmentManager.beginTransaction()
          .add(warningDialogFragment, dirtyDataWarningDialogFragmentTag).commitNow();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    AutoSizeUtil.resetScreenSize(this);
  }

  private void popUpLogoutAlertFragment(String title, String msg) {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        title, msg, getString(R.string.btn_ok), null);
    dialogFragment.setCallBackListener(new MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        SharedPreferenceMgr.getInstance().setLastLoginUser(StringUtils.EMPTY);
        logout();
      }

      @Override
      public void negativeClick(String tag) {
        //do nothing
      }
    });
    dialogFragment.setCancelable(false);
    dialogFragment.showOnlyOnce(
        LMISApp.getInstance().getTopActivity().getSupportFragmentManager(), TAG_LOGOUT_ALERT);
  }
}

