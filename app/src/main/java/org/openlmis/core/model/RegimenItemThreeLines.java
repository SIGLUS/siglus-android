package org.openlmis.core.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "regime_three_lines")
public class RegimenItemThreeLines extends BaseModel {
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private RnRForm form;

    @Expose
    @SerializedName("code")
    @DatabaseField
    private String regimeTypes;

    @Expose
    @SerializedName("patientsOnTreatment")
    @DatabaseField
    private Long patientsAmount;

    @Expose
    @SerializedName("comunitaryPharmacy")
    @DatabaseField
    private Long pharmacyAmount;

    public RegimenItemThreeLines(String type) {
        this.regimeTypes = type;
    }
    public RegimenItemThreeLines() {
        super();
    }
}
