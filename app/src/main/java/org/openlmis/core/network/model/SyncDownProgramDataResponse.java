package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.ProgramDataForm;

@Data
public class SyncDownProgramDataResponse {

  private List<ProgramDataForm> programDataForms;
}