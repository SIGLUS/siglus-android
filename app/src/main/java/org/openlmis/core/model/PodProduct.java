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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "pod_products")
public class PodProduct extends BaseModel {

  @DatabaseField
  private String code;

  @DatabaseField
  private long orderedQuantity;

  @DatabaseField
  private long partialFulfilledQuantity;

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
  private Pod pod;

  @ForeignCollectionField
  private ForeignCollection<PodLotItem> podLotItemForeignCollection;

  private List<PodLotItem> podLotItemsWrapper;
}
