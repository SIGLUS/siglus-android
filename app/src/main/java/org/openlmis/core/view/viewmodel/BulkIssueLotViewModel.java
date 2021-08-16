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

import static org.openlmis.core.view.viewmodel.BulkIssueProductViewModel.TYPE_DONE;
import static org.openlmis.core.view.viewmodel.BulkIssueProductViewModel.TYPE_EDIT;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.openlmis.core.model.DraftBulkIssueLot;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;

@Data
@Builder
public class BulkIssueLotViewModel implements Comparable<BulkIssueLotViewModel>, MultiItemEntity {

  private Long amount;

  private LotOnHand lotOnHand;

  private DraftBulkIssueLot draftLotItem;

  private boolean done;

  public static BulkIssueLotViewModel build(LotOnHand lotOnHand) {
    return new BulkIssueLotViewModelBuilder()
        .lotOnHand(lotOnHand)
        .build();
  }

  public void restoreFromDraft(DraftBulkIssueLot draftLot) {
    setAmount(draftLot.getAmount());
    setDraftLotItem(draftLot);
  }

  public DraftBulkIssueLot convertToDraft(DraftBulkIssueProduct draftProduct) {
    if (draftLotItem == null) {
      draftLotItem = new DraftBulkIssueLot();
    }
    return draftLotItem
        .setAmount(amount)
        .setLotNumber(lotOnHand.getLot().getLotNumber())
        .setDraftBulkIssueProduct(draftProduct);
  }

  public LotMovementItem convertToMovement() {
    LotMovementItem lotMovementItem = new LotMovementItem();
    lotMovementItem.setLot(lotOnHand.getLot());
    lotMovementItem.setMovementQuantity(amount);
    return lotMovementItem;
  }

  @Override
  public int compareTo(BulkIssueLotViewModel other) {
    long otherExpirationDate = other.getLotOnHand().getLot().getExpirationDate().getTime();
    long myExpirationDate = getLotOnHand().getLot().getExpirationDate().getTime();
    return Long.compare(myExpirationDate, otherExpirationDate);
  }

  public boolean isExpired() {
    return lotOnHand.getLot().isExpired();
  }

  public boolean hasChanged() {
    Long draftAmount = draftLotItem == null ? null : draftLotItem.getAmount();
    Long currentAmount = amount == null ? null : amount;
    return ObjectUtils.notEqual(draftAmount, currentAmount);
  }

  public boolean shouldDisplayWhenDone() {
    return isExpired() || (amount != null && amount != 0);
  }

  public boolean shouldDisplayWhenEdit() {
    return lotOnHand.getQuantityOnHand() != null && lotOnHand.getQuantityOnHand() > 0;
  }

  public boolean isBiggerThanSoh() {
    if (amount == null || amount == 0) {
      return false;
    }
    Long currentSoh = lotOnHand.getQuantityOnHand();
    return currentSoh != null && amount > currentSoh;
  }

  public boolean isSmallerThanSoh() {
    if (amount == null || amount == 0) {
      return true;
    }
    Long currentSoh = lotOnHand.getQuantityOnHand();
    return currentSoh != null && amount < currentSoh;
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }
}
