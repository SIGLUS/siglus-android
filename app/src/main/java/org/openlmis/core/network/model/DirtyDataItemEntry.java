package org.openlmis.core.network.model;


import org.openlmis.core.model.DirtyDataItemInfo;

import lombok.Data;

@Data
public class DirtyDataItemEntry {
    private String facilityId;
    private String productCode;
    private String jsonData;

    public DirtyDataItemEntry(DirtyDataItemInfo dirtyDataItemInfo, String facilityId) {
        this.setFacilityId(facilityId);
        this.setProductCode(dirtyDataItemInfo.getProductCode());
        this.setJsonData(dirtyDataItemInfo.getJsonData());
    }


}
