package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "program_data_columns")
public class ProgramDataColumn extends BaseModel {
    @DatabaseField
    String code;

    @DatabaseField
    String label;

    @DatabaseField
    String description;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Program program;

    public ProgramDataColumn(String code) {
        this.code = code;
    }
}
