package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;

public enum RapidTestFormGridViewModel {
    HIVDetermine("10","10"),
    HIVUnigold("20","30"),
    Syphillis("10","1"),
    Malaria("3","4");

    String consumptionValue;
    String positiveValue;

    RapidTestFormGridViewModel(String consumptionValue, String positiveValue) {
        this.consumptionValue = consumptionValue;
        this.positiveValue = positiveValue;
    }

    public String getConsumptionValue() {
        return consumptionValue;
    }

    public String getPositiveValue() {
        return positiveValue;
    }

    public void setConsumptionValue(String consumptionValue) {
        this.consumptionValue = consumptionValue;
    }

    public void setPositiveValue(String positiveValue) {
        this.positiveValue = positiveValue;
    }

    public boolean validate() {
        return StringUtils.isEmpty(consumptionValue) && StringUtils.isEmpty(positiveValue)
                || Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue);
    }
}
