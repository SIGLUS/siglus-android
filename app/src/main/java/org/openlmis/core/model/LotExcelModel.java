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
