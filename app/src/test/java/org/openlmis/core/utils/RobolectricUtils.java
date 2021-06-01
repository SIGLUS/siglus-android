package org.openlmis.core.utils;

import com.google.android.material.textfield.TextInputLayout;
import android.widget.TextView;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;

public class RobolectricUtils {
    public static TextView getErrorTextView(TextInputLayout inputLayout) {
        // Will use getError() method after support design library upgraded
        TextView errorText = null;
        Field field = FieldUtils.getField(TextInputLayout.class, "mErrorView", true);
        try {
            errorText = (TextView) field.get(inputLayout);
        } catch (IllegalAccessException ignored) {
        }
        return errorText;
    }
}
