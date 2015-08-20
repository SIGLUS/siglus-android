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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockCardListPresenter;
import org.openlmis.core.view.adapter.StockCardListAdapter;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stockcard_list)
public class StockCardListActivity extends BaseActivity implements StockCardListPresenter.StockCardListView, AdapterView.OnItemSelectedListener{

    @InjectView(R.id.sort_spinner)
    Spinner sortSpinner;

    @InjectView(R.id.products_list)
    RecyclerView stockCardRecycleView;

    @Inject
    StockCardListPresenter presenter;

    StockCardListAdapter mAdapter;

    List<StockCard> stockCardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter.attachView(this);
        stockCardList = presenter.loadStockCards();
        mAdapter = new StockCardListAdapter(presenter,stockCardList);

        initUI();
    }

    private  void initUI(){
        stockCardRecycleView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        stockCardRecycleView.setLayoutManager(mLayoutManager);
        stockCardRecycleView.setAdapter(mAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_items_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                mAdapter.sortByName(true);
                break;
            case 1:
                mAdapter.sortByName(false);
                break;
            case 2:
                mAdapter.sortBySOH(false);
                break;
            case 3:
                mAdapter.sortBySOH(true);
                break;
            default:
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public Presenter getPresenter() {
        return  presenter;
    }

    public void filterStockCard(String query) {
        mAdapter.filter(query);
    }

    @Override
    public boolean onSearchStart(String query) {
        sortSpinner.setSelection(0);
        filterStockCard(query);
        return true;
    }

    @Override
    public boolean onSearchClosed() {
        sortSpinner.setSelection(0);
        filterStockCard(StringUtils.EMPTY);
        return false;
    }
}
