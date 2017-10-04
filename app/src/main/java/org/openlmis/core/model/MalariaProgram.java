package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "malaria_program")
public class MalariaProgram extends BaseModel{

    public MalariaProgram() {}

    public MalariaProgram(DateTime reportedDate, DateTime startDatePeriod, DateTime endDatePeriod, boolean statusMissing, boolean statusDraft, Collection<Implementation> implementations) {
        this.reportedDate = reportedDate;
        this.startDatePeriod = startDatePeriod;
        this.endDatePeriod = endDatePeriod;
        this.statusMissing = statusMissing;
        this.statusDraft = statusDraft;
        this.implementations = implementations;
    }

    @DatabaseField
    private DateTime reportedDate;

    @DatabaseField
    private DateTime startDatePeriod;

    @DatabaseField
    private DateTime endDatePeriod;

    @DatabaseField
    private boolean statusMissing;

    @DatabaseField
    private boolean statusDraft;

    @DatabaseField
    private boolean statusComplete;

    @DatabaseField
    private boolean statusSynced;


    @ForeignCollectionField(columnName = "implementations", eager = true)
    private Collection<Implementation> implementations;
}
