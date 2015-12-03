package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.presenter.ArchivedPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.ArchivedListAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@ContentView(R.layout.activity_archived_drugs)
public class ArchivedDrugsListActivity extends SearchBarActivity {

    @InjectView(R.id.archived_list)
    protected RecyclerView archivedList;

    @InjectPresenter(ArchivedPresenter.class)
    ArchivedPresenter presenter;

    private ArchivedListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI() {
        archivedList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ArchivedListAdapter(this, new ArrayList<StockCardViewModel>());
        archivedList.setAdapter(mAdapter);
        presenter.loadArchivedDrugs().subscribe(getArchivedSubscriber());
    }

    protected Subscriber<List<StockCardViewModel>> getArchivedSubscriber() {
        return new Subscriber<List<StockCardViewModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(List<StockCardViewModel> stockCardViewModels) {
                mAdapter.refreshList(stockCardViewModels);
            }
        };
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

}
