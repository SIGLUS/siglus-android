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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.presenter.AllDrugsMovementPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.MovementHistoryAdapter;
import org.openlmis.core.view.viewmodel.StockHistoryViewModel;
import roboguice.inject.InjectView;

public class AllDrugsMovementHistoryFragment extends BaseFragment implements
    AllDrugsMovementPresenter.AllDrugsMovementView {

  @Inject
  AllDrugsMovementPresenter presenter;

  @InjectView(R.id.date_spinner)
  Spinner dateSpinner;

  @InjectView(R.id.tv_product_count)
  TextView tvProductCount;

  @InjectView(R.id.tv_movement_count)
  TextView tvMovementCount;

  @InjectView(R.id.rv_movement_history)
  RecyclerView movementHistoryListView;
  private MovementHistoryAdapter adapter;

  private static final List<Integer> DATE_ITEMS = Arrays.asList(7, 14, 30, 90, 180, 365);

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_all_drugs_movement_history, container, true);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    innitSpinner();
    initRecyclerView();
  }

  private void initRecyclerView() {
    movementHistoryListView.setLayoutManager(new LinearLayoutManager(getActivity()));
    adapter = new MovementHistoryAdapter();
    movementHistoryListView.setAdapter(adapter);
  }

  private void innitSpinner() {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
        R.array.movement_date_items_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    dateSpinner.setAdapter(adapter);
    dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        presenter.loadMovementHistory(DATE_ITEMS.get(position));
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
      }
    });
  }

  @Override
  public void refreshRecyclerView(List<StockHistoryViewModel> stockHistoryViewModels) {
    adapter.refresh(stockHistoryViewModels);
  }

  @Override
  public void updateHistoryCount(int productCount, int movementCount) {
    tvProductCount
        .setText(getResources().getString(R.string.msg_products_with_movement_count, productCount));
    tvMovementCount.setText(getResources().getString(R.string.msg_movement_count, movementCount));
  }
}

