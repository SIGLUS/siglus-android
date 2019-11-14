package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RegimenItem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import static org.openlmis.core.view.viewmodel.ALGridViewModel.SUFFIX_LENGTH;

@Data
public class ALReportItemViewModel implements Serializable {
    private ALGridViewModel gridOne = new ALGridViewModel(ALGridViewModel.ALColumnCode.OneColumn);
    private ALGridViewModel gridTwo = new ALGridViewModel(ALGridViewModel.ALColumnCode.TwoColumn);
    private ALGridViewModel gridThree = new ALGridViewModel(ALGridViewModel.ALColumnCode.ThreeColumn);
    private ALGridViewModel gridFour = new ALGridViewModel(ALGridViewModel.ALColumnCode.FourColumn);

    private List<ALGridViewModel> alGridViewModelList = Arrays.asList(gridOne, gridTwo, gridThree, gridFour);
    private Map<String, ALGridViewModel> alGridViewModelMap = new HashMap<>();
    private ALReportViewModel.ALItemType itemType;
    private boolean showCheckTip = false;

    public ALReportItemViewModel(ALReportViewModel.ALItemType itemType) {
        this.itemType = itemType;
        for (ALGridViewModel viewModel : alGridViewModelList) {
            alGridViewModelMap.put(viewModel.getColumnCode().getColumnName(), viewModel);
        }
    }

    public void setColumnValue(RegimenItem regimen, Long value) {
        String regimenName = regimen.getRegimen().getName();
        String columnName = regimenName.substring(regimenName.length() - SUFFIX_LENGTH);
        alGridViewModelMap.get(columnName).setValue(regimen, value);
    }

    public boolean isComplete() {
        for (ALGridViewModel viewModel : alGridViewModelList) {
            if (viewModel.getTreatmentsValue() == null || viewModel.getExistentStockValue() == null) {
                return false;
            }
        }
        return true;
    }
}
