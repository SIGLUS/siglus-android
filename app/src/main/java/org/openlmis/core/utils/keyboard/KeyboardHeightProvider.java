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

package org.openlmis.core.utils.keyboard;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import org.openlmis.core.R;

public class KeyboardHeightProvider extends PopupWindow {

  private KeyboardHeightObserver observer;

  private final View popupView;

  private final View parentView;

  private final Activity activity;

  private int currentKeyboardHeight = 0;

  public KeyboardHeightProvider(Activity activity) {
    super(activity);
    this.activity = activity;
    this.popupView = ((LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE))
        .inflate(R.layout.popupwindow, null, false);
    setContentView(popupView);
    setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
    parentView = activity.findViewById(android.R.id.content);
    setWidth(0);
    setHeight(MATCH_PARENT);
    setFocusable(false);
    popupView.getViewTreeObserver().addOnGlobalLayoutListener(this::handleOnGlobalLayout);
  }

  public void start() {
    if (!isShowing() && parentView.getWindowToken() != null) {
      setBackgroundDrawable(new ColorDrawable(0));
      showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
    }
  }

  public void close() {
    this.observer = null;
    dismiss();
  }

  public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
    this.observer = observer;
  }

  private void handleOnGlobalLayout() {
    final Rect activityRect = new Rect();
    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(activityRect);
    Rect popupWindowRect = new Rect();
    popupView.getWindowVisibleDisplayFrame(popupWindowRect);
    int keyboardHeight = activityRect.height() - popupWindowRect.height();
    if (keyboardHeight != currentKeyboardHeight) {
      this.currentKeyboardHeight = keyboardHeight;
      notifyKeyboardHeightChanged(keyboardHeight);
    }
  }

  private void notifyKeyboardHeightChanged(int height) {
    if (observer != null) {
      observer.onKeyboardHeightChanged(height);
    }
  }
}