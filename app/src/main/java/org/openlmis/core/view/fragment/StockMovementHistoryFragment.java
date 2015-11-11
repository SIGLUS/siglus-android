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
import org.openlmis.core.view.LoadingView;
import org.openlmis.core.view.adapter.StockMovementHistoryAdapter;

public class StockMovementHistoryFragment extends BaseFragment implements StockMovementHistoryPresenter.StockMovementHistoryView, OnRefreshListener {

    @Inject
    StockMovementHistoryPresenter presenter;

    private long startIndex = 0;
    private boolean isLoading;
    private boolean isFirstLoading;
    private boolean isRotated;

    private View contentView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LoadingView loadingView;
    private ListView historyListView;

    private BaseAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_stock_movement_history, container, false);

        presenter.attachView(this);
        presenter.setStockCardId(getActivity().getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0));

        return contentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof LoadingView) {
            loadingView = (LoadingView) activity;
        } else {
            throw new ClassCastException("Host Activity should implements LoadingView method");
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();

        if (isRotated) {
            addFooterViewIfMoreThanOneScreen();
        } else {
            initData();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        isRotated = true;
        super.onSaveInstanceState(outState);
    }

    private void initUI() {
        historyListView = (ListView) contentView.findViewById(R.id.list);

        adapter = new StockMovementHistoryAdapter(getActivity(), presenter.getStockMovementModelList());
        historyListView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.stock_movement_history_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
    }


    public void initData() {
        isFirstLoading = true;
        loadingView.loading();
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
            loadingView.loaded();
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
                loadingView.loaded();
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
        loadingView.loading();
    }

    @Override
    public void loading(String message) {
        loadingView.loading(message);
    }

    @Override
    public void loaded() {
        loadingView.loaded();
    }
}
