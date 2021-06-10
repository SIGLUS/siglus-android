package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_all_drugs_movement_history)
public class AllDrugsMovementHistoryActivity extends BaseActivity {

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ALL_DRUGS_MOVEMENT_HISTORY_SCREEN;
  }

  public static Intent getIntentToMe(Context context) {
    return new Intent(context, AllDrugsMovementHistoryActivity.class);
  }
}
