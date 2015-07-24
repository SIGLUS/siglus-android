package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable(tableName = "adjustment_reasons")
public class AdjustmentReason {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    String reasonId;

    @DatabaseField
    String reason;

    @DatabaseField
    boolean positive;
}
