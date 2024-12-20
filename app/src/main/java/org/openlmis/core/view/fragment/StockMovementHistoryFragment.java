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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.inject.Inject;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockMovementHistoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.widget.StockMovementHeaderView;
import roboguice.inject.InjectView;

public class StockMovementHistoryFragment extends BaseFragment implements
    StockMovementHistoryPresenter.StockMovementHistoryView {

  @Inject
  StockMovementHistoryPresenter presenter;

  @InjectView(R.id.stock_movement_history_swipe_container)
  SwipeRefreshLayout swipeRefreshLayout;

  @InjectView(R.id.rv_stock_movement_list)
  RecyclerView rvStockMovementList;

  @InjectView(R.id.tv_archived_old_data)
  TextView tvArchivedOldData;

  @InjectView(R.id.stock_movement_header)
  StockMovementHeaderView stockMovementHeaderView;

  private long startIndex = 0;
  private boolean isLoading;
  private boolean isFirstLoading;

  private StockMovementAdapter stockMovementAdapter;
  private boolean isKit;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    long stockCardID = requireActivity().getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0);
    isKit = requireActivity().getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);
    presenter.setStockCardId(stockCardID);
  }

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_stock_movement_history, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initUI();
    if (this.isSavedInstanceState) {
      addFooterViewIfMoreThanOneScreen();
    } else {
      initData();
    }
  }

  private void initUI() {
    rvStockMovementList.setLayoutManager(new LinearLayoutManager(requireContext()));
    stockMovementAdapter = new StockMovementAdapter();
    stockMovementAdapter.setKit(isKit);
    stockMovementAdapter.setPreviousPage(ScreenName.STOCK_MOVEMENT_DETAIL_HISTORY_SCREEN);
    rvStockMovementList.setAdapter(stockMovementAdapter);
    stockMovementAdapter.setNewInstance(presenter.getStockMovementModelList());
    if (!SharedPreferenceMgr.getInstance().hasDeletedOldStockMovement()) {
      tvArchivedOldData.setVisibility(View.GONE);
    }
    if (isKit) {
      stockMovementHeaderView.hideLotCodeHeaderView();
    }
    addRecyclerViewListener();
  }

  public void initData() {
    isFirstLoading = true;
    loading();
    swipeRefreshLayout.post(() -> {
      swipeRefreshLayout.setRefreshing(true);
      loadData();
    });
  }

  private void addRecyclerViewListener() {
    rvStockMovementList.addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null
            && layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
          if (!swipeRefreshLayout.isRefreshing()) {
            loadMoreData();
          }
        }
      }
    });
  }

  public void loadMoreData() {
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
      stockMovementAdapter.notifyDataSetChanged();
      if (isFirstLoading) {
        firstLoadingScrollToBottom();
        isFirstLoading = false;
      }
    } else {
      ToastUtil.showInCenter(R.string.hint_has_not_new_data);
      loaded();
    }
    isLoading = false;
    swipeRefreshLayout.setRefreshing(false);
  }

  private void firstLoadingScrollToBottom() {
    addFooterViewIfMoreThanOneScreen();
    rvStockMovementList.post(() -> {
      loaded();
    });
  }

  private void addFooterViewIfMoreThanOneScreen() {
    rvStockMovementList.post(() -> {
      if (isGreaterThanOneScreen()) {
        addFooterView();
      }
    });
  }

  private void addFooterView() {
    TextView view = new TextView(getActivity());
    view.setLayoutParams(new AbsListView.LayoutParams(MATCH_PARENT, 150));
    if (stockMovementAdapter.getFooterLayoutCount() == 0) {
      stockMovementAdapter.addFooterView(view);
    }
  }

  private boolean isGreaterThanOneScreen() {
    return rvStockMovementList.getChildCount() < stockMovementAdapter.getData().size();
  }
}