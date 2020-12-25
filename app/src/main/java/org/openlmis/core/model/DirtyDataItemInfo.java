package org.openlmis.core.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "dirty_data")
public class DirtyDataItemInfo extends BaseModel {
    @DatabaseField
    String jsonData;

    @DatabaseField
    boolean synced = false;

    @DatabaseField
    String productCode;

    @DatabaseField
    boolean fullyDelete = true;

    public DirtyDataItemInfo(String productCode, boolean sync_status, String json_data, boolean fullyDelete) {
        this.productCode = productCode;
        this.synced = sync_status;
        this.jsonData = json_data;
        this.fullyDelete = fullyDelete;
    }
}
