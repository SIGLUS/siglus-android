package org.openlmis.core.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.util.Collection;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DatabaseTable(tableName = "malaria_program")
@NoArgsConstructor
public class MalariaProgram extends BaseModel {

    @DatabaseField
    private DateTime reportedDate;

    @DatabaseField
    private DateTime startPeriodDate;

    @DatabaseField
    private DateTime endPeriodDate;

    @DatabaseField(dataType = DataType.ENUM_INTEGER)
    private PatientDataProgramStatus status;

    @ForeignCollectionField(columnName = "implementations", eager = true, maxEagerLevel = 2)
    private Collection<Implementation> implementations;

    @DatabaseField
    private String username;

    public MalariaProgram(String username, DateTime reportedDate, DateTime startPeriodDate,
                          DateTime endDatePeriod,
                          Collection<Implementation> implementations) {
        this.username = username;
        this.reportedDate = reportedDate;
        this.startPeriodDate = startPeriodDate;
        this.endPeriodDate = endDatePeriod;
        this.implementations = implementations;
    }
}
