/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */
package org.openlmis.core.utils;

import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import org.openlmis.core.LMISApp;

public final class ToastUtil {

    private ToastUtil() {

    }

    public static void show(CharSequence text) {
        if (TextUtils.isEmpty(text)) return;
        Toast.makeText(LMISApp.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void showForLongTime(@StringRes int resId) {
        Toast.makeText(LMISApp.getContext(), resId, Toast.LENGTH_LONG).show();
    }

    public static void show(int resId) {
        show(LMISApp.getContext().getString(resId));
    }

    public static void showInCenter(int text) {
        Toast toast = Toast
                .makeText(LMISApp.getContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
