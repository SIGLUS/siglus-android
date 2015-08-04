package org.openlmis.core.model;


import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "MIMIA_forms")
public class MIMIAForm extends BaseModel {

    @DatabaseField
    private int newPatients;
    @DatabaseField
    private int sustaining;
    @DatabaseField
    private int alteration;
    @DatabaseField
    private int totalMonthDispense;
    @DatabaseField
    private int totalPatients;

    @DatabaseField
    private int PTV;
    @DatabaseField
    private int PPE;

    @DatabaseField
    private String comments;

    @ForeignCollectionField()
    private ForeignCollection<RegimenItem> regimenItemList;

    @ForeignCollectionField()
    private ForeignCollection<MIMIAProductItem> productItemList;
}
