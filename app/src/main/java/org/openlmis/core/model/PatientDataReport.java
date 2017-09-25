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

    @DatabaseField
    private DateTime startDatePeriod;

    @DatabaseField
    private DateTime endDatePeriod;

    @DatabaseField
    private String type;

    @DatabaseField
    private long currentTreatment6x1;

    @DatabaseField
    private long currentTreatment6x2;

    @DatabaseField
    private long currentTreatment6x3;

    @DatabaseField
    private long currentTreatment6x4;

    @DatabaseField
    private long existingStock6x1;

    @DatabaseField
    private long existingStock6x2;

    @DatabaseField
    private long existingStock6x3;

    @DatabaseField
    private long existingStock6x4;

    @DatabaseField
    private boolean statusMissing;

    @DatabaseField
    private boolean statusDraft;

    @DatabaseField
    private boolean statusComplete;

    @DatabaseField
    private boolean statusSynced;

    public DateTime getReportedDate() {
        return reportedDate;
    }
}
