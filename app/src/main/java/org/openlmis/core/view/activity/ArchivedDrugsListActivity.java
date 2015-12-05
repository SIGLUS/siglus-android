package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.ArchivedListAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.Archived;
import static org.openlmis.core.view.holder.ArchivedDrugsViewHolder.ArchiveStockCardListener;

@ContentView(R.layout.activity_archived_drugs)
public class ArchivedDrugsListActivity extends SearchBarActivity implements StockCardPresenter.StockCardListView {

    @InjectView(R.id.archived_list)
    protected RecyclerView archivedList;

    @InjectPresenter(StockCardPresenter.class)
    StockCardPresenter presenter;

    private ArchivedListAdapter mAdapter;

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, ArchivedDrugsListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI() {
        archivedList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ArchivedListAdapter(new ArrayList<StockCardViewModel>(), archiveStockCardListener);
        archivedList.setAdapter(mAdapter);

        presenter.loadStockCards(Archived);
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

    protected ArchiveStockCardListener archiveStockCardListener = new ArchiveStockCardListener() {
        @Override
        public void viewMovementHistory(StockCard stockCard) {
            startActivity(StockMovementHistoryActivity.getIntentToMe(ArchivedDrugsListActivity.this,
                    stockCard.getId(),
                    stockCard.getProduct().getFormattedProductName(),
                    true));
        }

        @Override
        public void archiveStockCardBack(StockCard stockCard) {
            presenter.archiveBackStockCard(stockCard);
            presenter.loadStockCards(Archived);

            setResult(RESULT_OK);
        }
    };
}
