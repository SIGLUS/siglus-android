package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.RapidTestReportsPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.RapidTestReportAdapter;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_rapid_test_reports)
public class RapidTestReportsActivity extends BaseActivity {
    @InjectView(R.id.rv_rapid_test_list)
    RecyclerView rapidTestListView;

    @InjectPresenter(RapidTestReportsPresenter.class)
    RapidTestReportsPresenter presenter;

    private RapidTestReportAdapter rapidTestReportAdapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.RapidTestScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading();
        setupReportsList();
        Subscription subscription = presenter.loadViewModels().subscribe(getRefreshReportListSubscriber());
        subscriptions.add(subscription);
    }

    private Action1<? super List<RapidTestReportViewModel>> getRefreshReportListSubscriber() {
        return new Action1<List<RapidTestReportViewModel>>() {
            @Override
            public void call(List<RapidTestReportViewModel> viewModels) {
                loaded();
                rapidTestReportAdapter.notifyDataSetChanged();
            }
        };
    }

    private void setupReportsList() {
        rapidTestListView.setLayoutManager(new LinearLayoutManager(this));
        rapidTestReportAdapter = new RapidTestReportAdapter(this,presenter.getViewModelList());
        rapidTestListView.setAdapter(rapidTestReportAdapter);
    }


}
