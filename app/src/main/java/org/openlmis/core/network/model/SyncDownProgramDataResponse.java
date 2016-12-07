package org.openlmis.core.network.model;

import org.openlmis.core.model.ProgramDataForm;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownProgramDataResponse {
    private List<ProgramDataForm> programDataForms;
}