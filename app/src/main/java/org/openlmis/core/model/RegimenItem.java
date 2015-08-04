package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "regime_items")
public class RegimenItem extends BaseModel{

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private MIMIAForm form;

    @DatabaseField(foreign = true)
    private Regimen regimen;

    @DatabaseField
    private int amount;
}
