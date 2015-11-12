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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.InitialInventoryAdapter;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.adapter.PhysicalInventoryAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@ContentView(R.layout.activity_inventory)
public class InventoryActivity extends BaseActivity implements InventoryPresenter.InventoryView {

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    @InjectView(R.id.tv_total)
    public TextView tvTotal;

    @InjectView(R.id.action_panel)
    public ViewGroup bottomBtn;

    @InjectView(R.id.btn_complete)
    public Button btnDone;

    @InjectView(R.id.btn_save)
    public View btnSave;

    @InjectPresenter(InventoryPresenter.class)
    InventoryPresenter presenter;

    private InventoryListAdapter mAdapter;
    private boolean isPhysicalInventory;
    private boolean isAddNewDrug;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_add_new_drug).setVisible(false);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPhysicalInventory = getIntent().getBooleanExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, false);
        isAddNewDrug = getIntent().getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));

        if (isPhysicalInventory) {
            initPhysicalInventoryUI();
        } else {
            initInitialInventoryUI();
        }
    }

    private void initPhysicalInventoryUI() {
        setTitle(getResources().getString(R.string.title_physical_inventory));

        final List<StockCardViewModel> list = new ArrayList<>();
        ((ViewGroup) bottomBtn.getParent()).removeView(bottomBtn);
        mAdapter = new PhysicalInventoryAdapter(this, list, bottomBtn);
        productListRecycleView.setAdapter(mAdapter);

        loading();
        presenter.loadStockCardList().subscribe(stockCardSubscriber);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.savePhysicalInventory(((PhysicalInventoryAdapter) mAdapter).getData());
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doPhysicalInventory(((PhysicalInventoryAdapter) mAdapter).getData());
            }
        });
    }

    protected Subscriber<List<StockCardViewModel>> stockCardSubscriber = new Subscriber<List<StockCardViewModel>>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            ToastUtil.show(e.getMessage());
            loaded();
        }

        @Override
        public void onNext(List<StockCardViewModel> stockCardViewModels) {
            mAdapter.refreshList(stockCardViewModels);
            setTotal(stockCardViewModels.size());
            loaded();
        }
    };

    private void initInitialInventoryUI() {
        btnSave.setVisibility(View.GONE);
        if (isAddNewDrug) {
            setTitle(getResources().getString(R.string.title_add_new_drug));
        } else if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        mAdapter = new InitialInventoryAdapter(this, new ArrayList<StockCardViewModel>());
        productListRecycleView.setAdapter(mAdapter);

        loading();
        presenter.loadMasterProductList().subscribe(loadMasterSubscriber);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doInitialInventory(((InitialInventoryAdapter) mAdapter).getData());
            }
        });
    }

    protected Subscriber<List<StockCardViewModel>> loadMasterSubscriber = new Subscriber<List<StockCardViewModel>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            loaded();
            ToastUtil.show(e.getMessage());
        }

        @Override
        public void onNext(List<StockCardViewModel> stockCardViewModels) {
            mAdapter.refreshList(stockCardViewModels);
            setTotal(stockCardViewModels.size());
            loaded();
        }
    };

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        return false;
    }

    @Override
    public boolean onSearchClosed() {
        mAdapter.filter(StringUtils.EMPTY);
        return false;
    }

    public void goToMainPage() {
        saveBoolean(SharedPreferenceMgr.KEY_INIT_INVENTORY, false);
        startActivity(isAddNewDrug ? StockCardListActivity.getIntentToMe(this) : HomeActivity.getIntentToMe(this));
        this.finish();
    }

    @Override
    public void showErrorMessage(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    public boolean validateInventory() {
        int position = mAdapter.validateAll();
        if (position >= 0) {
            clearSearch();

            productListRecycleView.scrollToPosition(position);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isPhysicalInventory || isAddNewDrug) {
            super.onBackPressed();
        } else {
            ToastUtil.show(R.string.msg_save_before_exit);
        }
    }

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, InventoryActivity.class);
    }

    private void setTotal(int total) {
        tvTotal.setText(getString(R.string.label_total, total));
    }
}
