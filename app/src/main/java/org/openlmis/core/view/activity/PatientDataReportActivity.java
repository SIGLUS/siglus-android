package org.openlmis.core.view.activity;

import android.os.Bundle;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_patient_data_report)
public class PatientDataReportActivity extends BaseActivity {

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }
}
