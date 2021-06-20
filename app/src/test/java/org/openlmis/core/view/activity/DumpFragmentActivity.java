package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.widget.LinearLayout;
import org.openlmis.core.googleanalytics.ScreenName;

public class DumpFragmentActivity extends BaseActivity {

  @Override
  protected ScreenName getScreenName() {
    return null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout view = new LinearLayout(this);
    setContentView(view);
  }
}
