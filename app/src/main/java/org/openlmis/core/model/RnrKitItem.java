package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable(tableName = "rnr_kit_items")
public class RnrKitItem extends BaseModel {

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private RnRForm form;

    @Expose
    @DatabaseField
    private Integer kitsReceived;

    @Expose
    @DatabaseField
    private Integer kitsOpened;

    @Expose
    @DatabaseField
    private String kitCode;

    public static String US_KIT = "SCOD10";

    public static String APE_KIT = "SCOD12";
}
