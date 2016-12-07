package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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

    @Expose
    @SerializedName("columnCode")
    @DatabaseField(columnName = "program_data_column_code")
    private String programDataColumnCode;

    @Expose
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
