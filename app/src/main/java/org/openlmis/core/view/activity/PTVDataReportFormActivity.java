package org.openlmis.core.view.activity;

import android.os.Bundle;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_ptv_report_form)
public class PTVDataReportFormActivity extends BaseActivity {

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
