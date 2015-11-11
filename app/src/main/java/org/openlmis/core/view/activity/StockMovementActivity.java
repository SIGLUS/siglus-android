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

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.presenter.StockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movement)
public class StockMovementActivity extends BaseActivity implements StockMovementPresenter.StockMovementView {

    @InjectView(R.id.list_stock_movement)
    ListView stockMovementList;

    @InjectView(R.id.btn_complete)
    View btnComplete;

    @InjectView(R.id.btn_cancel)
    Button btnCancel;

    @InjectView(R.id.vg_expire_date_container)
    ExpireDateViewGroup expireDateViewGroup;

    @InjectPresenter(StockMovementPresenter.class)
    StockMovementPresenter presenter;

    @Inject
    LayoutInflater layoutInflater;

    private long stockId;
    private String stockName;
    private View buttonView;
    private StockMovementAdapter stockMovementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0);
        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
        try {
            presenter.setStockCard(stockId);
        } catch (LMISException e) {
            ToastUtil.show(R.string.msg_db_error);
            finish();
        }

        initUI();
    }

    private void initUI() {
        expireDateViewGroup.initExpireDateViewGroup(new StockCardViewModel(presenter.getStockCard()),true);

        buttonView = findViewById(R.id.action_panel);
        buttonView.setVisibility(View.GONE);

        stockMovementAdapter = new StockMovementAdapter(this, presenter);
        View headerView = layoutInflater.inflate(R.layout.item_stock_movement_header, stockMovementList, false);

        stockMovementList.addHeaderView(headerView);
        stockMovementList.setAdapter(stockMovementAdapter);

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.submitStockMovement(stockMovementAdapter.getEditableStockMovement());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockMovementAdapter.cancelStockMovement();

                deactivatedStockDraft();
            }
        });

        loading();
        presenter.loadStockMovementViewModels();
    }

    public void deactivatedStockDraft() {
        buttonView.setVisibility(View.GONE);
        stockMovementAdapter.cleanHighLight();
    }

    @Override
    public void showErrorAlert(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void refreshStockMovement() {
        stockMovementAdapter.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stock_movement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                startActivity(StockMovementHistoryActivity.getIntentToMe(this, stockId, stockName));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showBottomBtn() {
        if (buttonView.getVisibility() != View.VISIBLE) {
            buttonView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_from_bottom_in);
            buttonView.startAnimation(animation);
            stockMovementList.post(new Runnable() {
                @Override
                public void run() {
                    stockMovementList.setSelection(stockMovementList.getCount() - 1);
                }
            });
        }
    }
}
