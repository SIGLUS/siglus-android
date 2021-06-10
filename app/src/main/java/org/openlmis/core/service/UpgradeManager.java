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

package org.openlmis.core.service;

import com.google.inject.Singleton;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.AutoUpdateApk;
import org.openlmis.core.utils.DateUtil;

@Singleton
public class UpgradeManager {

  private final String upgradeServerUrl = LMISApp.getContext().getResources()
      .getString(R.string.upgrade_server_url);
  private final AutoUpdateApk autoUpdateApk = new AutoUpdateApk(LMISApp.getContext(), "",
      upgradeServerUrl);

  public void triggerUpgrade() {
    if (Period.isWithinSubmissionWindow(new DateTime(DateUtil.getCurrentDate()))) {
      return; //skip self auto upgrade if it's within 18h-25th of a month
    }
    autoUpdateApk.checkUpdatesManually();
  }
}
