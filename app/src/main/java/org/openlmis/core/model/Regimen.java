package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "regimes")
public class Regimen extends BaseModel{

    @DatabaseField
    private String name;

    @DatabaseField
    private String code;
}
