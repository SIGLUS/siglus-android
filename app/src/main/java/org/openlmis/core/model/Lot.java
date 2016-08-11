package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Data;

@Data
@DatabaseTable(tableName = "lots")
public class Lot extends BaseModel{

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Product product;

    @DatabaseField
    String lotNumber;

    @DatabaseField
    Date expirationDate;
}
