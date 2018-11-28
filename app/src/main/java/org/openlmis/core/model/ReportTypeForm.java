package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.ListUtil;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "reports_type")
public class ReportTypeForm extends BaseModel {
    @Getter
    @SerializedName("code")
    @DatabaseField
    private String code;

    @Getter
    @SerializedName("name")
    @DatabaseField
    private String name;

    @Getter
    @SerializedName("description")
    @DatabaseField
    private String description;

    @Getter
    @SerializedName("active")
    @DatabaseField
    public boolean active;

    @Getter
    @SerializedName("startTime")
    @DatabaseField
    public Date startTime;

}
