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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@DatabaseTable(tableName = "test_consumption_items")
public class TestConsumptionItem extends BaseModel {
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private RnRForm form;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private UsageColumnsMap usageColumnsMap;

  @Expose
  @DatabaseField
  private String service;

  @Expose
  @DatabaseField
  private int value;

  public TestConsumptionItem(String service, UsageColumnsMap usageColumnsMap, int value) {
    this.service = service;
    this.usageColumnsMap = usageColumnsMap;
    this.value = value;
  }
}
