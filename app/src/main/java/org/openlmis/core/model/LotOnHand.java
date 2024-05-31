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

import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openlmis.core.utils.DateUtil;

@Data
@DatabaseTable(tableName = "lots_on_hand")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class LotOnHand extends BaseModel {

  @Expose
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Lot lot;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  StockCard stockCard;

  @Expose
  @DatabaseField
  Long quantityOnHand;

  boolean checked = false;

  public LotOnHand(Lot lot, StockCard stockCard, Long quantityOnHand) {
    this.lot = lot;
    this.stockCard = stockCard;
    this.quantityOnHand = quantityOnHand;
  }

  public LotExcelModel convertToExcelModel(
      String orderedQuantity,
      String partialFulfilled,
      String suppliedQuantity
  ) {
    Product product = this.stockCard.product;
    String price = product.price != null ? product.price: "0" ;

    BigDecimal perPrice = new BigDecimal(price);
    BigDecimal totalValue = perPrice.multiply(new BigDecimal(suppliedQuantity));

    return new LotExcelModel(
        product.code,
        product.primaryName,
        lot.lotNumber,
        DateUtil.formatDate(lot.expirationDate, DB_DATE_FORMAT),
        orderedQuantity,
        partialFulfilled,
        suppliedQuantity,
        price,
        String.valueOf(totalValue)
    );
  }
}
