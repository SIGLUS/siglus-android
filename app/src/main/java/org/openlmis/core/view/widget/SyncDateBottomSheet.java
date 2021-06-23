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

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.fragment.BaseDialogFragment;
import roboguice.inject.InjectView;

public class SyncDateBottomSheet extends BaseDialogFragment {

  @InjectView(R.id.tv_last_synced_rnr_form)
  private TextView txRnrFormSyncTime;

  @InjectView(R.id.tx_last_synced_stockcard)
  private TextView txStockCardSyncTime;

  @InjectView(R.id.iv_rnr_error)
  ImageView ivRnRError;

  @InjectView(R.id.iv_stockcard_error)
  ImageView ivStockcardError;

  @Inject
  private SyncErrorsPresenter presenter;

  public static final String RNR_SYNC_TIME = "rnrFormSyncTime";
  public static final String STOCK_SYNC_TIME = "stockCardSyncTime";

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View inflate = inflater.inflate(R.layout.dialog_sync_date_bottom_sheet, container, false);
    return inflate;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initUI();
  }

  @Override
  public void onStart() {
    super.onStart();
    setDialogAttributes();
  }

  private void setDialogAttributes() {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    Window window = getDialog().getWindow();
    if (window != null) {
      params.copyFrom(getDialog().getWindow().getAttributes());
    }
    params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels
        * 0.95);
    params.height = (int) (getDialog().getContext().getResources().getDisplayMetrics().heightPixels
        * 0.15);
    getDialog().getWindow().setAttributes(params);
    getDialog().getWindow().setGravity(Gravity.BOTTOM);
  }

  private void initUI() {
    Bundle arguments = getArguments();
    if (arguments == null) {
      return;
    }
    txRnrFormSyncTime.setText(formatRnrLastSyncTime(arguments.getLong(RNR_SYNC_TIME)));
    txStockCardSyncTime.setText(formatStockCardLastSyncTime(arguments.getLong(STOCK_SYNC_TIME)));

    if (presenter.hasRnrSyncError()) {
      ivRnRError.setVisibility(View.VISIBLE);
    }

    if (presenter.hasStockCardSyncError()) {
      ivStockcardError.setVisibility(View.VISIBLE);
    }
  }

  public void show(@NonNull FragmentManager fragmentManager) {
    //avoid the duplicate Dialog
    if (fragmentManager.findFragmentByTag("sync_date_bottom_sheet") != null) {
      return;
    }
    super.show(fragmentManager, "sync_date_bottom_sheet");
  }


  public static Bundle getArgumentsToMe(long rnrLastSyncTime, long stockLastSyncTime) {
    Bundle bundle = new Bundle();
    bundle.putLong(SyncDateBottomSheet.RNR_SYNC_TIME, rnrLastSyncTime);
    bundle.putLong(SyncDateBottomSheet.STOCK_SYNC_TIME, stockLastSyncTime);
    return bundle;
  }

  //This method will move static and change to private after remove home page update feature toggle
  private String formatLastSyncTime(long syncedTimestamp, int syncTimeStringRId) {
    long diff = DateUtil.calculateTimeIntervalFromNow(syncedTimestamp);
    String syncTimeIntervalWithUnit;
    if (diff < DateUtil.MILLISECONDS_HOUR) {
      int quantity = (int) (diff / DateUtil.MILLISECONDS_MINUTE);
      syncTimeIntervalWithUnit = LMISApp.getContext().getResources()
          .getQuantityString(R.plurals.minute_unit, quantity, quantity);
    } else if (diff < DateUtil.MILLISECONDS_DAY) {
      int quantity = (int) (diff / DateUtil.MILLISECONDS_HOUR);
      syncTimeIntervalWithUnit = LMISApp.getContext().getResources()
          .getQuantityString(R.plurals.hour_unit, quantity, quantity);
    } else {
      int quantity = (int) (diff / DateUtil.MILLISECONDS_DAY);
      syncTimeIntervalWithUnit = LMISApp.getContext().getResources()
          .getQuantityString(R.plurals.day_unit, quantity, quantity);
    }
    return LMISApp.getContext().getResources()
        .getString(syncTimeStringRId, syncTimeIntervalWithUnit);
  }

  //This method will change to private after remove home page update feature toggle
  public String formatRnrLastSyncTime(long lastRnrSyncedTimestamp) {
    if (lastRnrSyncedTimestamp == 0) {
      return presenter.hasRnrSyncError() ? LMISApp.getContext()
          .getString(R.string.initial_rnr_sync_failed) : StringUtils.EMPTY;
    }
    return formatLastSyncTime(lastRnrSyncedTimestamp, R.string.label_rnr_form_last_synced_time_ago);
  }

  //This method will change to private after remove home page update feature toggle
  public String formatStockCardLastSyncTime(long lastStockCardSyncedTimestamp) {
    if (lastStockCardSyncedTimestamp == 0) {
      return presenter.hasStockCardSyncError() ? LMISApp.getContext()
          .getString(R.string.initial_stock_movement_sync_failed) : StringUtils.EMPTY;
    }
    return formatLastSyncTime(lastStockCardSyncedTimestamp,
        R.string.label_stock_card_last_synced_time_ago);
  }
}
