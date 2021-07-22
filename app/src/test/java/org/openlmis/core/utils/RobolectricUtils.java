package org.openlmis.core.utils;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;

import com.google.android.material.textfield.TextInputLayout;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.view.widget.SingleClickButtonListener;

public class RobolectricUtils {

  public static String getErrorText(TextInputLayout inputLayout) {
    return inputLayout == null || inputLayout.getError() == null ? null
        : inputLayout.getError().toString();
  }

  public static void waitLooperIdle() {
    shadowOf(getMainLooper()).idle();
  }

  public static void resetNextClickTime() {
    SingleClickButtonListener.setIsViewClicked(false);
    final long currentTimeMillis = LMISTestApp.getInstance().getCurrentTimeMillis();
    LMISTestApp.getInstance().setCurrentTimeMillis(currentTimeMillis + 600);
  }
}
