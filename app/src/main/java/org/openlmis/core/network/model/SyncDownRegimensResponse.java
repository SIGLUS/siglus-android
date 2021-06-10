package org.openlmis.core.network.model;


import org.openlmis.core.model.Regimen;

import java.util.List;

import lombok.Data;


@Data
public class SyncDownRegimensResponse {

    List<Regimen> regimenList;
}
