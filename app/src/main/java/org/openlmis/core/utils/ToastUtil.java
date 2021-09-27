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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class ToastUtil {

  public static SuperActivityToast activityToast;

  private ToastUtil() {
  }

  @SuppressLint("WrongConstant")
  public static void show(CharSequence text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    showActivityToast(text.toString(), Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM,
        (int) LMISApp.getContext().getResources().getDimension(R.dimen.px_180));
  }

  public static void show(int resId) {
    show(LMISApp.getContext().getString(resId));
  }

  public static void showInCenter(int text) {
    showActivityToast(LMISApp.getContext().getResources().getText(text).toString(), Gravity.CENTER, 0);
  }

  private static void showActivityToast(String text, int gravity, int yOffset) {
    WindowManager wm = (WindowManager) LMISApp.getContext().getSystemService(Context.WINDOW_SERVICE);
    int width = wm.getDefaultDisplay().getWidth();
    Activity currentActivity = LMISApp.getInstance().getActiveActivity();
    if (currentActivity != null) {
      if (activityToast == null) {
        activityToast = SuperActivityToast.create(currentActivity, new Style(), Style.TYPE_STANDARD);
      }
      activityToast.setText(text)
          .setDuration(Style.DURATION_VERY_LONG)
          .setGravity(gravity, 0, yOffset)
          .setWidth(width - 100)
          .setFrame(Style.FRAME_STANDARD)
          .setAnimations(Style.ANIMATIONS_FADE).show();
    }
  }
}
