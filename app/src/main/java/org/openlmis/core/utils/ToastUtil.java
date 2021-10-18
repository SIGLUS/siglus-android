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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
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
    showSystem(text);
  }

  public static void show(int resId) {
    show(LMISApp.getContext().getString(resId));
  }

  public static void showInCenter(int text) {
    showSystem(LMISApp.getContext().getResources().getText(text).toString());
  }

  public static void showSystem(CharSequence text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    Toast toast = makeText(LMISApp.getContext(), text, Toast.LENGTH_LONG);
    setSystemToastLayout(toast);
    toast.show();
  }

  private static void showSystemToast(String text) {
    Toast.makeText(LMISApp.getContext(), text, Toast.LENGTH_LONG).show();
  }

  private static void showActivityToast(String text, int gravity, int yOffset) {
    WindowManager wm = (WindowManager) LMISApp.getContext().getSystemService(Context.WINDOW_SERVICE);
    int width = wm.getDefaultDisplay().getWidth();
    Activity currentActivity = LMISApp.getActiveActivity();
    if (currentActivity != null) {
      activityToast = SuperActivityToast.create(currentActivity, new Style(), Style.TYPE_STANDARD);
      activityToast.setText(text)
          .setColor(LMISApp.getContext().getResources().getColor(R.color.color_6b6b6b))
          .setDuration(Style.DURATION_VERY_LONG)
          .setGravity(Gravity.CENTER, 0,
              (int) LMISApp.getContext().getResources().getDimension(R.dimen.px_180))
          .setWidth(width - 100)
          .setFrame(Style.FRAME_STANDARD)
          .setAnimations(Style.ANIMATIONS_FADE)
          .show();
    }
  }

  private static void setSystemToastLayout(Toast toast) {
    toast.setGravity(Gravity.BOTTOM, 0, (int) LMISApp.getContext().getResources().getDimension(R.dimen.px_50));
    View view = toast.getView();
    if (view != null) {
      view.setPadding(20, 10, 20, 10);
      view.setBackgroundResource(R.drawable.toast_bg);
      TextView textView = view.findViewById(android.R.id.message);
      textView.setTextColor(ContextCompat.getColor(LMISApp.getContext(), R.color.color_white));
      textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, LMISApp.getInstance().getResources()
          .getDimension(R.dimen.px_18));
    }
  }
}
