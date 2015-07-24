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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.widget.Button;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.InventoryListAdapter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_inventory)
public class InventoryActitivy extends BaseActivity {

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

    private void initUI(){
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        productListRecycleView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new InventoryListAdapter(this, presenter);
        productListRecycleView.setAdapter(mAdapter);
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
