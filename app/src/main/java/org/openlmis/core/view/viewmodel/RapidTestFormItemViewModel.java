package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.ProgramDataFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RapidTestFormItemViewModel {
    String issueReason;

    RapidTestFormGridViewModel gridHIVDetermine = RapidTestFormGridViewModel.HIVDetermine;
    RapidTestFormGridViewModel gridHIVUnigold = RapidTestFormGridViewModel.HIVUnigold;
    RapidTestFormGridViewModel gridSyphillis = RapidTestFormGridViewModel.Syphillis;
    RapidTestFormGridViewModel gridMalaria  = RapidTestFormGridViewModel.Malaria;

    List<ProgramDataFormItem> programDataFormItemList;

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

    public static final String CONSUME_HIVDETERMINE = "consumeHIVDetermine";
    public static final String POSITIVE_HIVDETERMINE = "positiveHIVDetermine";
    public static final String CONSUME_HIVUNIGOLD = "consumeHIVUnigold";
    public static final String POSITIVE_HIVUNIGOLD = "positiveHIVUnigold";
    public static final String CONSUME_SYPHILLIS = "consumeSyphillis";
    public static final String POSITIVE_SYPHILLIS = "positiveSyphillis";
    public static final String CONSUME_MALARIA = "consumeMalaria";
    public static final String POSITIVE_MALARIA = "positiveMalaria";
}
