package org.openlmis.core.view.viewmodel;

import org.joda.time.DateTime;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
import org.openlmis.core.model.Signature;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;

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

    List<RapidTestFormItemViewModel> itemViewModelList = Arrays.asList(item_PUB_PHARMACY, item_MATERNITY, item_GENERAL_WARD, item_ACC_EMERGENCY, item_MOBILE_UNIT, item_LABORATORY, item_UATS, item_PNCTL, item_PAV, item_DENTAL_WARD);

    Map<String, RapidTestFormItemViewModel> itemViewModelMap = new HashMap<>();

    private ProgramDataForm rapidTestForm = new ProgramDataForm();

    public static long DEFAULT_FORM_ID = 0;

    public RapidTestReportViewModel(Period period) {
        this.period = period;
        status = Status.MISSING;
        setItemViewModelMap();
    }

    private void setItemViewModelMap() {
        for (RapidTestFormItemViewModel viewModel : itemViewModelList) {
            itemViewModelMap.put(viewModel.getIssueReason(), viewModel);
        }
    }

    public RapidTestReportViewModel(ProgramDataForm programDataForm) {
        setRapidTestForm(programDataForm);
        period = Period.of(programDataForm.getPeriodBegin());
        setItemViewModelMap();
        setFormItemViewModels(programDataForm.getProgramDataFormItemListWrapper());
    }

    private void setFormItemViewModels(List<ProgramDataFormItem> programDataFormItemList) {
        for (ProgramDataFormItem item : programDataFormItemList) {
            itemViewModelMap.get(item.getName()).setColumnValue(item.getProgramDataColumnCode(), item.getValue());
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

    public void convertFormViewModelToDataModel(Program program) {
        rapidTestForm.setProgram(program);
        rapidTestForm.setPeriodBegin(period.getBegin().toDate());
        rapidTestForm.setPeriodEnd(period.getEnd().toDate());
        rapidTestForm.getProgramDataFormItemListWrapper().clear();
        convertFormItemViewModelToDataModel();
    }

    private void convertFormItemViewModelToDataModel() {
        for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
            rapidTestForm.getProgramDataFormItemListWrapper().addAll(itemViewModel.convertToDataModel());
        }
        for (ProgramDataFormItem item : rapidTestForm.getProgramDataFormItemListWrapper()) {
            item.setForm(rapidTestForm);
        }
    }

    public boolean isSynced() {
        return status == Status.SYNCED;
    }

    public boolean isEditable() {
        return status.isEditable() && isDraft();
    }

    public boolean isDraft() {
        return rapidTestForm.getStatus() == null || rapidTestForm.getStatus() == ProgramDataForm.STATUS.DRAFT;
    }

    public boolean validate() {
        for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
            if (!itemViewModel.validate()) {
                return false;
            }
        }
        return true;
    }

    public void setSignature(String signature) {
        if (rapidTestForm.getSignaturesWrapper().size() == 0) {
            rapidTestForm.getSignaturesWrapper().add(new ProgramDataFormSignature(rapidTestForm, signature, Signature.TYPE.SUBMITTER));
            rapidTestForm.setStatus(ProgramDataForm.STATUS.SUBMITTED);
        } else {
            rapidTestForm.getSignaturesWrapper().add(new ProgramDataFormSignature(rapidTestForm, signature, Signature.TYPE.APPROVER));
            rapidTestForm.setStatus(ProgramDataForm.STATUS.AUTHORIZED);
            rapidTestForm.setSubmittedTime(new Date());
            status = Status.COMPLETED;
        }
    }

    public boolean isAuthorized() {
        return rapidTestForm.getStatus() == ProgramDataForm.STATUS.AUTHORIZED;
    }

    public boolean isFormEmpty() {
        for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
            if (!itemViewModel.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isSubmitted() {
        return rapidTestForm.getStatus() == ProgramDataForm.STATUS.SUBMITTED;
    }

    public enum Status {
        MISSING(true, 0),
        INCOMPLETE(true, 1),
        COMPLETED(false, 2),
        SYNCED(false, 3);

        @Getter
        private boolean editable;
        private int viewType;

        Status(boolean editable, int viewType) {
            this.editable = editable;
            this.viewType = viewType;
        }

        public int getViewType() {
            return viewType;
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

