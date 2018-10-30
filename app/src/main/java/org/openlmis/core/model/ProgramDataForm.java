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

import lombok.Data;
import lombok.Getter;

@Data
@DatabaseTable(tableName = "program_data_forms")
public class ProgramDataForm extends BaseModel {
    public enum STATUS {
        DRAFT,
        SUBMITTED,
        AUTHORIZED
    }

    @Getter
    @DatabaseField(defaultValue = "DRAFT")
    private STATUS status;

    @Getter
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Program program;

    @Getter
    @DatabaseField
    private boolean synced = false;

    @Getter
    @Expose
    @DatabaseField
    public Date periodBegin;

    @Getter
    @Expose
    @DatabaseField
    public Date periodEnd;

    @Getter
    @Expose
    @DatabaseField
    private Date submittedTime;

    @Getter
    @Expose
    @DatabaseField(defaultValue = "")
    private String observataion;

    @ForeignCollectionField
    private ForeignCollection<ProgramDataFormItem> programDataFormItemList;

    @Expose
    @SerializedName("programDataFormItems")
    private List<ProgramDataFormItem> programDataFormItemListWrapper;

    @ForeignCollectionField()
    private ForeignCollection<ProgramDataFormSignature> signatures;

    @Expose
    @SerializedName("programDataFormSignatures")
    private List<ProgramDataFormSignature> signaturesWrapper;

    public List<ProgramDataFormSignature> getSignaturesWrapper() {
        signaturesWrapper = ListUtil.wrapOrEmpty(signatures, signaturesWrapper);
        return signaturesWrapper;
    }

    public List<ProgramDataFormItem> getProgramDataFormItemListWrapper() {
        programDataFormItemListWrapper = ListUtil.wrapOrEmpty(programDataFormItemList, programDataFormItemListWrapper);
        return programDataFormItemListWrapper;
    }

}
