package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;

@Data
public class SyncUpDeletedMovementResponse {

  List<String> errorCodes;
}
