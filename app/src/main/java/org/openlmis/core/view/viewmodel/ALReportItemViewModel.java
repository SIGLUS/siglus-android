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

    public ALReportItemViewModel(ALReportViewModel.ALItemType itemType) {
        this.itemType = itemType;
        for (ALGridViewModel viewModel : rapidTestFormGridViewModelList) {
            rapidTestFormGridViewModelMap.put(StringUtils.upperCase(viewModel.getColumnCode().name()), viewModel);
        }
    }

    public void setColumnValue(RegimenItem regimen, Long value) {
       String regimenName = regimen.getRegimen().getName();
       String columnName = regimenName.substring(regimenName.length()-2, regimenName.length());
        rapidTestFormGridViewModelMap.get(columnName).setValue(regimen, value);
    }

//    public void setColumnValue(ProgramDataColumn column, int value) {
//        String[] columnNames = column.getCode().split("_");
//        String columnName = columnNames[1];
//        rapidTestFormGridViewModelMap.get(columnName).setValue(column, value);
//    }
//
//    public void updateUnjustifiedColumn() {
//        for (RapidTestFormGridViewModel viewModel: rapidTestFormGridViewModelList) {
//            if(viewModel.isAddUnjustified()) {
//                viewModel.unjustifiedValue = "0";
//            }
//        }
//    }
//
//    public void setAPEItem() {
//        for (RapidTestFormGridViewModel viewModel: rapidTestFormGridViewModelList) {
//            viewModel.isAPE = true;
//        }
//    }

}
