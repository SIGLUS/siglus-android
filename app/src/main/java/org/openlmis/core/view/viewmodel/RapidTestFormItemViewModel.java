package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Data
public class RapidTestFormItemViewModel {
    String issueReason;

    RapidTestFormGridViewModel gridHIVDetermine = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.HIVDetermine);
    RapidTestFormGridViewModel gridHIVUnigold = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.HIVUnigold);
    RapidTestFormGridViewModel gridSyphillis = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.Syphillis);
    RapidTestFormGridViewModel gridMalaria = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.Malaria);

    List<RapidTestFormGridViewModel> rapidTestFormGridViewModelList = Arrays.asList(gridHIVDetermine, gridHIVUnigold, gridSyphillis, gridMalaria);

    Map<String, RapidTestFormGridViewModel> rapidTestFormGridViewModelMap = new HashMap<>();

    public RapidTestFormItemViewModel(String issueReason) {
        this.issueReason = issueReason;
        for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
            rapidTestFormGridViewModelMap.put(StringUtils.upperCase(viewModel.getColumnCode().name()), viewModel);
        }
    }

    public void setColumnValue(ProgramDataColumn column, int value) {
        String[] columnNames = column.getCode().split("_");
        String columnName = columnNames[1];
        rapidTestFormGridViewModelMap.get(columnName).setValue(column, value);
    }

    public static final String CONSUME_HIVDETERMINE = "CONSUME_HIVDETERMINE";
    public static final String POSITIVE_HIVDETERMINE = "POSITIVE_HIVDETERMINE";
    public static final String CONSUME_HIVUNIGOLD = "CONSUME_HIVUNIGOLD";
    public static final String POSITIVE_HIVUNIGOLD = "POSITIVE_HIVUNIGOLD";
    public static final String CONSUME_SYPHILLIS = "CONSUME_SYPHILLIS";
    public static final String POSITIVE_SYPHILLIS = "POSITIVE_SYPHILLIS";
    public static final String CONSUME_MALARIA = "CONSUME_MALARIA";
    public static final String POSITIVE_MALARIA = "POSITIVE_MALARIA";

    public List<ProgramDataFormItem> convertToDataModel() {
        if (issueReason.equals(LMISApp.getInstance().getString(R.string.total))) {
            return newArrayList();
        }

        List<ProgramDataFormItem> programDataFormItems = new ArrayList<>();
        for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
            programDataFormItems.addAll(gridViewModel.convertFormGridViewModelToDataModel(issueReason));
        }
        return programDataFormItems;
    }

    public boolean validate() {
        for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
            if (!gridViewModel.validate()) {
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

    public void clearValue(RapidTestFormGridViewModel.ColumnCode columnCode, boolean isConsume) {
        rapidTestFormGridViewModelMap.get(StringUtils.upperCase(columnCode.name())).clear(isConsume);
    }
}
