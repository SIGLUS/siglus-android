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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "dirty_data")
public class DirtyDataItemInfo extends BaseModel {

  @DatabaseField
  String jsonData;

  @DatabaseField
  boolean synced = false;

  @DatabaseField
  String productCode;

  @DatabaseField
  boolean fullyDelete = true;

  public DirtyDataItemInfo(String productCode, boolean sync_status, String json_data,
      boolean fullyDelete) {
    this.productCode = productCode;
    this.synced = sync_status;
    this.jsonData = json_data;
    this.fullyDelete = fullyDelete;
  }
}
