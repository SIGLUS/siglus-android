package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable(tableName = "program_data_items")
public class ProgramDataFormItem extends BaseModel {
    @DatabaseField
    private String programDataColumnCode;

    @DatabaseField
    private String name;

    @DatabaseField
    private int value;
}
