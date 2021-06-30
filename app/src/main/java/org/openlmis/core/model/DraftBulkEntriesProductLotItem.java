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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "draft_bulk_entries_product_lot_item")
public class DraftBulkEntriesProductLotItem extends BaseModel {

  @DatabaseField
  Long quantity;

  @DatabaseField
  Long lotSoh;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField
  String lotNumber;

  @DatabaseField
  String documentNumber;

  @DatabaseField
  String reason;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  Date expirationDate;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private DraftBulkEntriesProduct draftBulkEntriesProduct;

  @DatabaseField
  boolean newAdded;

  public DraftBulkEntriesProductLotItem(LotMovementViewModel lotMovementViewModel, Product product,
      boolean isNewAdded) {
    try {
      quantity = Long.parseLong(lotMovementViewModel.getQuantity());
    } catch (Exception e) {
      quantity = null;
    }
    setLotSoh(lotMovementViewModel.getLotSoh() == null ? null : Long.valueOf(lotMovementViewModel.getLotSoh()));
    setExpirationDate(DateUtil.getActualMaximumDate(DateUtil
        .parseString(lotMovementViewModel.getExpiryDate(),
            DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
    setLotNumber(lotMovementViewModel.getLotNumber());
    setProduct(product);
    setReason(lotMovementViewModel.getMovementReason());
    setDocumentNumber(lotMovementViewModel.getDocumentNumber());
    newAdded = isNewAdded;
  }
}
