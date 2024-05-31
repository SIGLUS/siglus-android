package org.openlmis.core.model;


import lombok.Data;

@Data
public class LotExcelModel {

  String productCode;

  String productName;

  String lotNumber;

  String expirationDate;

  String ordered;

  String partial;

  String suppliedQuantity;

  String price;

  String totalValue;

  public LotExcelModel(String productCode, String productName,
      String lotNumber, String expirationDate, String ordered, String partial,
      String suppliedQuantity, String price, String totalValue) {
    this.productCode = productCode;
    this.productName = productName;
    this.lotNumber = lotNumber;
    this.expirationDate = expirationDate;
    this.ordered = ordered;
    this.partial = partial;
    this.suppliedQuantity = suppliedQuantity;
    this.price = price;
    this.totalValue = totalValue;
  }
}
