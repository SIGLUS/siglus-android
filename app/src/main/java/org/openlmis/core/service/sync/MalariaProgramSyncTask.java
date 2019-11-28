package org.openlmis.core.service.sync;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.network.LMISRestApi;

import java.util.List;

public class MalariaProgramSyncTask implements SyncronizableTask {
    @Inject
    private MalariaProgramRepository malariaProgramRepository;

    @Inject
    private LMISRestApi restApi;

    public MalariaProgramSyncTask() {
        restApi = LMISApp.getInstance().getRestApi();
    }

    @Override
    public void sync() {
        try {
            List<MalariaProgram> pendingForSync = malariaProgramRepository.getPendingForSync();
            restApi.syncUpMalariaPrograms(pendingForSync);
            malariaProgramRepository.bulkUpdateAsSynced(pendingForSync);
        } catch (LMISException exception) {
            new LMISException(exception, "MalariaProgramSyncTask").reportToFabric();
//            exception.reportToFabric();
        }
    }
}
