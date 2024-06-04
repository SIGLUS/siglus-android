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

package org.openlmis.core.model;

import lombok.Data;

@Data
public class LotExcelModel {

  String productCode;

  String productName;

  String lotNumber;

  String expirationDate;

  String orderedQuantity;

  String partialFulfilled;

  String suppliedQuantity;

  String price;

  String totalValue;

  public LotExcelModel(String productCode, String productName,
      String lotNumber, String expirationDate, String orderedQuantity, String partialFulfilled,
      String suppliedQuantity, String price, String totalValue) {
    this.productCode = productCode;
    this.productName = productName;
    this.lotNumber = lotNumber;
    this.expirationDate = expirationDate;
    this.orderedQuantity = orderedQuantity;
    this.partialFulfilled = partialFulfilled;
    this.suppliedQuantity = suppliedQuantity;
    this.price = price;
    this.totalValue = totalValue;
  }
}
