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

import static org.openlmis.core.utils.DateUtil.DATE_TIME_FORMAT;
import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.utils.ListUtil;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "pods")
public class Pod extends BaseModel implements Serializable {

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date shippedDate;

  @DatabaseField
  private String deliveredBy;

  @DatabaseField
  private String receivedBy;

  @DatabaseField
  private String documentNo;

  @DatabaseField(dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date receivedDate;

  @DatabaseField
  private String orderCode;

  @DatabaseField
  private String originOrderCode;

  @DatabaseField
  private String orderSupplyFacilityName;

  @DatabaseField
  private String orderSupplyFacilityDistrict;

  @DatabaseField
  private String orderSupplyFacilityProvince;

  @DatabaseField
  private OrderStatus orderStatus;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DATE_TIME_FORMAT)
  private Date orderCreatedDate;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DATE_TIME_FORMAT)
  private Date orderLastModifiedDate;

  @DatabaseField
  private String requisitionNumber;

  @DatabaseField
  private boolean requisitionIsEmergency;

  @DatabaseField
  private String requisitionProgramCode;

  @DatabaseField
  private String stockManagementReason;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date requisitionStartDate;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date requisitionEndDate;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date requisitionActualStartDate;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private Date requisitionActualEndDate;

  @DatabaseField
  private Date processedDate;

  @DatabaseField
  private Date serverProcessedDate;

  @DatabaseField
  private boolean isLocal;

  @DatabaseField
  private boolean isDraft;

  @DatabaseField
  private boolean isSynced;

  @ForeignCollectionField(eager = true)
  private ForeignCollection<PodProductItem> podProductItemForeignCollection;

  @Setter
  private List<PodProductItem> podProductItemsWrapper;

  public List<PodProductItem> getPodProductItemsWrapper() {
    podProductItemsWrapper = ListUtil.wrapOrEmpty(podProductItemForeignCollection, podProductItemsWrapper);
    return podProductItemsWrapper;
  }

}
