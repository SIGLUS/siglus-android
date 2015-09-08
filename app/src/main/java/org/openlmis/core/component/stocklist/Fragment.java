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

package org.openlmis.core.component.stocklist;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.activity.StockMovementActivity;
import org.openlmis.core.component.Component;

import java.util.List;

import roboguice.inject.InjectView;

public class Fragment extends Component implements Presenter.StockCardListView, AdapterView.OnItemSelectedListener {

    public static final int REQUEST_CODE_CHANGE = 1;

    @InjectView(R.id.sort_spinner)
    Spinner sortSpinner;

    @InjectView(R.id.products_list)
    RecyclerView stockCardRecycleView;

    @Inject
    Presenter presenter;

    Adapter mAdapter;
    String className;

    View contentView;
    private List<StockCard> stockCards;
    int currentPostion;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        contentView = inflater.inflate(R.layout.fragment_stockcard_list, container, false);
        presenter.attachView(this);
        initView();
        return contentView;
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ComponentAttr);
        try {
            className = a.getString(R.styleable.ComponentAttr_className);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }
    }

    private void initView() {
        if (className == null) {
            className = StockMovementActivity.class.getName();
        }

        sortSpinner = (Spinner) contentView.findViewById(R.id.sort_spinner);
        stockCardRecycleView = (RecyclerView) contentView.findViewById(R.id.products_list);
        stockCards = presenter.getStockCards();
        mAdapter = new Adapter(this, presenter, stockCards, className);

        initProductList();
        initSortSpinner();
    }


    private void initProductList() {
        stockCardRecycleView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(contentView.getContext());
        stockCardRecycleView.setLayoutManager(mLayoutManager);
        stockCardRecycleView.setAdapter(mAdapter);

        presenter.loadStockCards();
    }

    private void initSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(contentView.getContext(),
                R.array.sort_items_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentPostion = position;
        switch (position) {
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
    public void loading() {

    }

    @Override
    public void loaded() {

    }

    @Override
    public void refresh() {
        stockCards = presenter.getStockCards();
        mAdapter = new Adapter(this, presenter, stockCards, className);
        stockCardRecycleView.setAdapter(mAdapter);
        onItemSelected(sortSpinner, null, currentPostion, 0L);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHANGE) {
            presenter.refreshStockCards();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSearch(String query) {
        mAdapter.filter(query);
    }
}
