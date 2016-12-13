package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RapidTestFormGridViewModel {
    ColumnCode columnCode;
    String consumptionValue = "";
    String positiveValue = "";

    ProgramDataColumn positiveColumn;
    ProgramDataColumn consumeColumn;

    final static String COLUMN_CODE_PREFIX_CONSUME = "CONSUME_";
    final static String COLUMN_CODE_PREFIX_POSITIVE = "POSITIVE_";

    RapidTestFormGridViewModel(ColumnCode columnCode) {
        this.columnCode = columnCode;
    }

    public boolean validate() {
        try {
            return isEmpty()
                    || Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setValue(ProgramDataColumn column, int value) {
        if (column.getCode().contains("CONSUME")) {
            consumeColumn = column;
            setConsumptionValue(String.valueOf(value));
        } else {
            positiveColumn = column;
            setPositiveValue(String.valueOf(value));
        }
    }

    public List<ProgramDataFormItem> convertFormGridViewModelToDataModel(String issueReason) {
        List<ProgramDataFormItem> programDataFormItems = new ArrayList<>();
        if (!StringUtils.isEmpty(getConsumptionValue())) {
            if (consumeColumn == null) {
                consumeColumn = new ProgramDataColumn();
                consumeColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_CONSUME));
            }
            ProgramDataFormItem consumeDataFormItem = new ProgramDataFormItem(issueReason, consumeColumn, Integer.parseInt(getConsumptionValue()));
            programDataFormItems.add(consumeDataFormItem);
        }
        if (!StringUtils.isEmpty(getPositiveValue())) {
            if (positiveColumn == null) {
                positiveColumn = new ProgramDataColumn();
                positiveColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE));
            }
            ProgramDataFormItem positiveDataFormItem = new ProgramDataFormItem(issueReason, positiveColumn, Integer.parseInt(getPositiveValue()));
            programDataFormItems.add(positiveDataFormItem);
        }
        return programDataFormItems;
    }

    public String generateFullColumnName(String prefix) {
        return prefix + StringUtils.upperCase(getColumnCode().name());
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(consumptionValue) && StringUtils.isEmpty(positiveValue);
    }

    public enum ColumnCode {
        HIVDetermine,
        HIVUnigold,
        Syphillis,
        Malaria
    }

}
