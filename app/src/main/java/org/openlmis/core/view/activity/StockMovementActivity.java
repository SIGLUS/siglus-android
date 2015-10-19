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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.presenter.StockMovementPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;

import java.text.ParseException;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movement)
public class StockMovementActivity extends BaseActivity implements StockMovementPresenter.StockMovementView {

    @InjectView(R.id.list_stock_movement)
    ListView stockMovementList;

    @Inject
    LayoutInflater layoutInflater;

    @InjectView(R.id.btn_complete)
    View btnComplete;

    @InjectView(R.id.btn_cancel)
    Button btnCancel;

    @InjectView(R.id.vg_expire_date_container)
    ViewGroup expireDateContainer;

    @InjectPresenter(StockMovementPresenter.class)
    StockMovementPresenter presenter;

    private long stockId;
    private StockMovementAdapter stockMovementAdapter;

    private String stockName;
    private View buttonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockId = getIntent().getLongExtra("stockCardId", 0);
        stockName = getIntent().getStringExtra("stockName");
        try {
            presenter.setStockCard(stockId);
        } catch (LMISException e) {
            ToastUtil.show(R.string.msg_db_error);
            finish();
        }

        initUI();
    }

    private void initUI() {
        displayExpireDate();

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

    private void displayExpireDate() {
        View addView = expireDateContainer.getChildAt(expireDateContainer.getChildCount() - 1);
        expireDateContainer.removeAllViews();
        expireDateContainer.addView(addView);
        for (String date : presenter.getStockCardExpireDates()) {
            initExpireDateView(date, expireDateContainer);
        }
    }

    private void initExpireDateView(String date, final ViewGroup expireDateContainer) {
        try {
            final String expireDate = DateUtil.convertDate(date, DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
            addExpireDateView(expireDate, expireDateContainer);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private ViewGroup addExpireDateView(final String expireDate, final ViewGroup expireDateContainer) {
        ViewGroup expireDateView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_expire_date, null);
        TextView tvExpireDate = (TextView) expireDateView.findViewById(R.id.tx_expire_data);
        tvExpireDate.setText(expireDate);
        expireDateContainer.addView(expireDateView, expireDateContainer.getChildCount() - 1);
        return expireDateView;
    }

    @Override
    public void showErrorAlert(String msg) {
        showMessage(msg);
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
    protected void onDestroy() {
        super.onDestroy();
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
        if (buttonView.getVisibility() == View.VISIBLE) {
            return;
        }
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
