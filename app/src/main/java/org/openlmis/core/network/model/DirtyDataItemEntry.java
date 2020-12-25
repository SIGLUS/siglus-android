package org.openlmis.core.network.model;

import lombok.Data;

@Data
public class DirtyDataItemEntry {
    private String productCode;
    private String clientMovements;
    private boolean fullyDelete = true;

    public DirtyDataItemEntry(String productCode, String jsonData) {
        this.productCode = productCode;
        this.clientMovements = jsonData;
    }
}
