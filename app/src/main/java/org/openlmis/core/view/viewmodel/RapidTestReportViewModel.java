package org.openlmis.core.view.viewmodel;

import org.joda.time.DateTime;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RapidTestReportViewModel implements Serializable {
    Period period;
    Status status;
    private DateTime syncedTime;

    RapidTestFormItemViewModel item_PUB_PHARMACY = new RapidTestFormItemViewModel(PUB_PHARMACY);
    RapidTestFormItemViewModel item_MATERNITY = new RapidTestFormItemViewModel(MATERNITY);
    RapidTestFormItemViewModel item_GENERAL_WARD = new RapidTestFormItemViewModel(GENERAL_WARD);
    RapidTestFormItemViewModel item_ACC_EMERGENCY = new RapidTestFormItemViewModel(ACC_EMERGENCY);
    RapidTestFormItemViewModel item_MOBILE_UNIT = new RapidTestFormItemViewModel(MOBILE_UNIT);
    RapidTestFormItemViewModel item_LABORATORY = new RapidTestFormItemViewModel(LABORATORY);
    RapidTestFormItemViewModel item_UATS = new RapidTestFormItemViewModel(UATS);
    RapidTestFormItemViewModel item_PNCTL = new RapidTestFormItemViewModel(PNCTL);
    RapidTestFormItemViewModel item_PAV = new RapidTestFormItemViewModel(PAV);
    RapidTestFormItemViewModel item_DENTAL_WARD = new RapidTestFormItemViewModel(DENTAL_WARD);
    RapidTestFormItemViewModel item_UNPACK_KIT = new RapidTestFormItemViewModel(UNPACK_KIT);


    private ProgramDataForm rapidTestForm;

    public static long DEFAULT_FORM_ID = 0;

    public RapidTestReportViewModel(Period period) {
        this.period = period;
        status = Status.MISSING;
    }

    public RapidTestReportViewModel(ProgramDataForm programDataForm) {
        setRapidTestForm(programDataForm);
        period = Period.of(programDataForm.getPeriodBegin());
        setFormItemViewModels(programDataForm.getProgramDataFormItemListWrapper());
    }

    private void setFormItemViewModels(List<ProgramDataFormItem> programDataFormItemList) {
        for (ProgramDataFormItem item : programDataFormItemList) {
            switch (item.getName()) {
                case  PUB_PHARMACY:
                    item_PUB_PHARMACY.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case MATERNITY:
                    item_MATERNITY.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case GENERAL_WARD:
                    item_GENERAL_WARD.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case ACC_EMERGENCY:
                    item_ACC_EMERGENCY.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case MOBILE_UNIT:
                    item_MOBILE_UNIT.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case LABORATORY:
                    item_LABORATORY.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case UATS:
                    item_UATS.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case PNCTL:
                    item_PNCTL.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case PAV:
                    item_PAV.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case DENTAL_WARD:
                    item_DENTAL_WARD.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
                case UNPACK_KIT:
                    item_UNPACK_KIT.setColumnValue(item.getProgramDataColumnCode(), item.getValue());
                    break;
            }
        }
    }

    public void setRapidTestForm(ProgramDataForm rapidTestForm) {
        this.rapidTestForm = rapidTestForm;
        switch (rapidTestForm.getStatus()) {
            case DRAFT:
            case SUBMITTED:
                this.status = Status.INCOMPLETE;
                break;
            case AUTHORIZED:
                this.status = Status.COMPLETED;
                break;
            default:
                this.status = Status.MISSING;
        }
        if (rapidTestForm.isSynced()) {
            this.status = Status.SYNCED;
        }
    }

    public DateTime getSyncedTime() {
        return syncedTime;
    }

    public enum Status {
        MISSING(0),
        INCOMPLETE(1),
        COMPLETED(2),
        SYNCED(3);

        public int type;

        Status(int type) {
            this.type = type;
        }
    }

    public static final String PUB_PHARMACY = "PUB_PHARMACY";
    public static final String MATERNITY = "MATERNITY";
    public static final String GENERAL_WARD = "GENERAL_WARD";
    public static final String ACC_EMERGENCY = "ACC_EMERGENCY";
    public static final String MOBILE_UNIT = "MOBILE_UNIT";
    public static final String LABORATORY = "LABORATORY";
    public static final String UATS = "UATS";
    public static final String PNCTL = "PNCTL";
    public static final String PAV = "PAV";
    public static final String DENTAL_WARD = "DENTAL_WARD";
    public static final String UNPACK_KIT = "UNPACK_KIT";
}

