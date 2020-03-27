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
    String json_data;

    @DatabaseField
    boolean sync_status;

    @DatabaseField
    String productCode;

    public DirtyDataItemInfo(String productCode, boolean sync_status, String json_data) {

    }
}
