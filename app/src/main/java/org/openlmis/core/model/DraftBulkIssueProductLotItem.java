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
import lombok.experimental.Accessors;

@Setter
@Getter
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "draft_bulk_issue_product_lot_item")
public class DraftBulkIssueProductLotItem extends BaseModel {

  @DatabaseField
  private Long amount;

  @DatabaseField
  private long lotSoh;

  @DatabaseField
  private String lotNumber;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date expirationDate;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private DraftBulkIssueProduct draftBulkIssueProduct;
}
