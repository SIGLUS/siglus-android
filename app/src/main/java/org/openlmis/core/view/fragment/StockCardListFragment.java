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

package org.openlmis.core.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.StockMovementActivity;
import org.openlmis.core.view.adapter.StockCardListAdapter;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

import roboguice.inject.InjectView;

import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.Active;

public class StockCardListFragment extends BaseFragment implements StockCardPresenter.StockCardListView, AdapterView.OnItemSelectedListener {

    @InjectView(R.id.sort_spinner)
    Spinner sortSpinner;

    @InjectView(R.id.tv_total)
    TextView tvTotal;

    @InjectView(R.id.products_list)
    RecyclerView stockCardRecycleView;

    @Inject
    StockCardPresenter presenter;

    StockCardListAdapter mAdapter;

    private List<StockCardViewModel> stockCardViewModels;
    private int currentPosition;

    @Override
    public Presenter initPresenter() {
        return presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stockcard_list, container, true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
    }

    private void initView(View view) {
        sortSpinner = (Spinner) view.findViewById(R.id.sort_spinner);
        stockCardRecycleView = (RecyclerView) view.findViewById(R.id.products_list);
        stockCardViewModels = presenter.getStockCardViewModels();
        mAdapter = new StockCardListAdapter(stockCardViewModels, onItemViewClickListener);

        initProductList();
        initSortSpinner();
    }

    private StockCardViewHolder.OnItemViewClickListener onItemViewClickListener = new StockCardViewHolder.OnItemViewClickListener() {
        @Override
        public void onItemViewClick(StockCardViewModel stockCardViewModel) {
            startActivityForResult(StockMovementActivity.getIntentToMe(getActivity(), stockCardViewModel.getStockCardId(), stockCardViewModel.getProduct().getFormattedProductName()), Constants.REQUEST_CODE_CHANGE);
        }
    };


    private void initProductList() {
        stockCardRecycleView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        stockCardRecycleView.setLayoutManager(mLayoutManager);
        stockCardRecycleView.setAdapter(mAdapter);

        presenter.loadStockCards(Active);
    }

    private void initSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_items_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentPosition = position;
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
    public void refresh() {
        stockCardViewModels = presenter.getStockCardViewModels();
        mAdapter = new StockCardListAdapter(stockCardViewModels, onItemViewClickListener);
        stockCardRecycleView.setAdapter(mAdapter);
        tvTotal.setText(getString(R.string.label_total, mAdapter.getItemCount()));
        onItemSelected(sortSpinner, null, currentPosition, 0L);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_CHANGE) {
            presenter.refreshStockCardViewModelsSOH();
            presenter.loadStockCards(Active);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onSearch(String query) {
        mAdapter.filter(query);
    }
}
