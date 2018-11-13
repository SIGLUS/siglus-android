package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ALGridViewModel {

    public enum ALGridColumnCode {
        treatments,
        existentStock
    }

    public enum ALColumnCode {
        OneColumn("6*1"),
        TwoColumn("6*2"),
        ThreeColumn("6*3"),
        FourColumn("6*4");
        private String columnCodeName;

        ALColumnCode(String code) {
            this.columnCodeName = code;
        }

        public String getColumnName() {
            return columnCodeName;
        }
    }

    ALColumnCode columnCode;
    String treatmentsValue = "";
    String existentStockValue = "";


    final static String COLUMN_CODE_PREFIX_TREATMENTS = "Consultas AL US/APE Malaria";
    final static String COLUMN_CODE_PREFIX_STOCK = "Consultas AL US/APE Malaria2";


    ALGridViewModel(ALColumnCode columnCode) {
        this.columnCode = columnCode;
    }

    public boolean validate() {
        try {
            return isEmpty();
        } catch (NumberFormatException e) {
            return false;
        }
    }

//    public void setValue(ProgramDataColumn column, int value) {
//        setConsumptionValue(column, value);
//        setPositiveValue(column, value);
//        setUnjustifiedValue(column, value);
//    }

//    public List<ProgramDataFormItem> convertFormGridViewModelToDataModel(MovementReasonManager.MovementReason issueReason) {
//        List<ProgramDataFormItem> programDataFormItems = new ArrayList<>();
//        setConsumptionFormItem(issueReason, programDataFormItems);
//        setPositiveFormItem(issueReason, programDataFormItems);
//        setUnjustifiedFormItem(issueReason, programDataFormItems);
//        return programDataFormItems;
//    }

//
//    public void setValue(RapidTestGridColumnCode column, String value) {
//        switch (column) {
//            case positive:
//                positiveValue = value;
//                break;
//            case consumption:
//                consumptionValue = value;
//                break;
//        }
//    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(treatmentsValue)
                && StringUtils.isEmpty(existentStockValue);
    }


//    private String generateFullColumnName(String prefix) {
//        return prefix + StringUtils.upperCase(getColumnCode().name());
//    }

//    private void setUnjustifiedValue(ProgramDataColumn column, int value) {
//        if (column.getCode().contains(COLUMN_CODE_PREFIX_UNJUSTIFIED)) {
//            unjustifiedColumn = column;
//            setUnjustifiedValue(String.valueOf(value));
//        }
//    }
//
//    private void setPositiveValue(ProgramDataColumn column, int value) {
//        if (column.getCode().contains(COLUMN_CODE_PREFIX_POSITIVE)) {
//            positiveColumn = column;
//            setPositiveValue(String.valueOf(value));
//        }
//    }



}
