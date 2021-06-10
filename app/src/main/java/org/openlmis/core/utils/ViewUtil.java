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

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class ViewUtil {

  private ViewUtil() {
  }

  public static void syncViewHeight(final View leftView, final View rightView) {
    int leftHeight = leftView.getHeight();
    int rightHeight = rightView.getHeight();
    if (leftHeight > rightHeight) {
      ViewGroup.LayoutParams layoutParams = rightView.getLayoutParams();
      layoutParams.height = leftHeight;
      rightView.setLayoutParams(layoutParams);
    } else {
      ViewGroup.LayoutParams layoutParams = leftView.getLayoutParams();
      layoutParams.height = rightHeight;
      leftView.setLayoutParams(layoutParams);

      if (rightView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
        rightView
            .setLayoutParams(new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height));
      } else {
        rightView.setLayoutParams(layoutParams);
      }
    }
  }

  public static boolean checkEditTextEmpty(EditText editText) {
    if (TextUtils.isEmpty(editText.getText().toString())) {
      editText.setError(LMISApp.getContext().getString(R.string.hint_error_input));
      editText.requestFocus();
      return false;
    }
    return true;
  }
}
