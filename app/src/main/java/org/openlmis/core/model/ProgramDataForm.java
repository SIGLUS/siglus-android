package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Data;

@Data
@DatabaseTable(tableName = "program_data_forms")
public class ProgramDataForm extends BaseModel {
    public enum STATUS {
        DRAFT,
        SUBMITTED,
        AUTHORIZED
    }

    @DatabaseField(defaultValue = "DRAFT")
    private STATUS status;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Program program;

    @DatabaseField
    private boolean synced = false;

    @DatabaseField
    private Date periodBegin;

    @DatabaseField
    private Date periodEnd;

    @DatabaseField
    private Date submittedTime;

}
