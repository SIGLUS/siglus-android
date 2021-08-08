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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "draft_bulk_issue_lots")
public class DraftBulkIssueLot extends BaseModel {

  @DatabaseField
  private Long amount;

  /**
   * foreign object, only fetch 2 level foreign object
   * see {@link com.j256.ormlite.field.DatabaseField#DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL}
   * don`t use more than 2 level
   */
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private LotOnHand lotOnHand;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private DraftBulkIssueProduct draftBulkIssueProduct;
}
