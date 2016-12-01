package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.ProgramDataFormItem;

import java.util.List;

import lombok.Data;

@Data
public class RapidTestFormItemViewModel {
    String issueReason;
    String consumeHIVDetermine;
    String positiveHIVDetermine;
    String consumeHIVUnigold;
    String positiveHIVUnigold;
    String consumeSyphillis;
    String positiveSyphillis;
    String consumeMalaria;
    String positiveMalaria;

    List<ProgramDataFormItem> programDataFormItemList;

    public RapidTestFormItemViewModel(String issueReason) {
        this.issueReason = issueReason;
    }

    public void setColumnValue(String columnCode, int value) {
        switch (columnCode) {
            case CONSUME_HIVDETERMINE:
                consumeHIVDetermine = String.valueOf(value);
                break;
            case POSITIVE_HIVDETERMINE:
                positiveHIVDetermine = String.valueOf(value);
                break;
            case CONSUME_HIVUNIGOLD:
                consumeHIVUnigold = String.valueOf(value);
                break;
            case POSITIVE_HIVUNIGOLD:
                positiveHIVUnigold = String.valueOf(value);
                break;
            case CONSUME_SYPHILLIS:
                consumeSyphillis = String.valueOf(value);
                break;
            case POSITIVE_SYPHILLIS:
                positiveSyphillis = String.valueOf(value);
                break;
            case CONSUME_MALARIA:
                consumeMalaria = String.valueOf(value);
                break;
            case POSITIVE_MALARIA:
                positiveMalaria = String.valueOf(value);
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
