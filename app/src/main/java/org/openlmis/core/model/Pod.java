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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.utils.ListUtil;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "pods")
public class Pod extends BaseModel {

  @DatabaseField
  private String shippedDate;

  @DatabaseField
  private String deliveredBy;

  @DatabaseField
  private String receivedBy;

  @DatabaseField
  private String documentNo;

  @DatabaseField
  private String receivedDate;

  @DatabaseField
  private String orderCode;

  @DatabaseField
  private String orderSupplyFacilityName;

  @DatabaseField
  private OrderStatus orderStatus;

  @DatabaseField
  private String orderCreatedDate;

  @DatabaseField
  private String  orderLastModifiedDate;

  @DatabaseField
  private String requisitionNumber;

  @DatabaseField
  private boolean requisitionIsEmergency;

  @DatabaseField
  private String requisitionProgramCode;

  @DatabaseField
  private String requisitionStartDate;

  @DatabaseField
  private String requisitionEndDate;

  @DatabaseField
  private String requisitionActualStartDate;

  @DatabaseField
  private String requisitionActualEndDate;

  @ForeignCollectionField
  private ForeignCollection<PodProduct> podProductForeignCollection;

  private List<PodProduct> podProductsWrapper;

  public List<PodProduct> getPodProductsWrapper() {
    podProductsWrapper = ListUtil.wrapOrEmpty(podProductForeignCollection, podProductsWrapper);
    return podProductsWrapper;
  }

}
