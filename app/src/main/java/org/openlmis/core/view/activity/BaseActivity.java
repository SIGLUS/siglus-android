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

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.presenter.DummyPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.fragment.RetainedFragment;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import roboguice.RoboGuice;
import roboguice.activity.RoboMigrationAndroidXActionBarActivity;
import rx.Subscription;

@SuppressWarnings("PMD")
public abstract class BaseActivity extends RoboMigrationAndroidXActionBarActivity implements BaseView {

  private static long lastOperateTime = 0L;

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
  protected boolean isLoading = false;

  private long appTimeout;

  private long onCreateStartMili;
  private boolean isPageLoadTimerInProgress;

  public static synchronized void setLastOperateTime(long newOperateTIme) {
    BaseActivity.lastOperateTime = newOperateTIme;
  }

  public static synchronized long getLastOperateTime() {
    return lastOperateTime;
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
    if (getLastOperateTime() > 0L && alreadyTimeOuted() && !isLoginActivityActive()) {
      logout();
      return true;
    } else {
      setLastOperateTime(LMISApp.getInstance().getCurrentTimeMillis());
      return super.dispatchTouchEvent(ev);
    }
  }

  protected void logout() {
    startActivity(new Intent(this, LoginActivity.class));
    setLastOperateTime(0L);
  }

  private boolean isLoginActivityActive() {
    return this instanceof LoginActivity;
  }

  private boolean alreadyTimeOuted() {
    long currentTimeMillis = LMISApp.getInstance().getCurrentTimeMillis();
    return currentTimeMillis - getLastOperateTime() > appTimeout;
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

    appTimeout = Long.parseLong(getResources().getString(R.string.app_time_out));

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

    loadingDialog = new ProgressDialog(this);
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
          Log.d(this.getTitle() + " page",
              " load time " + (LMISApp.getInstance().getCurrentTimeMillis() - onCreateStartMili)
                  + " ms" + " in " + BuildConfig.VERSION_CODE);
          isPageLoadTimerInProgress = false;
        }
      }
    } catch (IllegalArgumentException e) {
      Log.d("View", "loaded -> dialog already dismissed");
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
    Map<String, String> productCodeMap = stockMovementRepository
        .queryStockCardIdAndProductCode(deleteStockCardIds);
    productCodes.addAll(productCodeMap.values());
    productCodes.addAll(deleteProductCodes);
    ImmutableList<String> deletedList = FluentIterable.from(productCodes)
        .limit(3)
        .transform((productCode) -> productCode).toList();
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
}

