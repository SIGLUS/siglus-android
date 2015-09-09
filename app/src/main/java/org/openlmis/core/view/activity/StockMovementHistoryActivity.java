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

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.openlmis.core.R;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockMovementHistoryPresenter;
import org.openlmis.core.presenter.StockMovementPresenter;
import org.openlmis.core.view.adapter.StockMovementHistoryAdapter;
import org.openlmis.core.view.fragment.RetainedFragment;

import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movement_history)
public class StockMovementHistoryActivity extends BaseActivity implements StockMovementPresenter.StockMovementView {

    @InjectView(R.id.list)
    ListView historyListView;

    StockMovementHistoryPresenter presenter;
    private RetainedFragment dataFragment;
    private long startIndex = 0;
    private BaseAdapter adapter;
    private long stockId;

    @Override
    public Presenter getPresenter() {
        initPresenter();
        return presenter;
    }

    private void initPresenter() {
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("RetainedFragment");

        if (dataFragment == null) {
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "RetainedFragment").commit();
            presenter = RoboGuice.getInjector(getApplicationContext()).getInstance(StockMovementHistoryPresenter.class);
            dataFragment.putData("presenter", presenter);
        } else {
            presenter = (StockMovementHistoryPresenter) dataFragment.getData("presenter");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dataFragment.getData("startIndex") != null) {
            startIndex = (long) dataFragment.getData("startIndex");
        }

        stockId = getIntent().getLongExtra("stockCardId", 0);
        presenter.setStockCardId(stockId);
        initUI();
    }

    private void initUI() {
        adapter = new StockMovementHistoryAdapter(this, presenter.getStockMovementModelList());
        historyListView.setAdapter(adapter);

        presenter.loadStockMovementViewModels(startIndex);
        startIndex += StockMovementHistoryPresenter.MAXROWS;
    }

    @Override
    protected void onDestroy() {
        dataFragment.putData("presenter", presenter);
        dataFragment.putData("startIndex", startIndex);
        super.onDestroy();
    }

    @Override
    public void showErrorAlert(String msg) {

    }

    @Override
    public void refreshStockMovement() {
        adapter.notifyDataSetChanged();
    }

    public static Intent getIntentToMe(Context context, long stockCardId) {
        Intent intent = new Intent(context, StockMovementHistoryActivity.class);
        intent.putExtra("stockCardId", stockCardId);
        return intent;
    }


}
