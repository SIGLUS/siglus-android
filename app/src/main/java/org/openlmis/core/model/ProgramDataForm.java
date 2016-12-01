package org.openlmis.core.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.ListUtil;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
@DatabaseTable(tableName = "program_data_forms")
public class ProgramDataForm extends BaseModel {
    public enum STATUS {
        DRAFT,
        SUBMITTED,
        AUTHORIZED
    }

    @DatabaseField(defaultValue = "DRAFT")
    private STATUS status;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Program program;

    @DatabaseField
    private boolean synced = false;

    @DatabaseField
    private Date periodBegin;

    @DatabaseField
    private Date periodEnd;

    @DatabaseField
    private Date submittedTime;

    @ForeignCollectionField
    private ForeignCollection<ProgramDataFormItem> programDataFormItemList;

    private List<ProgramDataFormItem> programDataFormItemListWrapper;

    public List<ProgramDataFormItem> getProgramDataFormItemListWrapper() {
        programDataFormItemListWrapper = ListUtil.wrapOrEmpty(programDataFormItemList, programDataFormItemListWrapper);
        return programDataFormItemListWrapper;
    }

}
