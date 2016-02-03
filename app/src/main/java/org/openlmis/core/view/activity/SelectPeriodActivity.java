package org.openlmis.core.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_select_period)
public class SelectPeriodActivity extends BaseActivity {

    @InjectView(R.id.tv_select_period_instruction)
    public TextView tvInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        DateTime date = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        tvInstruction.setText(Html.fromHtml(this.getString(R.string.label_select_close_of_period, date.toString("MMM"), date.toString("DD MMM"))));
    }

    public static Intent getIntentToMe() {
        return new Intent();
    }
}
