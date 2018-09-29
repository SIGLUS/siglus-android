package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RapidTestFormGridViewModel {

    public enum RapidTestGridColumnCode {
        consumption,
        positive,
        unjustified
    }

    public enum ColumnCode {
        HIVDetermine,
        HIVUnigold,
        Syphillis,
        Malaria;

        @Override
        public String toString() {
            return StringUtils.upperCase(name());
        }
    }

    ColumnCode columnCode;
    String consumptionValue = "";
    String positiveValue = "";
    String unjustifiedValue = "";


    ProgramDataColumn positiveColumn;
    ProgramDataColumn consumeColumn;
    ProgramDataColumn unjustifiedColumn;

    final static String COLUMN_CODE_PREFIX_CONSUME = "CONSUME_";
    final static String COLUMN_CODE_PREFIX_POSITIVE = "POSITIVE_";
    final static String COLUMN_CODE_PREFIX_UNJUSTIFIED = "UNJUSTIFIED_";

    RapidTestFormGridViewModel(ColumnCode columnCode) {
        this.columnCode = columnCode;
    }

    public boolean validate() {
        try {
            return isEmpty()
                    || (Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue) && Long.parseLong(unjustifiedValue) >= 0) ;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setValue(ProgramDataColumn column, int value) {
        setConsumptionValue(column, value);
        setPositiveValue(column, value);
        setUnjustifiedValue(column, value);
    }

    public List<ProgramDataFormItem> convertFormGridViewModelToDataModel(MovementReasonManager.MovementReason issueReason) {
        List<ProgramDataFormItem> programDataFormItems = new ArrayList<>();
        setConsumptionFormItem(issueReason, programDataFormItems);
        setPositiveFormItem(issueReason, programDataFormItems);
        setUnjustifiedFormItem(issueReason, programDataFormItems);
        return programDataFormItems;
    }

    public void clear(RapidTestGridColumnCode column) {
        switch (column) {
            case positive:
                positiveValue =  "";
                break;
            case consumption:
                consumptionValue =  "";
                break;
            case unjustified:
                unjustifiedValue =  "";
                break;
        }
    }

    public void setValue(RapidTestGridColumnCode column, String value) {
        switch (column) {
            case positive:
                positiveValue = value;
                break;
            case consumption:
                consumptionValue = value;
                break;
            case unjustified:
                unjustifiedValue = value;
                break;
        }
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(consumptionValue) && StringUtils.isEmpty(positiveValue) && StringUtils.isEmpty(unjustifiedValue);
    }

    private String generateFullColumnName(String prefix) {
        return prefix + StringUtils.upperCase(getColumnCode().name());
    }

    private void setUnjustifiedValue(ProgramDataColumn column, int value) {
        if (column.getCode().contains(COLUMN_CODE_PREFIX_UNJUSTIFIED)) {
            unjustifiedColumn = column;
            setUnjustifiedValue(String.valueOf(value));
        }
    }

    private void setPositiveValue(ProgramDataColumn column, int value) {
        if (column.getCode().contains(COLUMN_CODE_PREFIX_POSITIVE)) {
            positiveColumn = column;
            setPositiveValue(String.valueOf(value));
        }
    }

    private void setConsumptionValue(ProgramDataColumn column, int value) {
        if (column.getCode().contains(COLUMN_CODE_PREFIX_CONSUME)) {
            consumeColumn = column;
            setConsumptionValue(String.valueOf(value));
        }
    }

    private void setUnjustifiedFormItem(MovementReasonManager.MovementReason issueReason, List<ProgramDataFormItem> programDataFormItems) {
        if (!StringUtils.isEmpty(getUnjustifiedValue())) {
            if (unjustifiedColumn == null) {
                unjustifiedColumn = new ProgramDataColumn();
                unjustifiedColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_UNJUSTIFIED));
            }
            ProgramDataFormItem unjustfiedDataFormItem = new ProgramDataFormItem(issueReason.getCode(), unjustifiedColumn, Integer.parseInt(getUnjustifiedValue()));
            programDataFormItems.add(unjustfiedDataFormItem);
        }
    }

    private void setPositiveFormItem(MovementReasonManager.MovementReason issueReason, List<ProgramDataFormItem> programDataFormItems) {
        if (!StringUtils.isEmpty(getPositiveValue())) {
            if (positiveColumn == null) {
                positiveColumn = new ProgramDataColumn();
                positiveColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE));
            }
            ProgramDataFormItem positiveDataFormItem = new ProgramDataFormItem(issueReason.getCode(), positiveColumn, Integer.parseInt(getPositiveValue()));
            programDataFormItems.add(positiveDataFormItem);
        }
    }

    private void setConsumptionFormItem(MovementReasonManager.MovementReason issueReason, List<ProgramDataFormItem> programDataFormItems) {
        if (!StringUtils.isEmpty(getConsumptionValue())) {
            if (consumeColumn == null) {
                consumeColumn = new ProgramDataColumn();
                consumeColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_CONSUME));
            }
            ProgramDataFormItem consumeDataFormItem = new ProgramDataFormItem(issueReason.getCode(), consumeColumn, Integer.parseInt(getConsumptionValue()));
            programDataFormItems.add(consumeDataFormItem);
        }
    }

}
