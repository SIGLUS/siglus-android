package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.openlmis.core.R;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_archived_drugs)
public class ArchivedDrugsListActivity extends SearchBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, ArchivedDrugsListActivity.class);
    }

    @Override
    public boolean onSearchStart(String query) {
        return false;
    }

    @Override
    public boolean onSearchClosed() {
        return false;
    }
}
