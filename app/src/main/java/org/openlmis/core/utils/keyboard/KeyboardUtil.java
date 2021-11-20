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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {

  private KeyboardUtil() {
    // do nothing
  }

  public static void hideKeyboard(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static void hideKeyboard(Activity activity) {
    InputMethodManager mImm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (mImm != null && mImm.isActive() && activity.getCurrentFocus() != null) {
      mImm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
  }
}
