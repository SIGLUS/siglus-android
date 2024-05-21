package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.ExpiredStockCardListPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.ExpiredStockCardListAdapter;

import java.util.ArrayList;

import roboguice.inject.InjectView;

public class ExpiredStockCardListFragment extends StockCardListFragment {

    @InjectView(R.id.sort_spinner)
    Spinner spinner;

    @Inject
    ExpiredStockCardListPresenter presenter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner.setVisibility(View.GONE);
    }

    @Override
    protected void createAdapter() {
        mAdapter = new ExpiredStockCardListAdapter(new ArrayList<>(), null);
    }

    @Override
    public Presenter initPresenter() {
        return presenter;
    }

    @Override
    public void loadStockCards() {
        presenter.loadExpiredStockCards();
    }
}