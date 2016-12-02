package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class RapidTestFormGridViewModel {
    ColumnCode columnCode;
    String consumptionValue = "";
    String positiveValue = "";

    RapidTestFormGridViewModel(ColumnCode columnCode) {
        this.columnCode = columnCode;
    }

    public boolean validate() {
        try {
            return StringUtils.isEmpty(consumptionValue) && StringUtils.isEmpty(positiveValue)
                    || Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public enum ColumnCode {
        HIVDetermine,
        HIVUnigold,
        Syphillis,
        Malaria
    }

}
