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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;

import java.util.Date;

import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main_page)
public class HomeActivity extends BaseActivity {

    @InjectView(R.id.btn_stock_card)
    Button btnStockCard;

    @InjectView(R.id.btn_inventory)
    Button btnInventory;

    @InjectView(R.id.btn_mmia)
    Button btnMMIA;

    @InjectView(R.id.btn_requisition)
    Button btnRequisition;

    @InjectView(R.id.btn_sync_data)
    Button btnSyncData;

    @InjectView(R.id.tx_last_synced_rnrform)
    TextView txLastSyncedRnrForm;

    @InjectView(R.id.tx_last_synced_stockcard)
    TextView txLastSyncedStockCard;

    @InjectView(R.id.btn_mmia_list)
    Button btnMMIAList;

    @InjectView(R.id.btn_via_list)
    Button btnVIAList;

    @InjectResource(R.integer.back_twice_interval)
    int BACK_TWICE_INTERVAL;

    @Inject
    SyncManager syncManager;

    private boolean exitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public void onClickStockCard(View view) {
        startActivity(StockCardListActivity.class, false);
    }

    public void onClickInventory(View view) {
        Intent intent = new Intent(HomeActivity.this, InventoryActivity.class);
        intent.putExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, true);
        startActivity(intent);
    }

    public void onClickMMIA(View view) {
        startActivity(MMIAActivity.class, false);
    }

    public void onClickRequisition(View view) {
        startActivity(RequisitionActivity.class, false);
    }

    public void onClickSyncData(View view) {
        syncManager.requestSyncImmediately();
    }


    public void onClickMMIAHistory(View view) {
        Intent intent = new Intent(this, RnRFormListActivity.class);
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, MMIARepository.MMIA_PROGRAM_CODE);
        startActivity(intent);
    }

    public void onClickVIAHistory(View view) {
        Intent intent = new Intent(this, RnRFormListActivity.class);
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, VIARepository.VIA_PROGRAM_CODE);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRnrFormLastSyncedTime();
        showStockCardLastSyncedTime();
    }

    private void showRnrFormLastSyncedTime() {
        long lastSyncedTimestamp = getPreferences().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
        if (lastSyncedTimestamp == 0) {
            return;
        }

        long currentTimestamp = new Date().getTime();

        long diff = currentTimestamp - lastSyncedTimestamp;

        if (diff < DateUtil.MILLISECONDS_HOUR) {
            txLastSyncedRnrForm.setText(getResources().getString(R.string.label_rnr_form_last_synced_mins_ago, (diff / DateUtil.MILLISECONDS_MINUTE)));
        } else if (diff < DateUtil.MILLISECONDS_DAY) {
            txLastSyncedRnrForm.setText(getResources().getString(R.string.label_rnr_form_last_synced_hours_ago, (diff / DateUtil.MILLISECONDS_HOUR)));
        } else {
            txLastSyncedRnrForm.setText(getResources().getString(R.string.label_rnr_form_last_synced_days_ago, (diff / DateUtil.MILLISECONDS_DAY)));
        }
    }

    private void showStockCardLastSyncedTime() {
        long lastSyncedTimestamp = getPreferences().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, 0);
        if (lastSyncedTimestamp == 0) {
            return;
        }

        long currentTimestamp = new Date().getTime();

        long diff = currentTimestamp - lastSyncedTimestamp;

        if (diff < DateUtil.MILLISECONDS_HOUR) {
            txLastSyncedStockCard.setText(getResources().getString(R.string.label_stock_card_last_synced_mins_ago, (diff / DateUtil.MILLISECONDS_MINUTE)));
        } else if (diff < DateUtil.MILLISECONDS_DAY) {
            txLastSyncedStockCard.setText(getResources().getString(R.string.label_stock_card_last_synced_hours_ago, (diff / DateUtil.MILLISECONDS_HOUR)));
        } else {
            txLastSyncedStockCard.setText(getResources().getString(R.string.label_stock_card_last_synced_days_ago, (diff / DateUtil.MILLISECONDS_DAY)));
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

}
