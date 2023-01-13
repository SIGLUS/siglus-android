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

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@DatabaseTable(tableName = "programs")
public class Program extends BaseModel {

  public static final String MALARIA_CODE = "ML";
  public static final String RAPID_TEST_CODE = "TR";
  public static final String TARV_CODE = "T";
  public static final String VIA_CODE = "VC";
  public static final String MMTB_CODE = "TB";

  @EqualsAndHashCode.Include
  @SerializedName("code")
  @DatabaseField
  String programCode;

  @EqualsAndHashCode.Include
  @SerializedName("name")
  @DatabaseField
  String programName;

  @SerializedName("parentCode")
  @DatabaseField
  String parentCode;

  @DatabaseField
  boolean isSupportEmergency;

  @ForeignCollectionField(columnName = "products")
  private Collection<Product> products;

  @ForeignCollectionField(columnName = "regimens")
  private Collection<Regimen> regimens;
}
