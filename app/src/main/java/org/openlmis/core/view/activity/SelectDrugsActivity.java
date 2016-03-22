package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;

public class SelectDrugsActivity extends BaseActivity {

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SelectDrugsScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_AMBER;
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, SelectDrugsActivity.class);
        return intent;
    }
}
