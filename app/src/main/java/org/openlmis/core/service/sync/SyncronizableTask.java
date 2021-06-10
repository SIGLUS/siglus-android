package org.openlmis.core.service.sync;

import org.openlmis.core.exceptions.LMISException;

public interface SyncronizableTask {

  void sync() throws LMISException;
}
