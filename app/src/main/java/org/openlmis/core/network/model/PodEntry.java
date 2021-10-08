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

package org.openlmis.core.network.model;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.utils.DateUtil;

@Data
public class PodEntry {

  private String shippedDate;
  private String receivedDate;
  private String deliveredBy;
  private String receivedBy;
  private String originNumber;
  private boolean isLocal;
  private String orderNumber;
  private String programCode;
  private List<ProductEntry> products;

  public PodEntry(Pod pod) {
    this.shippedDate = getDbFormatData(pod.getShippedDate());
    this.receivedDate = getDbFormatData(pod.getReceivedDate());
    this.deliveredBy = pod.getDeliveredBy();
    this.receivedBy = pod.getReceivedBy();
    this.orderNumber = pod.getOrderCode();
    this.originNumber = pod.getOriginOrderCode();
    this.isLocal = pod.isLocal();
    this.programCode = pod.getRequisitionProgramCode();
    this.products = buildProductEntryList(pod.getPodProductItemsWrapper());
  }

  @Nullable
  protected static String getDbFormatData(@Nullable Date date) {
    return date == null ? null : DateUtil.formatDate(date, DateUtil.DB_DATE_FORMAT);
  }

  private List<ProductEntry> buildProductEntryList(List<PodProductItem> podProductItems) {
    ArrayList<ProductEntry> productEntries = new ArrayList<>();
    for (PodProductItem podProductItem : podProductItems) {
      productEntries.add(new ProductEntry(podProductItem));
    }
    return productEntries;
  }

  @Data
  public static class ProductEntry {

    private String code;
    private long orderedQuantity;
    private long partialFulfilledQuantity;
    private List<LotEntry> lots;

    public ProductEntry(PodProductItem productItem) {
      this.code = productItem.getProduct().getCode();
      this.orderedQuantity = productItem.getOrderedQuantity() == null ? 0 : productItem.getOrderedQuantity();
      this.partialFulfilledQuantity =
          productItem.getPartialFulfilledQuantity() == null ? 0 : productItem.getPartialFulfilledQuantity();
      this.lots = buildProductLotEntryList(productItem.getPodProductLotItemsWrapper());
    }

    private List<LotEntry> buildProductLotEntryList(List<PodProductLotItem> podProductLotItems) {
      ArrayList<LotEntry> lotEntries = new ArrayList<>();
      for (PodProductLotItem podProductLotItem : podProductLotItems) {
        lotEntries.add(new LotEntry(podProductLotItem));
      }
      return lotEntries;
    }
  }

  @Data
  public static class LotEntry {

    private LotItemEntry lot;
    private long shippedQuantity;
    private long acceptedQuantity;
    private String rejectedReason;
    private String notes;

    public LotEntry(PodProductLotItem podProductLotItem) {
      if (!podProductLotItem.getPodProductItem().getProduct().isKit()) {
        this.lot = new LotItemEntry(podProductLotItem);
      }
      this.shippedQuantity = podProductLotItem.getShippedQuantity();
      this.acceptedQuantity = podProductLotItem.getAcceptedQuantity();
      this.rejectedReason = podProductLotItem.getRejectedReason();
      this.notes = podProductLotItem.getNotes();
    }
  }

  @Data
  public static class LotItemEntry {

    private String code;
    private String expirationDate;

    public LotItemEntry(PodProductLotItem podProductLotItem) {
      this.code = podProductLotItem.getLot().getLotNumber();
      this.expirationDate = getDbFormatData(podProductLotItem.getLot().getExpirationDate());
    }
  }
}
