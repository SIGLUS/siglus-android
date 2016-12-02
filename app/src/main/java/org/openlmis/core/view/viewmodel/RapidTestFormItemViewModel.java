package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

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

    public void setColumnValue(String columnCode, int value) {
        String[] columnNames = columnCode.split("_");
        String quantityCategory = columnNames[0];
        String columnName = columnNames[1];
        rapidTestFormGridViewModelMap.get(columnName).setValue(quantityCategory, value);
    }

    public static final String CONSUME_HIVDETERMINE = "CONSUME_HIVDETERMINE";
    public static final String POSITIVE_HIVDETERMINE = "POSITIVE_HIVDETERMINE";
    public static final String CONSUME_HIVUNIGOLD = "CONSUME_HIVUNIGOLD";
    public static final String POSITIVE_HIVUNIGOLD = "POSITIVE_HIVUNIGOLD";
    public static final String CONSUME_SYPHILLIS = "CONSUME_SYPHILLIS";
    public static final String POSITIVE_SYPHILLIS = "POSITIVE_SYPHILLIS";
    public static final String CONSUME_MALARIA = "CONSUME_MALARIA";
    public static final String POSITIVE_MALARIA = "POSITIVE_MALARIA";
}
