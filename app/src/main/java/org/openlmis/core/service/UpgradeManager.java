package org.openlmis.core.service;

import com.autoupdateapk.AutoUpdateApk;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;

@Singleton
public class UpgradeManager {
    private String upgradeServerUrl = LMISApp.getContext().getResources().getString(R.string.upgrade_server_url);
    private AutoUpdateApk autoUpdateApk = new AutoUpdateApk(LMISApp.getContext(), "", upgradeServerUrl);

    public void triggerUpgrade() {
        if (Period.isWithinSubmissionWindow(DateTime.now())) {
            return; //skip self auto upgrade if it's within 18th-25th of a month
        }

        autoUpdateApk.checkUpdatesManually();
    }
}
