package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import roboguice.activity.RoboMigrationAndroidXFragmentActivity;

public class DumpFragmentActivity extends RoboMigrationAndroidXFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view = new LinearLayout(this);
        setContentView(view);
    }
}
