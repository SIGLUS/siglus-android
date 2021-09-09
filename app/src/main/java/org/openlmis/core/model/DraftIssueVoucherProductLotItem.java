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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DatabaseTable(tableName = "draft_issue_voucher_product_lot_items")
public class DraftIssueVoucherProductLotItem  extends BaseModel {

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  DraftIssueVoucherProductItem draftIssueVoucherProductItem;

  @DatabaseField
  Long shippedQuantity;

  @DatabaseField
  Long acceptedQuantity;

  @DatabaseField
  String lotNumber;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DB_DATE_FORMAT)
  Date expirationDate;

  @DatabaseField
  boolean newAdded;

  @DatabaseField
  boolean done;

  public IssueVoucherLotViewModel from() {
    return IssueVoucherLotViewModel.builder()
        .product(draftIssueVoucherProductItem.getProduct())
        .done(done)
        .isNewAdd(isNewAdded())
        .shippedQuantity(shippedQuantity)
        .acceptedQuantity(acceptedQuantity)
        .lotNumber(lotNumber)
        .expiryDate(DateUtil.formatDate(expirationDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .productLotItem(this)
        .lot(Lot.builder()
            .lotNumber(lotNumber)
            .expirationDate(expirationDate)
            .product(draftIssueVoucherProductItem.getProduct())
            .build())
        .build();
  }



}
