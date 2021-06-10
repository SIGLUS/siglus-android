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

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DatabaseTable(tableName = "lots_on_hand")
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

  public LotOnHand(Lot lot, StockCard stockCard, Long quantityOnHand) {
    this.lot = lot;
    this.stockCard = stockCard;
    this.quantityOnHand = quantityOnHand;
  }
}
