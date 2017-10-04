package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "implementation")
public class Implementation extends BaseModel{

    public Implementation() {
    }

    public Implementation(String executor, Collection<Treatment> treatments) {
        this.executor = executor;
        this.treatments = treatments;
    }

    @DatabaseField(columnName = "program", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private MalariaProgram malariaProgram;

    @DatabaseField
    private String executor;

    @ForeignCollectionField(eager = true)
    private Collection<Treatment> treatments;
}
