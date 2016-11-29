package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.Constants;

public class RapidTestReportFormActivity extends BaseActivity {
    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static Intent getIntentToMe(Context context, long formId) {
        Intent intent = new Intent(context, RapidTestReportFormActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        return intent;
    }
}
