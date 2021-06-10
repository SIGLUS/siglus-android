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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "cmm")
public class Cmm extends BaseModel {

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  StockCard stockCard;

  @DatabaseField
  float cmmValue;

  @DatabaseField
  Date periodBegin;

  @DatabaseField
  Date periodEnd;

  @DatabaseField
  private boolean synced = false;

  public static Cmm initWith(StockCard stockCard, Period period) {
    Cmm cmm = new Cmm();
    cmm.setStockCard(stockCard);
    cmm.setCmmValue(stockCard.getAvgMonthlyConsumption());
    cmm.setPeriodBegin(period.getBegin().toDate());
    cmm.setPeriodEnd(period.getEnd().toDate());
    return cmm;
  }
}
