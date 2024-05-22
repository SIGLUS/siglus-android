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

import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.ACTIVE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.event.CmmCalculateEvent;
import org.openlmis.core.event.DeleteDirtyDataEvent;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InventoryUtils;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.HomeActivity;
import org.openlmis.core.view.activity.StockMovementsWithLotActivity;
import org.openlmis.core.view.adapter.StockCardListAdapter;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ProductsUpdateBanner;
import java.util.ArrayList;
import java.util.List;
import roboguice.inject.InjectView;

@BindEventBus
public class StockCardListFragment extends BaseFragment implements
    StockCardPresenter.StockCardListView, AdapterView.OnItemSelectedListener {

  @InjectView(R.id.sort_spinner)
  Spinner sortSpinner;

  @InjectView(R.id.tv_total)
  TextView tvTotal;

  @InjectView(R.id.products_list)
  RecyclerView stockCardRecycleView;

  @InjectView(R.id.product_update_banner)
  ProductsUpdateBanner productsUpdateBanner;

  @InjectView(R.id.fast_scroller)
  RecyclerViewFastScroller fastScroller;

  @Inject
  StockCardPresenter presenter;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  @Inject
  DirtyDataManager dirtyDataManager;

  StockCardListAdapter mAdapter;

  private int currentPosition;

  private ActivityResultCallback<ActivityResult> stockListCallback = result -> {
    if (result.getResultCode() == Activity.RESULT_OK) {
      refreshPresenterIfHasIssuesOrEntries(result.getData());
    }
  };

  private final ActivityResultLauncher<Intent> toStockMovementWithLotLauncher = registerForActivityResult(
      new StartActivityForResult(), stockListCallback);

  @Override
  public Presenter initPresenter() {
    return presenter;
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
    // do nothing
  }

  @Override
  public void refreshBannerText() {
    productsUpdateBanner.refreshBannerText();
  }

  @Override
  public void showWarning() {
    ((BaseActivity) requireActivity()).showDeletedWarningDialog(buildWarningDialogFragmentDelegate());
  }

  @Override
  public void refresh(List<InventoryViewModel> data) {
    mAdapter.refreshList(data);
    tvTotal.setText(getString(R.string.label_total, mAdapter.getItemCount()));
    onItemSelected(sortSpinner, null, currentPosition, 0L);
    setUpFastScroller(data);
  }

  protected boolean isFastScrollEnabled() { return false; }

  private void setUpFastScroller(List<InventoryViewModel> data) {
    if (!isFastScrollEnabled() || data.isEmpty()) {
      fastScroller.setVisibility(View.GONE);
      return;
    }

    List<AlphabetItem> mAlphabetItems = InventoryUtils.getAlphabetItemsByInventories(data);

    fastScroller.setRecyclerView(stockCardRecycleView);
    fastScroller.setUpAlphabet(mAlphabetItems);

    fastScroller.setVisibility(View.VISIBLE);
  }

  public void onSearch(String query) {
    mAdapter.filter(query);
    setUpFastScroller(mAdapter.getFilteredList());
    tvTotal.setText(getString(R.string.label_total, mAdapter.getItemCount()));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveCmmCalculateEvent(CmmCalculateEvent event) {
    if (event.isStart()) {
      return;
    }
    loadStockCards();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveDeleteDirtyDataEvent(DeleteDirtyDataEvent event) {
    if (DeleteDirtyDataEvent.START == event) {
      loading(getResources().getString(R.string.msg_delete_dirty_data));
    }
    if (DeleteDirtyDataEvent.FINISH == event) {
      loaded();
    }
  }

  protected void createAdapter() {
    mAdapter = new StockCardListAdapter(new ArrayList<>(), onItemViewClickListener);
  }

  public void loadStockCards() {
    presenter.loadStockCards(ACTIVE);
  }

  protected StockCardViewHolder.OnItemViewClickListener onItemViewClickListener = inventoryViewModel -> {
    Intent intent = getStockMovementIntent(inventoryViewModel);
    toStockMovementWithLotLauncher.launch(intent);
  };

  protected Intent getStockMovementIntent(InventoryViewModel inventoryViewModel) {
    return StockMovementsWithLotActivity.getIntentToMe(getActivity(), inventoryViewModel, false);
  }

  @NonNull
  private WarningDialogFragment.DialogDelegate buildWarningDialogFragmentDelegate() {
    return () -> {
      dirtyDataManager.deleteAndReset();
      Intent intent = HomeActivity.getIntentToMe(LMISApp.getContext());
      requireActivity().startActivity(intent);
      requireActivity().finish();
    };
  }

  private void initRecycleView() {
    createAdapter();
    stockCardRecycleView.setHasFixedSize(true);
    stockCardRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
    stockCardRecycleView.setAdapter(mAdapter);
  }

  private void initSortSpinner() {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
        R.array.sort_items_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sortSpinner.setAdapter(adapter);
    sortSpinner.setOnItemSelectedListener(this);
  }

  public void refreshPresenterIfHasIssuesOrEntries(Intent data) {
    long[] stockCardIds = data.getLongArrayExtra(Constants.PARAM_STOCK_CARD_ID_ARRAY);
    if (stockCardIds == null) {
      return;
    }
    presenter.refreshStockCardsObservable(stockCardIds);
  }

  public ActivityResultCallback<ActivityResult> getStockListCallback() {
    return stockListCallback;
  }
}
