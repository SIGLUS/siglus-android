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

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public class ClickIntervalChecker {

  private static final long CLICK_ITEM_INTERVAL = 500;
  private static final long APP_TIMEOUT_INTERVAL = Long.parseLong(
      LMISApp.getInstance().getResources().getString(R.string.app_time_out));

  private static ClickIntervalChecker instance;

  private static long lastOperateTime = 0L;
  private static long lastClickItemTime = 0L;

  private ClickIntervalChecker(){}

  public static synchronized ClickIntervalChecker getInstance() {
    if (instance == null) {
      instance = new ClickIntervalChecker();
    }
    return instance;
  }

  public static synchronized void setLastOperateTime(long newOperateTime) {
    ClickIntervalChecker.lastOperateTime = newOperateTime;
  }

  public static synchronized long getLastOperateTime() {
    return lastOperateTime;
  }

  public static long getLastClickItemTime() {
    return lastClickItemTime;
  }

  public static void setLastClickItemTime(long lastClickItemTime) {
    ClickIntervalChecker.lastClickItemTime = lastClickItemTime;
  }

  public boolean isClickLongerThanInterval() {
    long currentTimeMillis = LMISApp.getInstance().getCurrentTimeMillis();
    long elapsedTime = currentTimeMillis - getLastClickItemTime();
    return elapsedTime > CLICK_ITEM_INTERVAL;
  }

  public boolean isAppTimeOut() {
    long currentTimeMillis = LMISApp.getInstance().getCurrentTimeMillis();
    long elapsedTime = currentTimeMillis - getLastOperateTime();
    return (getLastOperateTime() > 0L) && (elapsedTime > APP_TIMEOUT_INTERVAL);
  }

  public void resetLastOperateTime() {
    setLastOperateTime(0L);
  }

//  public boolean checkInterval() {
//    long currentClickTime = LMISApp.getInstance().getCurrentTimeMillis();
//    long elapsedTime = currentClickTime - lastClickTime;
//
//    lastClickTime = currentClickTime;
//
//    //disable click when user click twice in 500ms
//    if (elapsedTime <= CLICK_ITEM_INTERVAL) {
//      return false;
//    }
//    return true;
//  }
}
