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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockMovementPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movement)
public class StockMovementActivity extends BaseActivity implements StockMovementPresenter.StockMovementView{


    @InjectView(R.id.list_stock_movement)
    ListView stockMovementList;

    @Inject
    LayoutInflater layoutInflater;

    @InjectView(R.id.btn_save)
    View btnSave;

    @InjectView(R.id.btn_complete)
    Button btnCancel;


    @Inject
    StockMovementPresenter presenter;
    private long stockId;
    private StockMovementAdapter stockMovementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockId = getIntent().getLongExtra("stockCardId", 0);
        try {
            presenter.setStockCard(stockId);
        }catch (LMISException e){
            ToastUtil.show(R.string.msg_db_error);
            finish();
        }
        initUI();
    }

    private void initUI(){
        stockMovementAdapter = new StockMovementAdapter(this, presenter);
        View headerView = layoutInflater.inflate(R.layout.item_stock_movement_header, stockMovementList, false);

        stockMovementList.addHeaderView(headerView);
        stockMovementList.setAdapter(stockMovementAdapter);

        btnCancel.setText(getResources().getString(R.string.btn_cancel));


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.submitStockMovement(stockMovementAdapter.getEditableStockMovement());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockMovementAdapter.cancelStockMovement();
            }
        });

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
    public void refreshStockMovement(StockMovementViewModel viewModel) {
        stockMovementAdapter.addLine(viewModel);
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
