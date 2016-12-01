package org.openlmis.core.view.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.RapidTestReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.RapidTestReportRowAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_rapid_test_report_form)
public class RapidTestReportFormActivity extends BaseActivity implements SimpleDialogFragment.MsgDialogCallBack{
    @InjectView(R.id.rv_rapid_report_row_item_list)
    RecyclerView rvReportRowItemListView;

    @InjectView(R.id.btn_complete)
    Button btnComplete;

    @InjectView(R.id.btn_save)
    View btnSave;

    @InjectPresenter(RapidTestReportFormPresenter.class)
    RapidTestReportFormPresenter presenter;

    RapidTestReportRowAdapter adapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.RapidTestReportFormScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading();
        long formId = getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0L);
        DateTime periodBegin = (DateTime) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);

        setUpButtonPanel();

        setUpRowItems();
        Subscription subscription = presenter.loadViewModel(formId, periodBegin).subscribe(getPopulateFormDataAction());
        subscriptions.add(subscription);
    }

    private void setUpRowItems() {
        adapter = new RapidTestReportRowAdapter();
        rvReportRowItemListView.setLayoutManager(new LinearLayoutManager(this));
        rvReportRowItemListView.setAdapter(adapter);
    }

    private void setUpButtonPanel() {
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO validate
                //TODO show sign dialog
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO validate the form and make the wrong input text red
                //TODO save the form
            }
        });
    }

    @NonNull
    private Action1<RapidTestReportViewModel> getPopulateFormDataAction() {
        return new Action1<RapidTestReportViewModel>() {
            @Override
            public void call(RapidTestReportViewModel viewModel) {
                populateFormData(viewModel);
                loaded();
            }
        };
    }

    private void populateFormData(RapidTestReportViewModel viewModel) {
        adapter.refresh(viewModel.getItemViewModelList());
    }

    public static Intent getIntentToMe(Context context, long formId, DateTime periodBegin) {
        Intent intent = new Intent(context, RapidTestReportFormActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }

    @Override
    public void onBackPressed() {
        showConfirmDialog();
    }

    private void showConfirmDialog() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.msg_back_confirm),
                getString(R.string.btn_positive),
                getString(R.string.btn_negative),
                "onBackPressed");
        dialogFragment.show(getFragmentManager(), "");
    }

    @Override
    public void positiveClick(String tag) {
        //TODO delete draft
        super.onBackPressed();
    }

    @Override
    public void negativeClick(String tag) {
    }
}
