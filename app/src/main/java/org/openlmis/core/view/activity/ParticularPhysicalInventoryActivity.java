package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;

public class ParticularPhysicalInventoryActivity extends PhysicalInventoryActivity {


    public static Intent getIntentToMe(Context context) {
        return new Intent(context, ParticularPhysicalInventoryActivity.class);
    }

    @Override
    public void onBackPressed() {
        if (isSearchViewActivity()) {
            searchView.onActionViewCollapsed();
        }
    }
}
