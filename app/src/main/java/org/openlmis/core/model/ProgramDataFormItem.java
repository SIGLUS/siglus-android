package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "program_data_items")
public class ProgramDataFormItem extends BaseModel {
    @Expose
    @DatabaseField
    private String name;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private ProgramDataColumn programDataColumn;

    @Expose
    @DatabaseField
    private int value;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private ProgramDataForm form;

    @DatabaseField(defaultValue = "")
    private String Observataion;

    public ProgramDataFormItem(String name, ProgramDataColumn programDataColumn, int value) {
        this.name = name;
        this.programDataColumn = programDataColumn;
        this.value = value;
    }
}
