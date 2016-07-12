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

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.DummyPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.fragment.RetainedFragment;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.activity.RoboActionBarActivity;
import rx.Subscription;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public abstract class BaseActivity extends RoboActionBarActivity implements BaseView {

    @Inject
    SharedPreferenceMgr preferencesMgr;

    protected RetainedFragment dataFragment;
    protected Presenter presenter;
    protected List<Subscription> subscriptions = new ArrayList<>();

    protected Class<? extends Presenter> presenterClass;
    protected ProgressDialog loadingDialog;
    protected boolean isLoading = false;

    private long APP_TIMEOUT;

    private long onCreateStartMili;
    private boolean isPageLoadTimerInProgress;

    public void injectPresenter() {
        Field[] fields = FieldUtils.getAllFields(this.getClass());

        Optional<Field> annotatedFiled = FluentIterable.from(newArrayList(fields)).firstMatch(new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return field.getAnnotation(InjectPresenter.class) != null;
            }
        });

        if (annotatedFiled.isPresent()) {
            InjectPresenter annotation = annotatedFiled.get().getAnnotation(InjectPresenter.class);
            if (!Presenter.class.isAssignableFrom(annotation.value())) {
                throw new RuntimeException("Invalid InjectPresenter class :" + annotation.value());
            }

            presenter = initPresenter(annotation.value());
            try {
                annotatedFiled.get().setAccessible(true);
                annotatedFiled.get().set(this, presenter);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("InjectPresenter type cast failed :" + annotation.value().getSimpleName());
            }
        }
        if (presenter == null) {
            presenter = new DummyPresenter();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
        sendScreenToGoogleAnalytics();
    }

    protected abstract ScreenName getScreenName();

    protected void sendScreenToGoogleAnalytics() {
        ScreenName screenName = getScreenName();

        if (screenName != null) {
            LMISApp.getInstance().trackScreen(screenName);
        }
    }

    ;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (LMISApp.lastOperateTime > 0L && alreadyTimeOuted() && !isLoginActivityActive()) {
            logout();
            return true;
        } else {
            LMISApp.lastOperateTime = LMISApp.getInstance().getCurrentTimeMillis();
            return super.dispatchTouchEvent(ev);
        }
    }

    protected void logout() {
        startActivity(new Intent(this, LoginActivity.class));
        LMISApp.lastOperateTime = 0L;
    }

    private boolean isLoginActivityActive() {
        return this instanceof LoginActivity;
    }

    private boolean alreadyTimeOuted() {
        Long currentTimeMillis = LMISApp.getInstance().getCurrentTimeMillis();
        return currentTimeMillis - LMISApp.lastOperateTime > APP_TIMEOUT;
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
            presenter.attachView(BaseActivity.this);
        } catch (ViewNotMatchException e) {
            e.reportToFabric();
            ToastUtil.show(e.getMessage());
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        APP_TIMEOUT = Long.parseLong(getResources().getString(R.string.app_time_out));

    }

    protected
    @StyleRes
    int getThemeRes() {
        return R.style.AppTheme;
    }

    @Override
    protected void onDestroy() {
        if (presenter != null && presenterClass != null) {
            presenter.onStop();
            dataFragment.putData(presenterClass.getSimpleName(), presenter);
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
        FragmentManager fm = getFragmentManager();
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
                    Log.d(this.getTitle() + " page", "load time " + (System.currentTimeMillis() - onCreateStartMili) + " ms");
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

    public void startActivity(Class activityName, boolean closeThis) {
        Intent intent = new Intent();
        intent.setClass(this, activityName);
        startActivity(intent);

        if (closeThis) this.finish();
    }

    public void startActivity(Class activityName) {
        startActivity(activityName, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void hideImm() {
        InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mImm != null && mImm.isActive() && this.getCurrentFocus() != null) {
            mImm.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), 0);
        }
    }

}

