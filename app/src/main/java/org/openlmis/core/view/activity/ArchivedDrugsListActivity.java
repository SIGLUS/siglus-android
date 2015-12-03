package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.ArchivedListAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_archived_drugs)
public class ArchivedDrugsListActivity extends SearchBarActivity implements StockCardPresenter.StockCardListView {

    @InjectView(R.id.archived_list)
    protected RecyclerView archivedList;

    @InjectPresenter(StockCardPresenter.class)
    StockCardPresenter presenter;

    private ArchivedListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI() {
        archivedList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ArchivedListAdapter(new ArrayList<StockCardViewModel>());
        archivedList.setAdapter(mAdapter);

        presenter.loadStockCards();
    }

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, ArchivedDrugsListActivity.class);
    }

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        return false;
    }

    @Override
    public boolean onSearchClosed() {
        mAdapter.filter(StringUtils.EMPTY);
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.search_view_enhancement)) {
            return true;
        }
        return false;
    }

    @Override
    public void refresh() {
        List<StockCardViewModel> stockCardViewModels = presenter.getStockCardViewModels();
        mAdapter.refreshList(stockCardViewModels);
    }
}
