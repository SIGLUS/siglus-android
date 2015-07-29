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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.common.Constants;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_inventory)
public class InventoryActitivy extends BaseActivity implements InventoryPresenter.InventoryView{

    @InjectView(R.id.search_view)
    SearchView searchView;

    @InjectView(R.id.products_list)
    RecyclerView productListRecycleView;

    @InjectView(R.id.btn_done)
    Button btnDone;


    @Inject
    InventoryPresenter presenter;

    LinearLayoutManager mLayoutManager;
    private InventoryListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI() {
        mLayoutManager = new LinearLayoutManager(this);
        productListRecycleView.setLayoutManager(mLayoutManager);

        mAdapter = new InventoryListAdapter(this, presenter.loadMasterProductList());
        productListRecycleView.setAdapter(mAdapter);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position;
                if ((position = checkInventory()) == -1) {
                    presenter.initStockCard(mAdapter.getInventoryList());
                    goToMainPage();
                } else {
                    productListRecycleView.scrollToPosition(position);
                    Toast.makeText(InventoryActitivy.this, R.string.msg_inventory_check_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goToMainPage() {
        saveBoolean(Constants.KEY_INIT_INVENTORY, false);

        Intent intent = getIntent();
        intent.setClass(this, HomeActivity.class);
        startActivity(intent);
        this.finish();
    }

    public int checkInventory() {
        List<InventoryViewModel> list = mAdapter.getInventoryList();

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                InventoryViewModel inventory = list.get(i);
                if (inventory.isChecked()) {
                    if (StringUtils.isEmpty(inventory.getQuantity())) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }


    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
