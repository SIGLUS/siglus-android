package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "program_data_Basic_items")
public class ProgramDataFormBasicItem extends BaseModel {
    @Expose
    @SerializedName("code")
    @DatabaseField
    String code;

    @Expose
    @SerializedName("name")
    @DatabaseField
    String name;

    @Expose
    @SerializedName("beginningBalance")
    @DatabaseField
    private long initialAmount;

    @Expose
    @SerializedName("quantityReceived")
    @DatabaseField
    private long received;

    @Expose
    @SerializedName("quantityDispensed")
    @DatabaseField
    private Long issued;

    @Expose
    @SerializedName("totalLossesAndAdjustments")
    @DatabaseField
    private Long adjustment;

    @Expose
    @SerializedName("expirationDate")
    @DatabaseField
    private String validate;

    @Expose
    @SerializedName("stockInHand")
    @DatabaseField
    private Long inventory;
}
