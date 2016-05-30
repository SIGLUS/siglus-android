package org.openlmis.core.network.model;

import org.openlmis.core.model.Cmm;

import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;
import static org.openlmis.core.utils.DateUtil.formatDate;

public class CmmEntry {
    String productCode;

    float cmmValue;

    String periodBegin;

    String periodEnd;

    public CmmEntry(String productCode, float cmmValue, String periodBegin, String periodEnd) {
        this.productCode = productCode;
        this.cmmValue = cmmValue;
        this.periodBegin = periodBegin;
        this.periodEnd = periodEnd;
    }

    public static CmmEntry createFrom(Cmm cmm) {
        return new CmmEntry(cmm.getStockCard().getProduct().getCode(),
                cmm.getCmmValue(),
                formatDate(cmm.getPeriodBegin(), DB_DATE_FORMAT),
                formatDate(cmm.getPeriodEnd(), DB_DATE_FORMAT));
    }
}
