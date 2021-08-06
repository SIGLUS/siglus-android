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

package org.openlmis.core.view.viewmodel;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.DraftBulkIssueProductLotItem;

@Data
@Builder
public class BulkIssueLotViewModel implements Comparable<BulkIssueLotViewModel> {

  private Long amount;

  private long lotSoh;

  private String lotNumber;

  private Date expirationDate;

  private DraftBulkIssueProductLotItem draftLotItem;

  public static BulkIssueLotViewModel buildFromDraft(DraftBulkIssueProductLotItem draftLotItem) {
    return new BulkIssueLotViewModelBuilder()
        .amount(draftLotItem.getAmount())
        .lotSoh(draftLotItem.getLotSoh())
        .lotNumber(draftLotItem.getLotNumber())
        .expirationDate(draftLotItem.getExpirationDate())
        .draftLotItem(draftLotItem)
        .build();
  }

  public static BulkIssueLotViewModel buildFromProduct(long lotSoh, String lotNumber, Date expirationDate) {
    return new BulkIssueLotViewModelBuilder()
        .lotSoh(lotSoh)
        .lotNumber(lotNumber)
        .expirationDate(expirationDate)
        .build();
  }

  public DraftBulkIssueProductLotItem convertToDraft(DraftBulkIssueProduct draftProduct) {
    if (draftLotItem == null) {
      draftLotItem = new DraftBulkIssueProductLotItem();
    }
    return draftLotItem
        .setAmount(amount)
        .setLotSoh(lotSoh)
        .setExpirationDate(expirationDate)
        .setLotNumber(lotNumber)
        .setDraftBulkIssueProduct(draftProduct);
  }

  @Override
  public int compareTo(BulkIssueLotViewModel other) {
    long otherExpirationDate = other.getExpirationDate().getTime();
    long myExpirationDate = getExpirationDate().getTime();
    return Long.compare(myExpirationDate, otherExpirationDate);
  }

  public boolean isExpired() {
    return new DateTime(expirationDate).minusDays(-1).isBeforeNow();
  }

  public boolean hasChanged() {
    Long draftAmount = draftLotItem == null ? null : draftLotItem.getAmount();
    Long currentAmount = amount == null ? null : amount;
    return ObjectUtils.notEqual(draftAmount, currentAmount);
  }
}
