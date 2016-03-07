package org.openlmis.core.service;

import com.autoupdateapk.AutoUpdateApk;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

@Singleton
public class UpgradeManager {
    private String upgradeServerUrl = LMISApp.getContext().getResources().getString(R.string.upgrade_server_url);
    private AutoUpdateApk autoUpdateApk = new AutoUpdateApk(LMISApp.getContext(), "", upgradeServerUrl);

    public void triggerUpgrade() {
        autoUpdateApk.checkUpdatesManually();
    }
}
