package org.openlmis.core.utils;

import com.google.android.material.textfield.TextInputLayout;

public class RobolectricUtils {
    public static String getErrorText(TextInputLayout inputLayout) {
        return inputLayout == null || inputLayout.getError() == null ? null : inputLayout.getError().toString();
    }
}
