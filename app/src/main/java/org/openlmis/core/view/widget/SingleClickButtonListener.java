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

package org.openlmis.core.view.widget;

import android.os.Handler;
import android.view.View;
import org.openlmis.core.LMISApp;

public abstract class SingleClickButtonListener implements View.OnClickListener {

  private long minClickInterval = 500;

  private static boolean isViewClicked = false;

  public static synchronized boolean getIsViewClicked() {
    return SingleClickButtonListener.isViewClicked;
  }

  public static synchronized void setIsViewClicked(boolean isViewClicked) {
    SingleClickButtonListener.isViewClicked = isViewClicked;
  }

  private long lastClickTime;

  public abstract void onSingleClick(View v);

  @Override
  public final void onClick(View v) {
    long currentClickTime = LMISApp.getInstance().getCurrentTimeMillis();
    long elapsedTime = currentClickTime - lastClickTime;

    lastClickTime = currentClickTime;

    if (elapsedTime <= minClickInterval) {
      return;
    }
    if (!isViewClicked) {
      setIsViewClicked(true);
      startTimer();
    } else {
      return;
    }
    onSingleClick(v);
  }

  public void setMinClickInterval(long minClickInterval) {
    this.minClickInterval = minClickInterval;
  }

  private void startTimer() {
    Handler handler = new Handler();
    handler.postDelayed(() -> setIsViewClicked(false), minClickInterval);
  }
}