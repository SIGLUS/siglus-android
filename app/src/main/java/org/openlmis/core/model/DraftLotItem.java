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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

@Getter
@Setter
@DatabaseTable(tableName = "draft_lot_items")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DraftLotItem extends BaseModel {

  @DatabaseField
  Long quantity;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField
  String lotNumber;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  Date expirationDate;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private DraftInventory draftInventory;

  @DatabaseField
  boolean newAdded;

  public DraftLotItem(LotMovementViewModel lotMovementViewModel, Product product,
      boolean isNewAdded) {
    try {
      quantity = Long.parseLong(lotMovementViewModel.getQuantity());
    } catch (Exception e) {
      quantity = null;
    }
    setExpirationDate(DateUtil.parseString(lotMovementViewModel.getExpiryDate(), DB_DATE_FORMAT));
    setLotNumber(lotMovementViewModel.getLotNumber());
    setProduct(product);
    newAdded = isNewAdded;
  }
}
