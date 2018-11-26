package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
import org.openlmis.core.model.Signature;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;

import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.*;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.*;

@Data
public class RapidTestReportViewModel implements Serializable {
    Period period;
    String observataion;
    public Status status;
    private Date syncedTime;

    MovementReasonManager movementReasonManager;

    RapidTestFormItemViewModel itemTotal;
    RapidTestFormItemViewModel itemRealTotal;
    RapidTestFormItemViewModel itemAPEs;

    List<RapidTestFormItemViewModel> itemViewModelList = new ArrayList<>();
    Map<String, RapidTestFormItemViewModel> itemViewModelMap = new HashMap<>();

    List<ProgramDataFormBasicItem> basicItems = new ArrayList<>();

    private ProgramDataForm rapidTestForm = new ProgramDataForm();

    public static long DEFAULT_FORM_ID = 0;
    public static String DEFAULT_TOTAl_NULL = "";

    public RapidTestReportViewModel(Period period) {
        this.period = period;
        status = Status.MISSING;
        observataion = "";
        setupCategories();
        setItemViewModelMap();
    }

    public RapidTestReportViewModel(Period period, Status statusInput) {
        this.period = period;
        status = statusInput;
        observataion = "";
        setupCategories();
        setItemViewModelMap();
    }

    private void setupCategories() {
        movementReasonManager = MovementReasonManager.getInstance();
        List<MovementReasonManager.MovementReason> issueReasons = FluentIterable.from(movementReasonManager.buildReasonListForMovementType(MovementReasonManager.MovementType.ISSUE))
                .filter(new Predicate<MovementReasonManager.MovementReason>() {
                    @Override
                    public boolean apply(MovementReasonManager.MovementReason movementReason) {
                        return !movementReason.getCode().equals("PUB_PHARMACY");
                    }
                }).toList();

        for (MovementReasonManager.MovementReason movementReason : issueReasons) {
            RapidTestFormItemViewModel item = new RapidTestFormItemViewModel(movementReason);
            itemViewModelList.add(item);
        }

        MovementReasonManager.MovementReason totalCategory = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "TOTAL", LMISApp.getInstance().getString(R.string.total));
        MovementReasonManager.MovementReason realTotalCategory = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "realTotalCategory", LMISApp.getInstance().getString(R.string.total));
        MovementReasonManager.MovementReason totalAPES = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "APES", LMISApp.getInstance().getString(R.string.ape));

        itemTotal = new RapidTestFormItemViewModel(totalCategory);
        itemRealTotal = new RapidTestFormItemViewModel(realTotalCategory);
        itemAPEs = new RapidTestFormItemViewModel(totalAPES);
        itemAPEs.setAPEItem();

        itemViewModelList.add(itemTotal);
        itemViewModelList.add(itemAPEs);
    }

    private void setItemViewModelMap() {
        for (RapidTestFormItemViewModel viewModel : itemViewModelList) {
            itemViewModelMap.put(viewModel.getIssueReason().getCode(), viewModel);
        }
    }

    public RapidTestReportViewModel(ProgramDataForm programDataForm) {
        setRapidTestForm(programDataForm);
        DateTime beginDateTime = new DateTime(programDataForm.getPeriodBegin());
        DateTime endDateTime = new DateTime(programDataForm.getPeriodEnd());
        period = new Period(beginDateTime, endDateTime);
        observataion = programDataForm.getObservataion();
        setupCategories();
        setItemViewModelMap();
        setFormItemViewModels(programDataForm.getProgramDataFormItemListWrapper());
    }

    private void setFormItemViewModels(List<ProgramDataFormItem> programDataFormItemList) {
        for (ProgramDataFormItem item : programDataFormItemList) {
            itemViewModelMap.get(item.getName()).setColumnValue(item.getProgramDataColumn(), item.getValue());
        }
        addCompatibleWithNotSubmitUnjustified();
        addCompatibleWithNotSubmitAPE();
        for (ColumnCode columnCode : ColumnCode.values()) {
            updateTotal(columnCode, consumption);
            updateTotal(columnCode, positive);
            updateTotal(columnCode, unjustified);
        }
        updateAPEWaring();
    }

    private void addCompatibleWithNotSubmitUnjustified() {
        for (RapidTestFormItemViewModel formItemViewModel : itemViewModelList) {
            formItemViewModel.updateUnjustifiedColumn();
        }
    }

    private void addCompatibleWithNotSubmitAPE() {
        List<String> columnList = Arrays.asList(new String[]{"HIVDetermine", "HIVUnigold", "Syphillis", "Malaria"});
        for (String columnName : columnList) {
            if (isNeedAPE(columnName)) {
                RapidTestFormGridViewModel viewModel = itemAPEs.rapidTestFormGridViewModelMap.get(StringUtils.upperCase(columnName));
                itemAPEs.updateNoValueGridRowToZero(viewModel);
            }
        }
    }

    private Boolean isNeedAPE(String columnName) {
        for (RapidTestFormItemViewModel viewModel : itemViewModelList) {
            if (!viewModel.rapidTestFormGridViewModelMap.get(StringUtils.upperCase(columnName)).isEmpty()) {
                return true;
            }
        }
        return false;
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

    public Date getSyncedTime() {
        return rapidTestForm.getSubmittedTime();
    }

    public void convertFormViewModelToDataModel(Program program) {
        rapidTestForm.setProgram(program);
        rapidTestForm.setPeriodBegin(period.getBegin().toDate());
        rapidTestForm.setPeriodEnd(period.getEnd().toDate());
        rapidTestForm.setObservataion(observataion);
        rapidTestForm.getProgramDataFormItemListWrapper().clear();
        rapidTestForm.setFormBasicItems(basicItems);
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

    public boolean validatePositive() {
        for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
            if (itemViewModel == itemAPEs) {
                continue;
            }
            if (!itemViewModel.validatePositive()) {
                return false;
            }
        }
        return true;
    }

    public boolean validateUnjustified() {
        for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
            if (itemViewModel == itemAPEs) {
                continue;
            }
            if (!itemViewModel.validateUnjustified()) {
                return false;
            }
        }
        return true;
    }

    public boolean validateAPES() {
        for (RapidTestFormGridViewModel itemViewModel : itemAPEs.rapidTestFormGridViewModelList) {
            if (itemViewModel.isNeedAddGridViewWarning()) {
                return false;
            }
        }
        return true;
    }

    public boolean validateOnlyAPES() {
        return itemRealTotal.isEmpty() && !itemAPEs.isEmpty();
    }

    public void addSignature(String signature) {
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

    public void updateTotal(ColumnCode columnCode, RapidTestGridColumnCode gridColumnCode) {
        itemTotal.clearValue(columnCode, gridColumnCode);
        itemRealTotal.clearValue(columnCode, gridColumnCode);
        Total total = new Total();
        total.longTotal = 0;
        total.stringTotal = DEFAULT_TOTAl_NULL;
        for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
            if (itemViewModel == itemAPEs) {
                continue;
            }
            RapidTestFormGridViewModel gridViewModel = itemViewModel.getRapidTestFormGridViewModelMap().get(columnCode.toString());
            total = calculateTotalLogic(total, gridViewModel, gridColumnCode);
        }
        setTotalRowValue(itemTotal, columnCode, gridColumnCode, String.valueOf(total.longTotal));
        setTotalRowValue(itemRealTotal, columnCode, gridColumnCode, total.stringTotal);
    }

    public void updateAPEWaring() {
        for (RapidTestFormGridViewModel viewModel : itemRealTotal.rapidTestFormGridViewModelList) {
            RapidTestFormGridViewModel APEViewModel = itemAPEs.rapidTestFormGridViewModelMap.get(viewModel.getColumnCode().name().toUpperCase());
            if (!viewModel.isEmpty()) {
                APEViewModel.isNeedAllAPEValue = true;
            } else {
                APEViewModel.isNeedAllAPEValue = false;
            }
        }
    }

    private Total calculateTotalLogic(Total total, RapidTestFormGridViewModel gridViewModel, RapidTestGridColumnCode gridColumnCode) {
        switch (gridColumnCode) {
            case consumption:
                if (!gridViewModel.getConsumptionValue().equals("")) {
                    total.longTotal += Long.parseLong(gridViewModel.getConsumptionValue());
                    total.stringTotal = String.valueOf(total.longTotal);
                }
                break;
            case positive:
                if (!gridViewModel.getPositiveValue().equals("")) {
                    total.longTotal += Long.parseLong(gridViewModel.getPositiveValue());
                    total.stringTotal = String.valueOf(total.longTotal);
                }
                break;
            case unjustified:
                if (!gridViewModel.getUnjustifiedValue().equals("")) {
                    total.longTotal += Long.parseLong(gridViewModel.getUnjustifiedValue());
                    total.stringTotal = String.valueOf(total.longTotal);
                }
                break;
        }

        return total;
    }

    private void setTotalRowValue(RapidTestFormItemViewModel totalItem, ColumnCode columnCode, RapidTestGridColumnCode gridColumnCode, String total) {
        switch (gridColumnCode) {
            case consumption:
                totalItem.getRapidTestFormGridViewModelMap().get(columnCode.toString()).setConsumptionValue(total);
                break;
            case positive:
                totalItem.getRapidTestFormGridViewModelMap().get(columnCode.toString()).setPositiveValue(total);
                break;
            case unjustified:
                totalItem.getRapidTestFormGridViewModelMap().get(columnCode.toString()).setUnjustifiedValue(total);
                break;
        }
    }

    public enum Status {
        MISSING(true, 0),
        INCOMPLETE(true, 1),
        COMPLETED(false, 2),
        SYNCED(false, 3),
        FIRST_MISSING(false, 4),
        UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD(false, 5),
        CANNOT_DO_MONTHLY_INVENTORY(false, 6),
        COMPLETE_INVENTORY(false, 7);


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
}

class Total {
    long longTotal;
    String stringTotal;

    // 构造函数
    public Total() {
        super();
    }

}
