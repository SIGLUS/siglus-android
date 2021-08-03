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
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.utils.ListUtil;

@Getter
@Setter
@DatabaseTable(tableName = "rnr_form_items")
public class RnrFormItem extends BaseModel {

  @Expose
  @SerializedName("productCode")
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private Product product;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private RnRForm form;

  @Expose
  @SerializedName("beginningBalance")
  @DatabaseField
  private Long initialAmount;

  @Expose
  @SerializedName("totalReceivedQuantity")
  @DatabaseField
  private long received;

  @Expose
  @SerializedName("totalConsumedQuantity")
  @DatabaseField
  private Long issued;

  @Expose
  @SerializedName("totalLossesAndAdjustments")
  @DatabaseField
  private Long adjustment;

  @Expose
  @SerializedName("stockOnHand")
  @DatabaseField
  private Long inventory;

  @Expose
  @SerializedName("expirationDate")
  @DatabaseField
  private String validate;

  @Expose
  @SerializedName("requestedQuantity")
  @DatabaseField
  private Long requestAmount;

  @Expose
  @SerializedName("authorizedQuantity")
  @DatabaseField
  private Long approvedAmount;

  @Expose
  @DatabaseField
  private Long calculatedOrderQuantity;

  @Expose
  @SerializedName("totalServiceQuantity")
  @DatabaseField
  private Long totalServiceQuantity;

  @DatabaseField(defaultValue = "false")
  private Boolean isCustomAmount;

  @Expose
  @DatabaseField
  private boolean isManualAdd = false;

  private String category;

  @ForeignCollectionField()
  private ForeignCollection<ServiceItem> serviceItemList;

  private List<ServiceItem> serviceItemListWrapper;

  public List<ServiceItem> getServiceItemListWrapper() {
    serviceItemListWrapper = ListUtil.wrapOrEmpty(serviceItemList, serviceItemListWrapper);
    return serviceItemListWrapper;
  }

}
