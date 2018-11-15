package org.openlmis.core.view.viewmodel;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ALReportItemViewModel implements Serializable {
    ALGridViewModel gridOne = new ALGridViewModel(ALGridViewModel.ALColumnCode.OneColumn);
    ALGridViewModel gridTwo = new ALGridViewModel(ALGridViewModel.ALColumnCode.TwoColumn);
    ALGridViewModel gridThree = new ALGridViewModel(ALGridViewModel.ALColumnCode.ThreeColumn);
    ALGridViewModel gridFour = new ALGridViewModel(ALGridViewModel.ALColumnCode.FourColumn);

    public List<ALGridViewModel> rapidTestFormGridViewModelList = Arrays.asList(gridOne, gridTwo, gridThree, gridFour);
    public Map<String, ALGridViewModel> rapidTestFormGridViewModelMap = new HashMap<>();
    private ALReportViewModel.ALItemType itemType;
    public boolean showCheckTip = false;

    public ALReportItemViewModel(ALReportViewModel.ALItemType itemType) {
        this.itemType = itemType;
        for (ALGridViewModel viewModel : rapidTestFormGridViewModelList) {
            rapidTestFormGridViewModelMap.put(viewModel.getColumnCode().getColumnName(), viewModel);
        }
    }

    public void setColumnValue(RegimenItem regimen, Long value) {
       String regimenName = regimen.getRegimen().getName();
       String columnName = regimenName.substring(regimenName.length()-3, regimenName.length());
        rapidTestFormGridViewModelMap.get(columnName).setValue(regimen, value);
    }

    public Boolean isComplete() {
        for (ALGridViewModel viewModel : rapidTestFormGridViewModelList) {
            if (viewModel.treatmentsValue == null || viewModel.existentStockValue == null) {
                return false;
            }
        }
        return true;
    }
}
