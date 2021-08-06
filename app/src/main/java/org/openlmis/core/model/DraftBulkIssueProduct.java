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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openlmis.core.utils.ListUtil;

@Setter
@Getter
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "draft_bulk_issue_product")
public class DraftBulkIssueProduct extends BaseModel {

  @Include
  @DatabaseField
  private boolean done;

  @Include
  @DatabaseField
  private Long requested;

  @Include
  @DatabaseField(canBeNull = false)
  private String movementReasonCode;

  @Include
  @DatabaseField
  private String documentNumber;

  @Include
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private Product product;

  @ForeignCollectionField(eager = true)
  private ForeignCollection<DraftBulkIssueProductLotItem> foreignDraftLotItems;

  private List<DraftBulkIssueProductLotItem> draftLotItemListWrapper;

  public List<DraftBulkIssueProductLotItem> getDraftLotItemListWrapper() {
    return ListUtil.wrapOrEmpty(foreignDraftLotItems, draftLotItemListWrapper);
  }
}
