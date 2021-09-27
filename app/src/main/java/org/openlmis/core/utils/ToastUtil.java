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

  private ToastUtil() {
  }

  @SuppressLint("WrongConstant")
  public static void show(CharSequence text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    WindowManager wm = (WindowManager) LMISApp.getContext().getSystemService(Context.WINDOW_SERVICE);
    int width = wm.getDefaultDisplay().getWidth();
    Activity currentActivity = LMISApp.getActiveActivity();
    if (currentActivity != null) {
      SuperActivityToast.create(currentActivity, new Style(), Style.TYPE_STANDARD)
          .setText(text.toString())
          .setDuration(Style.DURATION_VERY_LONG)
          .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0,
              (int) LMISApp.getContext().getResources().getDimension(R.dimen.px_180))
          .setWidth(width - 100)
          .setFrame(Style.FRAME_STANDARD)
          .setAnimations(Style.ANIMATIONS_FADE).show();
    }
  }

  public static void show(int resId) {
    show(LMISApp.getContext().getString(resId));
  }

  public static void showInCenter(int text) {
    WindowManager wm = (WindowManager) LMISApp.getContext().getSystemService(Context.WINDOW_SERVICE);
    int width = wm.getDefaultDisplay().getWidth();
    Activity currentActivity = LMISApp.getActiveActivity();
    if (currentActivity != null) {
      SuperActivityToast.create(currentActivity, new Style(), Style.TYPE_STANDARD)
          .setText(LMISApp.getContext().getResources().getText(text).toString())
          .setDuration(Style.DURATION_VERY_LONG)
          .setGravity(Gravity.CENTER, 0,
              (int) LMISApp.getContext().getResources().getDimension(R.dimen.px_180))
          .setWidth(width - 100)
          .setFrame(Style.FRAME_STANDARD)
          .setAnimations(Style.ANIMATIONS_FADE).show();
    }
  }
}
