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

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.StockMovementsActivity;
import org.openlmis.core.view.activity.StockMovementsWithLotActivity;
import org.openlmis.core.view.adapter.StockCardListAdapter;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ProductsUpdateBanner;

import java.util.ArrayList;
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

    @InjectView(R.id.product_update_banner)
    ProductsUpdateBanner productsUpdateBanner;

    @Inject
    StockCardPresenter presenter;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    StockCardListAdapter mAdapter;

    private int currentPosition;

    @Override
    public Presenter initPresenter() {
        return presenter;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stockcard_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRecycleView();
        initSortSpinner();

        loadStockCards();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_FROM_STOCK_LIST_PAGE) {
            presenter.refreshStockCardViewModelsSOH();
            productsUpdateBanner.refreshBannerText();

            loadStockCards();
        }
    }

    public void onSearch(String query) {
        mAdapter.filter(query);
        tvTotal.setText(getString(R.string.label_total, mAdapter.getItemCount()));
    }

    protected void createAdapter() {
        mAdapter = new StockCardListAdapter(new ArrayList<InventoryViewModel>(), onItemViewClickListener);
    }

    protected void loadStockCards() {
        presenter.loadStockCards(Active);
    }

    private void initRecycleView() {
        createAdapter();

        stockCardRecycleView.setHasFixedSize(true);
        stockCardRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        stockCardRecycleView.setAdapter(mAdapter);
    }

    protected StockCardViewHolder.OnItemViewClickListener onItemViewClickListener = new StockCardViewHolder.OnItemViewClickListener() {
        @Override
        public void onItemViewClick(InventoryViewModel inventoryViewModel) {
            Intent intent = getStockMovementIntent(inventoryViewModel);
            startActivityForResult(intent, Constants.REQUEST_FROM_STOCK_LIST_PAGE);
        }
    };

    protected Intent getStockMovementIntent(InventoryViewModel inventoryViewModel) {
        if(LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            return StockMovementsWithLotActivity.getIntentToMe(getActivity(), inventoryViewModel, false);
        } else {
            return StockMovementsActivity.getIntentToMe(getActivity(), inventoryViewModel, false);
        }
    }

    private void initSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_items_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void refresh(List<InventoryViewModel> data) {
        mAdapter.refreshList(data);

        tvTotal.setText(getString(R.string.label_total, mAdapter.getItemCount()));
        onItemSelected(sortSpinner, null, currentPosition, 0L);
    }

}
