package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.Period;
import org.openlmis.core.presenter.RapidTestReportsPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.RapidTestReportAdapter;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_rapid_test_reports)
public class RapidTestReportsActivity extends BaseReportListActivity {
    @InjectView(R.id.rv_rapid_test_list)
    RecyclerView rapidTestListView;

    @InjectPresenter(RapidTestReportsPresenter.class)
    RapidTestReportsPresenter presenter;

    private RapidTestReportAdapter rapidTestReportAdapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.RAPID_TEST_SCREEN;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading();
        setupReportsList();
        loadForms();
    }

    protected void loadForms() {
        Subscription subscription = presenter.loadViewModels().subscribe(getRefreshReportListSubscriber());
        subscriptions.add(subscription);
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_BlueGray;
    }

    private Action1<? super List<RapidTestReportViewModel>> getRefreshReportListSubscriber() {
        return (Action1<List<RapidTestReportViewModel>>) viewModels -> {
            loaded();
            rapidTestReportAdapter.notifyDataSetChanged();
        };
    }

    private void setupReportsList() {
        rapidTestListView.setLayoutManager(new LinearLayoutManager(this));
        rapidTestReportAdapter = new RapidTestReportAdapter(this, presenter.getViewModelList());
        rapidTestListView.setAdapter(rapidTestReportAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM
                || requestCode ==Constants.REQUEST_FROM_RNR_LIST_PAGE) {
            loadForms();
        } else if (requestCode == Constants.REQUEST_SELECT_PERIOD_END && resultCode == Activity.RESULT_OK) {
            Period period = (Period) data.getSerializableExtra(Constants.PARAM_PERIOD);
            startActivityForResult(RapidTestReportFormActivity.getIntentToMe(this, RapidTestReportViewModel.DEFAULT_FORM_ID, period, period.getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM);
        }
    }
}
