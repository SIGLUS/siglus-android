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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.DummyPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.View;
import org.openlmis.core.view.fragment.RetainedFragment;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.Field;

import roboguice.RoboGuice;
import roboguice.activity.RoboActionBarActivity;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public abstract class BaseActivity extends RoboActionBarActivity implements View {


    protected RetainedFragment dataFragment;
    @Inject
    SharedPreferenceMgr preferencesMgr;
    protected SearchView searchView;

    private long APP_TIMEOUT;

    protected Presenter presenter;

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

    ProgressDialog loadingDialog;

    protected Class<? extends Presenter> presenterClass;

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (LMISApp.lastOperateTime > 0L && alreadyTimeOuted(LMISApp.getInstance().getCurrentTimeMillis())) {
            logout();
            return true;
        } else {
            LMISApp.lastOperateTime = LMISApp.getInstance().getCurrentTimeMillis();
            return super.dispatchTouchEvent(ev);
        }
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        LMISApp.lastOperateTime = 0L;
    }

    private boolean alreadyTimeOuted(Long eventTime) {
        return eventTime - LMISApp.lastOperateTime > APP_TIMEOUT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataFragment();
        injectPresenter();
        try {
            presenter.attachView(BaseActivity.this);
        } catch (ViewNotMatchException e) {
            e.printStackTrace();
            showMessage(e.getMessage());
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        APP_TIMEOUT = Long.parseLong(getResources().getString(R.string.app_time_out));
    }

    @Override
    protected void onDestroy() {
        if (presenter != null && presenterClass != null) {
            dataFragment.putData(presenterClass.getSimpleName(), presenter);
        }
        super.onDestroy();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return onSearchStart(newText);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return onSearchClosed();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void clearSearch() {
        if (searchView != null) {
            searchView.setQuery(StringUtils.EMPTY, true);
        }
    }

    public void loading() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setIndeterminate(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        if (!isFinishing()) {
            loadingDialog.show();
        }
    }

    public void loaded() {
        try {
            if (loadingDialog != null && !isFinishing()) {
                loadingDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            Log.d("View", "loaded -> dialog already dismissed");
        }
    }

    public void saveString(String key, String value) {
        preferencesMgr.getPreference().edit().putString(key, value).apply();
    }

    public void saveInt(String key, int value) {
        preferencesMgr.getPreference().edit().putInt(key, value).apply();
    }

    public void saveBoolean(String key, boolean value) {
        preferencesMgr.getPreference().edit().putBoolean(key, value).apply();
    }

    public SharedPreferences getPreferences() {
        return preferencesMgr.getPreference();
    }

    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showMessage(int resId) {
        String msg = getResources().getString(resId);
        showMessage(msg);
    }

    public void showMessage(int resId, Object... args) {
        String msg = getResources().getString(resId, args);
        showMessage(msg);
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
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                return onSettingClick();
            case R.id.action_add_new_drug:
                startActivity(new Intent()
                        .setClass(this, InventoryActivity.class)
                        .putExtra(InventoryActivity.PARAM_IS_ADD_NEW_DRUG, true));
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

    public boolean onSearchStart(String query) {
        return false;
    }

    public boolean onSearchClosed() {
        return false;
    }

    public boolean onSettingClick() {
        return false;
    }
}

