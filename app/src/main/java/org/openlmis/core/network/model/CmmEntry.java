/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.network.model;

import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;
import static org.openlmis.core.utils.DateUtil.formatDate;

import org.openlmis.core.model.Cmm;

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
