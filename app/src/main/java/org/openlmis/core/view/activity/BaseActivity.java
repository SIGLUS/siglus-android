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
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.View;

import roboguice.activity.RoboActionBarActivity;

public abstract class BaseActivity extends RoboActionBarActivity implements View {


    @Inject
    SharedPreferenceMgr preferencesMgr;
    protected SearchView searchView;

    private long APP_TIMEOUT;

    public abstract Presenter getPresenter();

    ProgressDialog loadingDialog;

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPresenter().onStop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (LMISApp.lastOperateTime > 0L && alreadyTimeOuted(ev.getEventTime())) {
            logout();
            return true;
        } else {
            LMISApp.lastOperateTime = ev.getEventTime();
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
        try {
            getPresenter().attachView(BaseActivity.this);
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
        if (loadingDialog != null) {
            try {
                loadingDialog.dismiss();
            } catch (IllegalArgumentException e) {
                Log.d("View", "loaded -> dialog already dismissed");
            }
            if (loadingDialog != null && !isFinishing()) {
                loadingDialog.dismiss();
            }
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

