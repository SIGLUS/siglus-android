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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;

import roboguice.inject.InjectView;

public abstract class InventoryActivity extends SearchBarActivity implements InventoryPresenter.InventoryView, SimpleDialogFragment.MsgDialogCallBack {

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

    protected InventoryListAdapter mAdapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.InventoryScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        initUI();

        trackInventoryEvent(TrackerActions.SelectInventory);
    }

    public void initUI() {
        loading();
    }

    protected void trackInventoryEvent(TrackerActions action) {
        LMISApp.getInstance().trackEvent(TrackerCategories.Inventory, action);
    }

    @Override
    public abstract void goToParentPage();

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        return false;
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

    protected void setTotal(int total) {
        tvTotal.setText(getString(R.string.label_total, total));
    }

    @Override
    public void positiveClick(String tag) {
        this.finish();
    }

    @Override
    public void negativeClick(String tag) {

    }
}
