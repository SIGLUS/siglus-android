package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import roboguice.activity.RoboFragmentActivity;

public class DumpFragmentActivity extends RoboFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view = new LinearLayout(this);
        setContentView(view);
    }
}
