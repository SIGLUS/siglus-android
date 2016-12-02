package org.openlmis.core.view.viewmodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RapidTestFormItemViewModel {
    String issueReason;

    RapidTestFormGridViewModel gridHIVDetermine = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.HIVDetermine);
    RapidTestFormGridViewModel gridHIVUnigold = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.HIVUnigold);
    RapidTestFormGridViewModel gridSyphillis = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.Syphillis);
    RapidTestFormGridViewModel gridMalaria  = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.Malaria);

    List<RapidTestFormGridViewModel> rapidTestFormGridViewModelList = new ArrayList<>();

    public RapidTestFormItemViewModel(String issueReason) {
        this.issueReason = issueReason;
        rapidTestFormGridViewModelList.add(gridHIVDetermine);
        rapidTestFormGridViewModelList.add(gridHIVUnigold);
        rapidTestFormGridViewModelList.add(gridSyphillis);
        rapidTestFormGridViewModelList.add(gridMalaria);
    }

    public void setColumnValue(String columnCode, int value) {
        switch (columnCode) {
            case CONSUME_HIVDETERMINE:
                gridHIVDetermine.setConsumptionValue(String.valueOf(value));
                break;
            case POSITIVE_HIVDETERMINE:
                gridHIVDetermine.setPositiveValue(String.valueOf(value));
                break;
            case CONSUME_HIVUNIGOLD:
                gridHIVUnigold.setConsumptionValue(String.valueOf(value));
                break;
            case POSITIVE_HIVUNIGOLD:
                gridHIVUnigold.setPositiveValue(String.valueOf(value));
                break;
            case CONSUME_SYPHILLIS:
                gridSyphillis.setConsumptionValue(String.valueOf(value));
                break;
            case POSITIVE_SYPHILLIS:
                gridSyphillis.setPositiveValue(String.valueOf(value));
                break;
            case CONSUME_MALARIA:
                gridMalaria.setConsumptionValue(String.valueOf(value));
                break;
            case POSITIVE_MALARIA:
                gridMalaria.setPositiveValue(String.valueOf(value));
                break;
        }
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
