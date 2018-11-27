package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "program_data_Basic_items")
public class ProgramDataFormBasicItem extends BaseModel {
    @Expose
    @SerializedName("productCode")
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Product product;

    @Expose
    @SerializedName("beginningBalance")
    @DatabaseField
    private Long initialAmount;

    @DatabaseField(defaultValue = "false")
    private Boolean isCustomAmount;

    @Expose
    @SerializedName("quantityReceived")
    @DatabaseField
    private long received;

    @Expose
    @SerializedName("quantityDispensed")
    @DatabaseField
    private long issued;

    @Expose
    @SerializedName("totalLossesAndAdjustments")
    @DatabaseField
    private long adjustment;

    @Expose
    @SerializedName("expirationDate")
    @DatabaseField
    private String validate;

    @Expose
    @SerializedName("stockInHand")
    @DatabaseField
    private Long inventory;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private ProgramDataForm form;

}
