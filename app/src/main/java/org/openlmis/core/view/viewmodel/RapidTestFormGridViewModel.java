package org.openlmis.core.view.viewmodel;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.ProgramDataFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RapidTestFormGridViewModel {
    ColumnCode columnCode;
    String consumptionValue = "";
    String positiveValue = "";

    final static String COLUMN_CODE_PREFIX_CONSUME = "CONSUME_";
    final static String COLUMN_CODE_PREFIX_POSITIVE = "POSITIVE_";

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

    public void setValue(String quantityCategory, int value) {
        if (quantityCategory.equals("CONSUME")) {
            setConsumptionValue(String.valueOf(value));
        } else {
            setPositiveValue(String.valueOf(value));
        }
    }

    public List<ProgramDataFormItem> convertFormGridViewModelToDataModel(String issueReason) {
        List<ProgramDataFormItem> programDataFormItems = new ArrayList<>();
        if (!StringUtils.isEmpty(getConsumptionValue())) {
            ProgramDataFormItem consumeDataFormItem = new ProgramDataFormItem(issueReason, generateFullColumnName(COLUMN_CODE_PREFIX_CONSUME), Integer.parseInt(getConsumptionValue()));
            programDataFormItems.add(consumeDataFormItem);
        }
        if (!StringUtils.isEmpty(getPositiveValue())) {
            ProgramDataFormItem positiveDataFormItem = new ProgramDataFormItem(issueReason, generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE), Integer.parseInt(getPositiveValue()));
            programDataFormItems.add(positiveDataFormItem);
        }
        return programDataFormItems;
    }

    @NonNull
    public String generateFullColumnName(String prefix) {
        return  prefix + StringUtils.upperCase(getColumnCode().name());
    }

    public enum ColumnCode {
        HIVDetermine,
        HIVUnigold,
        Syphillis,
        Malaria
    }

}
