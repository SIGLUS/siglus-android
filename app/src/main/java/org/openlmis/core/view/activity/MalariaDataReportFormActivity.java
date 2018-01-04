package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.fragment.MalariaDataReportFormFragment;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_malaria_data_report_form)
public class MalariaDataReportFormActivity extends BaseActivity {

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.MalariaDataReportFormScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    public void onBackPressed() {
        ((MalariaDataReportFormFragment) getFragmentManager().findFragmentById(R.id.fragment_malaria_data_report_form)).onBackPressed();
    }

    public static Intent getIntentToMe(Context context, long formId, DateTime periodBegin) {
        Intent intent = new Intent(context, MalariaDataReportFormActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }
}
