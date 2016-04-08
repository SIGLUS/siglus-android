package org.openlmis.core.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class DummyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view = new LinearLayout(this);
        setContentView(view);
    }
}
