package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.fragment.PatientDataReportFormFragment;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_patient_data_report_form)
public class PatientDataReportFormActivity extends BaseActivity {

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.PatientDataReportFormScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    public void onBackPressed() {
        ((PatientDataReportFormFragment) getFragmentManager().findFragmentById(R.id.fragment_patient_data_report_form)).onBackPressed();
    }

    public static Intent getIntentToMe(Context context, long formId, DateTime periodBegin) {
        Intent intent = new Intent(context, PatientDataReportFormActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }
}
