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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.widget.SyncTimeView;

import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_home_page)
public class HomeActivity extends BaseActivity {

    @InjectView(R.id.btn_stock_card)
    Button btnStockCard;

    @InjectView(R.id.btn_inventory)
    Button btnInventory;

    SyncTimeView syncTimeView;

    @InjectView(R.id.btn_mmia_list)
    Button btnMMIAList;

    @InjectView(R.id.btn_via_list)
    Button btnVIAList;

    @InjectView(R.id.btn_kit_stock_card)
    Button btnKitStockCard;

    @InjectResource(R.integer.back_twice_interval)
    int BACK_TWICE_INTERVAL;

    @Inject
    SyncService syncService;

    private boolean exitPressedOnce = false;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.HomeScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UserInfoMgr.getInstance().getUser() == null) {
            // In case some users use some unknown way entered here!!!
            logout();
            finish();
        } else {
            setTitle(UserInfoMgr.getInstance().getFacilityName());
            syncTimeView = (SyncTimeView) findViewById(R.id.view_sync_time);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        registerSyncStartReceiver();
        registerSyncFinishedReceiver();
    }

    private void registerSyncStartReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_FILTER_START_SYNC_DATA);
        registerReceiver(syncStartReceiver, filter);
    }

    private void registerSyncFinishedReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
        registerReceiver(syncFinishedReceiver, filter);
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_Gray;
    }

    BroadcastReceiver syncStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            syncTimeView.showSyncProgressBarAndHideIcon();
        }
    };

    BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setSyncedTime();
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(syncStartReceiver);
        unregisterReceiver(syncFinishedReceiver);
        super.onDestroy();
    }

    public void onClickStockCard(View view) {
        startActivity(StockCardListActivity.class);
    }

    public void onClickKitStockCard(View view) {
        startActivity(KitStockCardListActivity.class);
    }

    public void onClickInventory(View view) {
        Intent intent = new Intent(HomeActivity.this, InventoryActivity.class);
        intent.putExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, true);
        startActivity(intent);
    }

    public void syncData() {
        Log.d("HomeActivity", "requesting immediate sync");
        syncService.requestSyncImmediately();
    }

    public void onClickMMIAHistory(View view) {
        startActivity(RnRFormListActivity.getIntentToMe(this, Constants.MMIA_PROGRAM_CODE));
        TrackRnREventUtil.trackRnRListEvent(TrackerActions.SelectMMIA, Constants.MMIA_PROGRAM_CODE);
    }

    public void onClickVIAHistory(View view) {
        startActivity(RnRFormListActivity.getIntentToMe(this, Constants.VIA_PROGRAM_CODE));
        TrackRnREventUtil.trackRnRListEvent(TrackerActions.SelectVIA, Constants.VIA_PROGRAM_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSyncedTime();
    }

    protected void setSyncedTime() {
        syncTimeView.showLastSyncTime();
    }

    @Override
    public void onBackPressed() {
        if (exitPressedOnce) {
            moveTaskToBack(true);
        } else {
            ToastUtil.show(R.string.msg_back_twice_to_exit);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exitPressedOnce = false;
                }
            }, BACK_TWICE_INTERVAL);
        }
        exitPressedOnce = !exitPressedOnce;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_wipe_data_and_resync)) {
            menu.findItem(R.id.action_wipe_data).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                startActivity(LoginActivity.class);
                finish();
                return true;
            case R.id.action_sync_data:
                syncData();
                return true;
            case R.id.action_wipe_data:
                alertWipeData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private void alertWipeData() {
        if (!LMISApp.getInstance().isConnectionAvailable()) {
            ToastUtil.show(R.string.message_wipe_no_connection);
            return;
        }

        WarningDialogFragment wipeDataDialog = WarningDialogFragment.newInstance(
                R.string.message_warning_wipe_data, R.string.btn_positive, R.string.btn_negative);
        wipeDataDialog.setDelegate(new WarningDialogFragment.DialogDelegate() {
            @Override
            public void onPositiveClick() {
                setRestartIntent();
                LMISApp.getInstance().wipeAppData();
            }
        });
        wipeDataDialog.show(getFragmentManager(), "WipeDataWarning");
    }

    private void setRestartIntent() {
        int requestCode = 100;
        int startAppInterval = 500;

        User currentUser = UserInfoMgr.getInstance().getUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constants.PARAM_USERNAME, currentUser.getUsername());
        intent.putExtra(Constants.PARAM_PASSWORD, currentUser.getPassword());

        PendingIntent mPendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + startAppInterval, mPendingIntent);
    }

}
