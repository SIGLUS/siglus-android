package org.openlmis.core.network.model;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownArchivedProductCodesResponse {
    List<String> archivedProductCodes;
}
