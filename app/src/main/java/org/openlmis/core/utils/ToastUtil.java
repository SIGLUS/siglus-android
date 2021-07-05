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

import static android.widget.Toast.makeText;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class ToastUtil {

  private ToastUtil() {

  }

  public static void show(CharSequence text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    Toast toast = makeText(LMISApp.getContext(), text, Toast.LENGTH_SHORT);
    setToastLayout(toast);
    toast.show();
  }

  public static void show(int resId) {
    show(LMISApp.getContext().getString(resId));
  }

  public static void showForLongTime(CharSequence text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    Toast toast = makeText(LMISApp.getContext(), text, Toast.LENGTH_LONG);
    setToastLayout(toast);
    toast.show();
  }

  public static void showForLongTime(@StringRes int resId) {
    Toast toast = makeText(LMISApp.getContext(), resId, Toast.LENGTH_LONG);
    setToastLayout(toast);
    toast.show();
  }

  public static void showInCenter(int text) {
    Toast toast = makeText(LMISApp.getContext(), text, Toast.LENGTH_SHORT);
    setToastLayout(toast);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }

  private static void setToastLayout(Toast toast) {
    toast.setGravity(Gravity.BOTTOM, 0, 50);
    View view = toast.getView();
    if (view != null) {
      view.setBackgroundResource(R.drawable.toast_bg);
      TextView textView = view.findViewById(android.R.id.message);
      textView.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_white));
      textView.setTextSize(18);
    }
  }
}
