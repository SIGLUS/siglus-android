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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openlmis.core.utils.HashUtil;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@DatabaseTable(tableName = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseModel {

  @DatabaseField
  String username;

  @DatabaseField
  String userFirstName;

  @DatabaseField
  String userLastName;

  String password;

  @DatabaseField(columnName = "password")
  String passwordMD5;

  @DatabaseField
  String facilityCode;

  @DatabaseField
  String facilityName;

  @DatabaseField
  String facilityId;

  String accessToken;

  String tokenType;

  String referenceDataUserId;

  Boolean isTokenExpired = false;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
    calculatePasswordMD5();
  }

  public void calculatePasswordMD5() {
    this.passwordMD5 = HashUtil.md5(password);
  }

  public String getPasswordMD5() {
    if (passwordMD5 == null && password != null) {
      calculatePasswordMD5();
    }
    return passwordMD5;
  }

  public void setPassword(String password) {
    this.password = password;
    calculatePasswordMD5();
  }
}
