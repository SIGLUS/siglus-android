package org.openlmis.core.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.DateUtil;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "cmm")
public class Cmm extends BaseModel {

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    StockCard stockCard;

    @DatabaseField
    float cmmValue;

    @DatabaseField
    Date periodBegin;

    @DatabaseField
    Date periodEnd;

    @DatabaseField
    private boolean synced = false;
}
