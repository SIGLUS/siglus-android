package org.openlmis.core.utils;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;

import com.google.android.material.textfield.TextInputLayout;

public class RobolectricUtils {

  public static String getErrorText(TextInputLayout inputLayout) {
    return inputLayout == null || inputLayout.getError() == null ? null
        : inputLayout.getError().toString();
  }

  public static void waitLooperIdle() {
    shadowOf(getMainLooper()).idle();
  }
}
