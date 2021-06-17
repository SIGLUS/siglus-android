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

package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.inject.Inject;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.openlmis.core.service.SyncDownManager;
import org.openlmis.core.service.SyncUpManager;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.BaseActivity;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class SyncTimeView extends LinearLayout implements View.OnClickListener {

  @InjectView(R.id.pb_sync_data)
  ProgressBar progressBar;

  @InjectView(R.id.tx_sync_time)
  TextView txSyncTime;

  @InjectView(R.id.iv_sync_time_icon)
  ImageView ivSyncTimeIcon;

  @Inject
  SyncErrorsPresenter syncErrorsPresenter;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  protected Context context;
  protected long rnrLastSyncTime;
  protected long stockLastSyncTime;
  protected boolean hasRnrSyncError;
  protected boolean hasStockCardSyncError;

  public SyncTimeView(Context context) {
    super(context);
    init(context);
  }

  public SyncTimeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    LayoutInflater.from(context).inflate(R.layout.view_sync_time, this);
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    txSyncTime.setOnClickListener(this);
  }

  public void showLastSyncTime() {
    if (SyncUpManager.isSyncing || SyncDownManager.isSyncing) {
      showSyncProgressBarAndHideIcon();
    } else {
      hideSyncProgressBarAndShowIcon();
    }

    rnrLastSyncTime = sharedPreferenceMgr.getRnrLastSyncTime();
    stockLastSyncTime = sharedPreferenceMgr.getStockLastSyncTime();

    if (isNeverSyncSuccessful()) {
      if (hasSyncFailed()) {
        txSyncTime.setText(LMISApp.getContext().getString(R.string.initial_sync_failed));
      }
      return;
    }

    txSyncTime.setText(updateSyncTimeViewUI());

    if (sharedPreferenceMgr.isStockCardLastYearSyncError()) {
      setSyncStockCardLastYearError();
    }
  }

  private String updateSyncTimeViewUI() {
    long syncTimeInterval = getSyncTimeInterval(rnrLastSyncTime, stockLastSyncTime);

    String syncTimeIntervalWithUnit;
    if (syncTimeInterval < DateUtil.MILLISECONDS_HOUR) {
      int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_MINUTE);
      syncTimeIntervalWithUnit = getResources()
          .getQuantityString(R.plurals.minute_unit, quantity, quantity);
      ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_green);
    } else if (syncTimeInterval < DateUtil.MILLISECONDS_DAY) {
      int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_HOUR);
      syncTimeIntervalWithUnit = getResources()
          .getQuantityString(R.plurals.hour_unit, quantity, quantity);
      ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_green);
    } else if (syncTimeInterval < DateUtil.MILLISECONDS_DAY * 3) {
      int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_DAY);
      syncTimeIntervalWithUnit = getResources()
          .getQuantityString(R.plurals.day_unit, quantity, quantity);
      ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_yellow);
    } else {
      int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_DAY);
      syncTimeIntervalWithUnit = getResources()
          .getQuantityString(R.plurals.day_unit, quantity, quantity);
      ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_red);
    }

    String msg = getResources().getString(R.string.label_last_synced_ago, syncTimeIntervalWithUnit);
    return msg;
  }

  private boolean isNeverSyncSuccessful() {
    return rnrLastSyncTime == 0 && stockLastSyncTime == 0;
  }

  private boolean hasSyncFailed() {
    return syncErrorsPresenter.hasRnrSyncError() || syncErrorsPresenter.hasStockCardSyncError();
  }

  private long getSyncTimeInterval(long rnrLastSyncTime, long stockLastSyncTime) {
    long latestSyncTime;
    if (rnrLastSyncTime > stockLastSyncTime) {
      latestSyncTime = rnrLastSyncTime;
    } else {
      latestSyncTime = stockLastSyncTime;
    }
    return DateUtil.calculateTimeIntervalFromNow(latestSyncTime);
  }

  @Override
  public void onClick(View v) {
    if (!sharedPreferenceMgr.isStockCardLastYearSyncError() && !sharedPreferenceMgr
        .shouldSyncLastYearStockData()) {
      showLastSyncTime();
      popUpBottomSheet();
    }
  }

  private void popUpBottomSheet() {
    SyncDateBottomSheet syncDateBottomSheet = new SyncDateBottomSheet();
    syncDateBottomSheet
        .setArguments(SyncDateBottomSheet.getArgumentsToMe(rnrLastSyncTime, stockLastSyncTime));
    syncDateBottomSheet.show(((BaseActivity) context).getFragmentManager());
  }

  public void showSyncProgressBarAndHideIcon() {
    ivSyncTimeIcon.setVisibility(GONE);
    progressBar.setVisibility(VISIBLE);
  }

  private void hideSyncProgressBarAndShowIcon() {
    progressBar.setVisibility(GONE);
    ivSyncTimeIcon.setVisibility(VISIBLE);
  }

  public void setSyncStockCardLastYearText() {
    showSyncProgressBarAndHideIcon();
    txSyncTime.setText(R.string.last_year_stock_cards_sync);
  }

  public void setSyncStockCardLastYearError() {
    hideSyncProgressBarAndShowIcon();
    txSyncTime.setText(R.string.sync_stock_card_last_year_error);
    ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_red);
  }

  public void setSyncedMovementError(String error) {
    rnrLastSyncTime = sharedPreferenceMgr.getRnrLastSyncTime();
    stockLastSyncTime = sharedPreferenceMgr.getStockLastSyncTime();
    hideSyncProgressBarAndShowIcon();
    String msg = context
        .getString(R.string.sync_stock_movement_error, error, updateSyncTimeViewUI());
    txSyncTime.setText(msg);
    ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_red);
  }
}
