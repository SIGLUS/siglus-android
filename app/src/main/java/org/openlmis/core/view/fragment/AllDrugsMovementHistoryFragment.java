package org.openlmis.core.view.fragment;

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
import org.openlmis.core.presenter.AllDrugsMovementPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.MovementHistoryAdapter;
import org.openlmis.core.view.viewmodel.StockHistoryViewModel;

import java.util.Arrays;
import java.util.List;

import roboguice.inject.InjectView;

public class AllDrugsMovementHistoryFragment extends BaseFragment implements AllDrugsMovementPresenter.AllDrugsMovementView {
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

    private static List<Integer> DATE_ITEMS = Arrays.asList(7, 14, 30, 90, 180, 365);

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

            }
        });
    }

    @Override
    public void refreshRecyclerView(List<StockHistoryViewModel> stockHistoryViewModels) {
        adapter.refresh(stockHistoryViewModels);
    }

    @Override
    public void updateHistoryCount(int productCount, int movementCount) {
        tvProductCount.setText(getResources().getString(R.string.msg_products_with_movement_count, productCount));
        tvMovementCount.setText(getResources().getString(R.string.msg_movement_count, movementCount));
    }
}

