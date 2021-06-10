package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.keyboard.KeyboardHeightObserver;
import org.openlmis.core.utils.keyboard.KeyboardHeightProvider;
import org.openlmis.core.view.fragment.RapidTestReportFormFragment;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_rapid_test_report_form)
public class RapidTestReportFormActivity extends BaseActivity implements KeyboardHeightObserver {

  private RapidTestReportFormFragment rapidTestReportFormFragment;

  private KeyboardHeightProvider keyboardHeightProvider;

  private final Point currentTouchPoint = new Point();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rapidTestReportFormFragment = (RapidTestReportFormFragment) getFragmentManager()
        .findFragmentById(R.id.fragment_rapid_test_report_form);
    keyboardHeightProvider = new KeyboardHeightProvider(this);
    findViewById(R.id.fl_rapid_test_root).post(keyboardHeightProvider::start);
  }

  @Override
  protected void onResume() {
    super.onResume();
    keyboardHeightProvider.setKeyboardHeightObserver(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    keyboardHeightProvider.setKeyboardHeightObserver(null);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    keyboardHeightProvider.close();
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.RAPID_TEST_REPORT_FORM_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_BlueGray;
  }

  @Override
  public void onBackPressed() {
    rapidTestReportFormFragment.onBackPressed();
  }

  public static Intent getIntentToMe(Context context, long formId, DateTime periodBegin) {
    Intent intent = new Intent(context, RapidTestReportFormActivity.class);
    intent.putExtra(Constants.PARAM_FORM_ID, formId);
    intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
    return intent;
  }

  public static Intent getIntentToMe(Context context, long formId, Period period,
      DateTime periodBegin) {
    Intent intent = new Intent(context, RapidTestReportFormActivity.class);
    intent.putExtra(Constants.PARAM_FORM_ID, formId);
    intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
    intent.putExtra(Constants.PARAM_PERIOD, period);
    return intent;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    currentTouchPoint.set((int) (ev.getRawX()), (int) (ev.getRawY()));
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public void onKeyboardHeightChanged(int height) {
    if (rapidTestReportFormFragment != null) {
      rapidTestReportFormFragment.keyboardChanged(height, currentTouchPoint);
    }
  }
}
