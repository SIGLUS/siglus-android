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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.StockMovementHistoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.StockMovementHistoryAdapter;

public class StockMovementHistoryFragment extends BaseFragment implements StockMovementHistoryPresenter.StockMovementHistoryView, OnRefreshListener {
    ListView historyListView;
    View horizontalScrollView;

    @Inject
    StockMovementHistoryPresenter presenter;

    private long startIndex = 0;
    private BaseAdapter adapter;
    private boolean isLoading;
    private boolean isFirstLoading;
    private View contentView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isRotated;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_stock_movement_history, container, false);
        presenter.attachView(this);
        presenter.setStockCardId(getActivity().getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0));
        initUI();

        if (isRotated) {
            reCreateView();
        } else {
            initData();
        }
        return contentView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        isRotated = true;
        super.onSaveInstanceState(outState);
    }

    private void reCreateView() {
        addFooterViewIfMoreThanOneScreen();
    }

    private void initUI() {
        historyListView = (ListView) contentView.findViewById(R.id.list);
        horizontalScrollView = contentView.findViewById(R.id.horizontal_scrollview);

        adapter = new StockMovementHistoryAdapter(getActivity(), presenter.getStockMovementModelList());
        historyListView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.stock_movement_history_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
    }


    public void initData() {
        isFirstLoading = true;
        ((BaseActivity) getActivity()).loading();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                loadData();
            }
        });
    }

    @Override
    public void onRefresh() {
        if (!isLoading) {
            loadData();
        }
    }

    private void loadData() {
        isLoading = true;
        presenter.loadStockMovementViewModels(startIndex);
        startIndex += StockMovementHistoryPresenter.MAXROWS;
    }

    @Override
    public void refreshStockMovement(boolean hasNewData) {
        if (hasNewData) {
            adapter.notifyDataSetChanged();
            if (isFirstLoading) {
                firstLoadingScrollToBottom();
                isFirstLoading = false;
            }
        } else {
            ToastUtil.showInCenter(R.string.hint_has_not_new_data);
            ((BaseActivity) getActivity()).loaded();
        }
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);
    }

    private void firstLoadingScrollToBottom() {
        addFooterViewIfMoreThanOneScreen();
        historyListView.post(new Runnable() {
            @Override
            public void run() {
                historyListView.setSelection(historyListView.getCount() - 1);
                ((BaseActivity) getActivity()).loaded();
            }
        });
    }

    private void addFooterViewIfMoreThanOneScreen() {
        historyListView.post(new Runnable() {
                                 @Override
                                 public void run() {
                                     if (isGreaterThanOneScreen()) {
                                         addFooterView();
                                     }
                                 }
                             }
        );
    }

    private void addFooterView() {
        TextView view = new TextView(getActivity());
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 150));
        if (historyListView.getFooterViewsCount() == 0) {
            historyListView.addFooterView(view);
        }
    }

    private boolean isGreaterThanOneScreen() {
        return historyListView.getChildCount() < historyListView.getCount();
    }

    @Override
    public void loading() {

    }

    @Override
    public void loading(String message) {

    }

    @Override
    public void loaded() {
    }
}
