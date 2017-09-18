package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "patient_data_report")
public class PatientDataReport extends BaseModel {

    @DatabaseField
    private DateTime reportedDate;

    public DateTime getReportedDate() {
        return reportedDate;
    }
}
