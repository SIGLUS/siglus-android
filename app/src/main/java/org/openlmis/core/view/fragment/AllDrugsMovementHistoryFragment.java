package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.AllDrugsMovementPresenter;
import org.openlmis.core.presenter.Presenter;

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
        presenter.loadAllMovementHistory();
    }

    private void initRecyclerView() {
    }

    private void innitSpinner() {

    }
}
