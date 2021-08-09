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

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.openlmis.core.model.DraftBulkIssueLot;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotOnHand;

@Data
@Builder
public class BulkIssueLotViewModel implements Comparable<BulkIssueLotViewModel> {

  private Long amount;

  private LotOnHand lotOnHand;

  private DraftBulkIssueLot draftLotItem;

  public static BulkIssueLotViewModel buildFromDraft(DraftBulkIssueLot draftLotItem) {
    return new BulkIssueLotViewModelBuilder()
        .amount(draftLotItem.getAmount())
        .lotOnHand(draftLotItem.getLotOnHand())
        .draftLotItem(draftLotItem)
        .build();
  }

  public static BulkIssueLotViewModel buildFromLotOnHand(LotOnHand lotOnHand) {
    return new BulkIssueLotViewModelBuilder()
        .lotOnHand(lotOnHand)
        .build();
  }

  public DraftBulkIssueLot convertToDraft(DraftBulkIssueProduct draftProduct) {
    if (draftLotItem == null) {
      draftLotItem = new DraftBulkIssueLot();
    }
    return draftLotItem
        .setAmount(amount)
        .setLotOnHand(lotOnHand)
        .setDraftBulkIssueProduct(draftProduct);
  }

  @Override
  public int compareTo(BulkIssueLotViewModel other) {
    long otherExpirationDate = other.getLotOnHand().getLot().getExpirationDate().getTime();
    long myExpirationDate = getLotOnHand().getLot().getExpirationDate().getTime();
    return Long.compare(myExpirationDate, otherExpirationDate);
  }

  public boolean isExpired() {
    return new DateTime(lotOnHand.getLot().getExpirationDate()).plusDays(1).isBeforeNow();
  }

  public boolean hasChanged() {
    Long draftAmount = draftLotItem == null ? null : draftLotItem.getAmount();
    Long currentAmount = amount == null ? null : amount;
    return ObjectUtils.notEqual(draftAmount, currentAmount);
  }
}
