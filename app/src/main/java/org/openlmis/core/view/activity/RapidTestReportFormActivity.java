package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.RapidTestReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_rapid_test_report_form)
public class RapidTestReportFormActivity extends BaseActivity {
    @InjectView(R.id.tl_rapid_test_form)
    TableLayout rapidTestFormTable;

    @InjectView(R.id.tv_row_header_unpack_kit)
    TextView tvRowHeaderUnpackKit;

    @InjectPresenter(RapidTestReportFormPresenter.class)
    RapidTestReportFormPresenter presenter;

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
        Subscription subscription = presenter.loadViewModel(formId, periodBegin).subscribe(getPopulateFormDataAction());
        subscriptions.add(subscription);
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
        for (int i = 0; i< 11;i++) {
            TableRow tableRow = (TableRow) rapidTestFormTable.getChildAt(i);
            populateRow(viewModel.getItemViewModelList().get(i),tableRow);
        }
    }

    private void populateRow(RapidTestFormItemViewModel rowItem, TableRow tableRow) {
        ((EditText) ((LinearLayout)tableRow.getChildAt(1)).getChildAt(0)).setText(rowItem.getConsumeHIVDetermine());
        ((EditText) ((LinearLayout)tableRow.getChildAt(1)).getChildAt(2)).setText(rowItem.getPositiveHIVDetermine());
        ((EditText) ((LinearLayout)tableRow.getChildAt(2)).getChildAt(0)).setText(rowItem.getConsumeHIVUnigold());
        ((EditText) ((LinearLayout)tableRow.getChildAt(2)).getChildAt(2)).setText(rowItem.getPositiveHIVUnigold());
        ((EditText) ((LinearLayout)tableRow.getChildAt(3)).getChildAt(0)).setText(rowItem.getConsumeSyphillis());
        ((EditText) ((LinearLayout)tableRow.getChildAt(3)).getChildAt(2)).setText(rowItem.getPositiveSyphillis());
        ((EditText) ((LinearLayout)tableRow.getChildAt(4)).getChildAt(0)).setText(rowItem.getConsumeMalaria());
        ((EditText) ((LinearLayout)tableRow.getChildAt(4)).getChildAt(2)).setText(rowItem.getPositiveMalaria());
    }

    public static Intent getIntentToMe(Context context, long formId, DateTime periodBegin) {
        Intent intent = new Intent(context, RapidTestReportFormActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }
}
