package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.ColumnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Data
public class RapidTestFormItemViewModel {

    MovementReasonManager.MovementReason issueReason;

    RapidTestFormGridViewModel gridHIVDetermine = new RapidTestFormGridViewModel(ColumnCode.HIVDETERMINE);
    RapidTestFormGridViewModel gridHIVUnigold = new RapidTestFormGridViewModel(ColumnCode.HIVUNIGOLD);
    RapidTestFormGridViewModel gridSyphillis = new RapidTestFormGridViewModel(ColumnCode.SYPHILLIS);
    RapidTestFormGridViewModel gridMalaria = new RapidTestFormGridViewModel(ColumnCode.MALARIA);

    List<RapidTestFormGridViewModel> rapidTestFormGridViewModelList = Arrays.asList(gridHIVDetermine, gridHIVUnigold, gridSyphillis, gridMalaria);

    Map<ColumnCode, RapidTestFormGridViewModel> rapidTestFormGridViewModelMap = new HashMap<>();

    public RapidTestFormItemViewModel(MovementReasonManager.MovementReason issueReason) {
        this.issueReason = issueReason;
        for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
            rapidTestFormGridViewModelMap.put(viewModel.getColumnCode(), viewModel);
        }
    }

    public void setColumnValue(ProgramDataColumn column, int value) {
        rapidTestFormGridViewModelMap.get(ColumnCode.valueOf(column.getCode().split("_")[1]))
                .setValue(column, value);
    }

    public void updateUnjustifiedColumn() {
        for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
            if (viewModel.isAddUnjustified()) {
                viewModel.unjustifiedValue = "0";
            }
        }
    }

    public void setAPEItem() {
        for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
            viewModel.isAPE = true;
        }
    }

    public void updateNoValueGridRowToZero(RapidTestFormGridViewModel viewModel) {
        viewModel.consumptionValue = StringUtils.isEmpty(viewModel.consumptionValue) ? "0" : viewModel.consumptionValue;
        viewModel.positiveValue = StringUtils.isEmpty(viewModel.positiveValue) ? "0" : viewModel.positiveValue;
        viewModel.unjustifiedValue = StringUtils.isEmpty(viewModel.unjustifiedValue) ? "0" : viewModel.unjustifiedValue;
    }

    public List<ProgramDataFormItem> convertToDataModel() {
        if (issueReason.getDescription().equals(LMISApp.getInstance().getString(R.string.total))) {
            return newArrayList();
        }

        List<ProgramDataFormItem> programDataFormItems = new ArrayList<>();
        for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
            programDataFormItems.addAll(gridViewModel.convertFormGridViewModelToDataModel(issueReason));
        }
        return programDataFormItems;
    }

    public boolean validatePositive() {
        for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
            if (!gridViewModel.validatePositive()) {
                return false;
            }
        }
        return true;
    }

    public boolean validateUnjustified() {
        for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
            if (!gridViewModel.validateUnjustified()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
            if (!gridViewModel.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void clearValue(ColumnCode columnCode, RapidTestGridColumnCode gridColumnCode) {
        rapidTestFormGridViewModelMap.get(columnCode).clear(gridColumnCode);
    }
}
