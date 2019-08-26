package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "regime_short_code")
public class RegimeShortCode extends BaseModel {
    @DatabaseField
    String code;

    @DatabaseField
    private String shortCode;

    @DatabaseField
    private Regimen.RegimeType type;
}
