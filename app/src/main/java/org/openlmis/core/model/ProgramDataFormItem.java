package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "program_data_items")
public class ProgramDataFormItem extends BaseModel {
    @DatabaseField
    private String name;

    @DatabaseField
    private String programDataColumnCode;

    @DatabaseField
    private int value;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private ProgramDataForm form;

    public ProgramDataFormItem(String name, String programDataColumnCode, int value) {
        this.name = name;
        this.programDataColumnCode = programDataColumnCode;
        this.value = value;
    }
}
