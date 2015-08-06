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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.StockCardListPresenter;
import org.openlmis.core.view.adapter.StockCardListAdapter;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import roboguice.fragment.RoboFragment;

public class StockCardListFragment extends RoboFragment implements StockCardListPresenter.StockCardListView{


    RecyclerView inventoryList;

    @Inject
    StockRepository stockRepository;

    @Inject
    @Getter
    StockCardListPresenter presenter;

    StockCardListAdapter mAdapter;

    List<StockCard> stockCardList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_inventory_list, null);
        inventoryList = (RecyclerView)view.findViewById(R.id.products_list);

        inventoryList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(container.getContext());
        inventoryList.setLayoutManager(mLayoutManager);
        inventoryList.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter.attachView(this);
        stockCardList = presenter.loadStockCards();
        mAdapter = new StockCardListAdapter(stockCardList);
    }


    public void filterStockCard(String query) {
        mAdapter.filter(query);
    }
}
