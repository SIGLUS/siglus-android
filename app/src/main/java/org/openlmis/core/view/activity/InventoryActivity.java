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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.common.Constants;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.FeatureToggle;
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

    public static final String PARAM_IS_PHYSICAL_INVENTORY = "isInitialInventory";
    public static final String PARAM_IS_ADD_NEW_DRUG = "isAddNewDrug";

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    @InjectView(R.id.btn_done)
    public Button btnDone;


    @Inject
    InventoryPresenter presenter;

    LinearLayoutManager mLayoutManager;
    InventoryListAdapter mAdapter;

    boolean isPhysicalInventory = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean createOptionsMenu = super.onCreateOptionsMenu(menu);

        menu.findItem(R.id.action_add_new_drug).setVisible(false);

        return createOptionsMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayoutManager = new LinearLayoutManager(this);
        productListRecycleView.setLayoutManager(mLayoutManager);
        isPhysicalInventory = getIntent().getBooleanExtra(PARAM_IS_PHYSICAL_INVENTORY, false);

        if (isPhysicalInventory) {
            initPhysicalInventoryUI();
        } else {
            if (isAddNewDrug()) {
                setTitle(getResources().getString(R.string.title_add_new_drug));
            }
            initInitialInventoryUI();

            if (getSupportActionBar() != null && !isAddNewDrug()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private boolean isAddNewDrug() {
        return getIntent().getBooleanExtra(PARAM_IS_ADD_NEW_DRUG, false);
    }

    private void initPhysicalInventoryUI() {
        final List<StockCardViewModel> list = new ArrayList<>();
        mAdapter = new PhysicalInventoryAdapter(this, list);
        productListRecycleView.setAdapter(mAdapter);

        setTitle(getResources().getString(R.string.title_physical_inventory));

        loading();
        presenter.loadStockCardList().subscribe(new Subscriber<List<StockCardViewModel>>() {
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
                loaded();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doPhysicalInventory(((PhysicalInventoryAdapter) mAdapter).getData());
            }
        });
    }


    private void initInitialInventoryUI() {
        final List<StockCardViewModel> list = new ArrayList<>();
        mAdapter = new InitialInventoryAdapter(this, list);
        productListRecycleView.setAdapter(mAdapter);

        loading();
        presenter.loadMasterProductList().subscribe(new Subscriber<List<StockCardViewModel>>() {
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
                loaded();
            }
        });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doInitialInventory(((InitialInventoryAdapter) mAdapter).getData());
            }
        });

    }

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
        saveBoolean(Constants.KEY_INIT_INVENTORY, false);

        Intent intent = getIntent();
        intent.setClass(this, HomeActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
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

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, InventoryActivity.class);
        return intent;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (FeatureToggle.isOpen(R.bool.time_out_235)) {
            if (!isPhysicalInventory && !isAddNewDrug() && keyCode == KeyEvent.KEYCODE_BACK) {
                moveTaskToBack(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
