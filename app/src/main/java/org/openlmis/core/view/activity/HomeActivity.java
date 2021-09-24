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

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.inject.Inject;
import java.io.File;
import org.apache.commons.collections.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.event.CmmCalculateEvent;
import org.openlmis.core.event.SyncPercentEvent;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.persistence.ExportSqliteOpenHelper;
import org.openlmis.core.presenter.HomePresenter;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.FileUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.widget.DashboardView;
import org.openlmis.core.view.widget.IncompleteRequisitionBanner;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.openlmis.core.view.widget.SyncTimeView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_home_page)
public class HomeActivity extends BaseActivity implements HomePresenter.HomeView {

  private static final String EXPORT_DATA_PARENT_DIR = "//data//";

  IncompleteRequisitionBanner incompleteRequisitionBanner;

  SyncTimeView syncTimeView;

  @InjectView(R.id.dv_product_dashboard)
  DashboardView dvProductDashboard;

  @InjectView(R.id.btn_stock_card)
  View btnStockCardOverView;

  @InjectView(R.id.btn_inventory)
  View btnPhysicalInventory;

  @InjectView(R.id.btn_requisitions)
  View btnRequisitions;

  @InjectView(R.id.btn_kits)
  View btnKits;

  @InjectView(R.id.btn_issue_voucher)
  View btnIssueVoucher;

  @InjectResource(R.integer.back_twice_interval)
  private int backTwiceInterval;

  @Inject
  SyncService syncService;
  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;
  @Inject
  DirtyDataManager dirtyDataManager;

  @InjectPresenter(HomePresenter.class)
  private HomePresenter homePresenter;

  private boolean exitPressedOnce = false;

  private static final int PERMISSION_REQUEST_CODE = 200;

  private boolean isCmmCalculating = false;

  private int syncedCount = 0;

  public void syncData() {
    syncService.requestSyncImmediatelyFromUserTrigger();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveSyncStatusEvent(SyncStatusEvent event) {
    switch (event.getStatus()) {
      case START:
        syncTimeView.showSyncProgressBarAndHideIcon();
        break;
      case FINISH:
        setSyncedTime();
        refreshDashboard();
        break;
      case ERROR:
        if (event.getMsg() != null) {
          syncTimeView.setSyncedMovementError(event.getMsg());
        } else {
          syncTimeView.setSyncStockCardLastYearError();
        }
        break;
      default:
        break;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveCmmCalculateEvent(CmmCalculateEvent event) {
    isCmmCalculating = event.isStart();
    refreshDashboard();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveSyncPercentEvent(SyncPercentEvent event) {
    this.syncedCount = event.getSyncedCount();
    refreshDashboard();
  }

  @Override
  public void onBackPressed() {
    if (exitPressedOnce) {
      moveTaskToBack(true);
    } else {
      ToastUtil.show(R.string.msg_back_twice_to_exit);
      new Handler().postDelayed(() -> exitPressedOnce = false, backTwiceInterval);
    }
    exitPressedOnce = !exitPressedOnce;
  }

  public static Intent getIntentToMe(Context context) {
    Intent intent = new Intent(context, HomeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    return intent;
  }

  @Override
  public void updateDashboard(int regularAmount, int outAmount, int lowAmount, int overAmount) {
    dvProductDashboard.showFinished(regularAmount, outAmount, lowAmount, overAmount);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_home, menu);
    return true;
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
      case R.id.action_export_db:
        exportDB();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode != PERMISSION_REQUEST_CODE) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }
    if (permissions.length <= 0 || grantResults.length <= 0) {
      finish();
      return;
    }
    boolean flag = true;
    for (int grantResult : grantResults) {
      if (grantResult != PackageManager.PERMISSION_GRANTED) {
        flag = false;
        break;
      }
    }
    if (flag) {
      exportDBHavePermission();
    } else {
      finish();
    }
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.HOME_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
    if (UserInfoMgr.getInstance().getUser() == null) {
      // In case some users use some unknown way entered here!!!
      logout();
      finish();
    } else {
      setTitle(UserInfoMgr.getInstance().getFacilityName());
      syncTimeView = findViewById(R.id.view_sync_time);
      incompleteRequisitionBanner = findViewById(R.id.view_incomplete_requisition_banner);
      btnStockCardOverView.setOnClickListener(singleClickButtonListener);
      btnPhysicalInventory.setOnClickListener(singleClickButtonListener);
      btnRequisitions.setOnClickListener(singleClickButtonListener);
      btnKits.setOnClickListener(singleClickButtonListener);
      btnIssueVoucher.setOnClickListener(singleClickButtonListener);
      if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      }
    }
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_Gray;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      incompleteRequisitionBanner.setIncompleteRequisitionBanner();
    } else {
      incompleteRequisitionBanner.setVisibility(View.GONE);
    }
    if (sharedPreferenceMgr.isStockCardLastYearSyncError()) {
      syncTimeView.setSyncStockCardLastYearError();
    } else if (!TextUtils.isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
      syncTimeView.setSyncedMovementError(sharedPreferenceMgr.getStockMovementSyncError());
    } else {
      setSyncedTime();
    }

    dirtyDataManager.dirtyDataMonthlyCheck();
    isHaveDirtyData();
    refreshDashboard();
  }

  @Override
  protected void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }

  protected final InternetCheckListener validateConnectionListener = internet -> {
    if (!internet && !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      ToastUtil.show(R.string.message_wipe_no_connection);
    } else {
      WarningDialogFragment wipeDataDialog = warningDialogFragmentBuilder.build(buildWipeDialogDelegate(),
          R.string.message_warning_wipe_data,
          R.string.btn_positive,
          R.string.btn_negative);
      getSupportFragmentManager().beginTransaction().add(wipeDataDialog, "WipeDataWarning").commitNow();
    }
  };

  private final SingleClickButtonListener singleClickButtonListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      if (isHaveDirtyData()) {
        return;
      }
      switch (v.getId()) {
        case R.id.btn_stock_card:
          startActivity(StockCardListActivity.class);
          break;
        case R.id.btn_inventory:
          Intent inventoryIntent = new Intent(HomeActivity.this, PhysicalInventoryActivity.class);
          startActivity(inventoryIntent);
          break;
        case R.id.btn_requisitions:
          Intent reportIntent = new Intent(HomeActivity.this, ReportListActivity.class);
          startActivity(reportIntent);
          break;
        case R.id.btn_kits:
          startActivity(KitStockCardListActivity.class);
          break;
        case R.id.btn_issue_voucher:
          if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_pod)) {
            startActivity(new Intent(HomeActivity.this, IssueVoucherListActivity.class));
          }
          break;
        default:
          // do nothing
      }
    }
  };

  private void setSyncedTime() {
    if (!sharedPreferenceMgr.shouldSyncLastYearStockData() && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
      syncTimeView.showLastSyncTime();
    } else if (!TextUtils.isEmpty(sharedPreferenceMgr.getStockMovementSyncError())) {
      syncTimeView.setSyncedMovementError(sharedPreferenceMgr.getStockMovementSyncError());
    } else {
      syncTimeView.setSyncStockCardLastYearText();
    }
  }

  private void exportDB() {
    int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      exportDBHavePermission();
    } else {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("get permssion");
        alertBuilder.setMessage("storage permssion");
        alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> ActivityCompat
            .requestPermissions(HomeActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE));
        AlertDialog alert = alertBuilder.create();
        alert.show();
        Log.e("", "permission denied, show dialog");
      } else {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
      }
    }
  }

  private void exportDBHavePermission() {
    File currentDB = new File(Environment.getDataDirectory(),
        EXPORT_DATA_PARENT_DIR + LMISApp.getContext().getApplicationContext().getPackageName()
            + "//databases//lmis_db");
    File currentXML = new File(Environment.getDataDirectory(),
        EXPORT_DATA_PARENT_DIR + LMISApp.getContext().getApplicationContext().getPackageName()
            + "//shared_prefs//LMISPreference.xml");
    File tempBackup = new File(Environment.getDataDirectory(),
        EXPORT_DATA_PARENT_DIR + LMISApp.getContext().getApplicationContext().getPackageName()
            + "//databases//lmis_copy");
    File currentXMLBackup = new File(Environment.getDataDirectory(),
        EXPORT_DATA_PARENT_DIR + LMISApp.getContext().getApplicationContext().getPackageName()
            + "//shared_prefs//LMISPreferenceBackup.xml");
    File externalBackup = new File(Environment.getExternalStorageDirectory(), "lmis_backup");
    File xmlExternalBackup = new File(Environment.getExternalStorageDirectory(), "LMISPreferenceBackup.xml");
    try {
      FileUtil.copy(currentDB, tempBackup);
      FileUtil.copy(currentXML, currentXMLBackup);
      ExportSqliteOpenHelper.removePrivateUserInfo(this);
      FileUtil.copy(tempBackup, externalBackup);
      FileUtil.copy(currentXMLBackup, xmlExternalBackup);
      ToastUtil.show(Html.fromHtml(getString(R.string.msg_export_data_success, externalBackup.getPath())));
    } catch (Exception e) {
      new LMISException(e, "HomeActivity.exportDB").reportToFabric();
      ToastUtil.show(e.getMessage());
    } finally {
      if (tempBackup.canRead()) {
        FileUtil.deleteDir(tempBackup);
      }
      if (currentXMLBackup.canRead()) {
        FileUtil.deleteDir(currentXMLBackup);
      }
    }
  }

  private void alertWipeData() {
    new InternetCheck().check(validateConnectionListener);
  }

  private WarningDialogFragment.DialogDelegate buildWipeDialogDelegate() {
    return () -> {
      setRestartIntent();
      LMISApp.getInstance().wipeAppData();
    };
  }

  private void setRestartIntent() {
    int requestCode = 100;
    int startAppInterval = 500;
    User currentUser = UserInfoMgr.getInstance().getUser();
    Intent intent = new Intent(this, LoginActivity.class);
    intent.putExtra(Constants.PARAM_USERNAME, currentUser.getUsername());
    intent.putExtra(Constants.PARAM_PASSWORD, currentUser.getPassword());
    PendingIntent mPendingIntent = PendingIntent
        .getActivity(this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (mgr != null) {
      mgr.set(AlarmManager.RTC, LMISApp.getInstance().getCurrentTimeMillis() + startAppInterval, mPendingIntent);
    }
  }

  private boolean isHaveDirtyData() {
    if (!CollectionUtils.isEmpty(sharedPreferenceMgr.getDeletedProduct())
        || !CollectionUtils.isEmpty(sharedPreferenceMgr.getDeletedMovementItems())) {
      showDeletedWarningDialog(dirtyDataManager::deleteAndReset);
      return true;
    }
    return false;
  }

  private void refreshDashboard() {
    if (sharedPreferenceMgr.shouldSyncLastYearStockData() && sharedPreferenceMgr.isSyncingLastYearStockCards()) {
      if (isCmmCalculating) {
        dvProductDashboard.showCalculating();
      } else {
        dvProductDashboard.showLoading(syncedCount, Constants.STOCK_CARD_MAX_SYNC_MONTH);
      }
    } else {
      homePresenter.getDashboardData();
    }
  }
}
