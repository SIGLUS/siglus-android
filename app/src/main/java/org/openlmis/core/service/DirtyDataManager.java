package org.openlmis.core.service;

import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.network.LMISRestApi;

@Singleton
public class DirtyDataManager {

    protected LMISRestApi lmisRestApi;

    public DirtyDataManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public void correctData() {

    }
}
