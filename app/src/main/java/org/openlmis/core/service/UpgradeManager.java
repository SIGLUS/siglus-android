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
