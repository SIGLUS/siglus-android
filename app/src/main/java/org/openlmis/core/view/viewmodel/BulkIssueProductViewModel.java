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

import androidx.annotation.StringRes;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.DraftBulkIssueLot;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@Builder
public class BulkIssueProductViewModel implements MultiItemEntity, Comparable<BulkIssueProductViewModel> {

  public static final int TYPE_EDIT = 1;

  public static final int TYPE_DONE = 2;

  private boolean done;

  @StringRes
  private int warningRes;

  private boolean showErrorFlag;

  private StockCard stockCard;

  private Long requested;

  private DraftBulkIssueProduct draftProduct;

  private List<BulkIssueLotViewModel> lotViewModels;

  public static BulkIssueProductViewModel build(StockCard stockCard, List<LotOnHand> lotOnHandList) {
    return new BulkIssueProductViewModelBuilder()
        .stockCard(stockCard)
        .lotViewModels(FluentIterable
            .from(lotOnHandList)
            .filter(lotOnHand -> lotOnHand.getQuantityOnHand() != null && lotOnHand.getQuantityOnHand() > 0)
            .transform(BulkIssueLotViewModel::build)
            .toSortedList(BulkIssueLotViewModel::compareTo))
        .build();
  }

  public void restoreFromDraft(DraftBulkIssueProduct draftProduct) {
    setDraftProduct(draftProduct);
    setRequested(draftProduct.getRequested());
    if (CollectionUtils.isEmpty(lotViewModels)) {
      setDone(draftProduct.isDone());
      return;
    }
    boolean hasIllegalAmount = false;
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      for (DraftBulkIssueLot draftLot : draftProduct.getDraftLotListWrapper()) {
        if (!StringUtils.equals(draftLot.getLotNumber(), lotViewModel.getLotOnHand().getLot().getLotNumber())) {
          continue;
        }
        lotViewModel.restoreFromDraft(draftLot);
        if (!hasIllegalAmount && lotViewModel.isBiggerThanSoh()) {
          hasIllegalAmount = true;
        }
      }
    }

    setDone(draftProduct.isDone() && !hasIllegalAmount);
    if (getFilteredLotViewModels().isEmpty() || isAllLotsExpired()) {
      setDone(false);
    }
  }

  public DraftBulkIssueProduct convertToDraft(String documentNumber, String movementReasonCode) {
    if (draftProduct == null) {
      draftProduct = new DraftBulkIssueProduct();
    }
    return draftProduct
        .setDone(done)
        .setMovementReasonCode(movementReasonCode)
        .setStockCard(stockCard)
        .setRequested(requested)
        .setDocumentNumber(documentNumber)
        .setDraftLotListWrapper(FluentIterable
            .from(lotViewModels)
            .transform(lotViewModel -> Objects.requireNonNull(lotViewModel).convertToDraft(draftProduct))
            .toList());
  }

  public StockMovementItem convertToMovement(String movementReasonCode, String documentNumber) {
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setStockOnHand(stockCard.calculateSOHFromLots());
    stockMovementItem.setMovementType(MovementType.ISSUE);
    stockMovementItem.setRequested(requested);
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setReason(movementReasonCode);
    stockMovementItem.setDocumentNumber(documentNumber);
    stockMovementItem.setMovementDate(DateUtil.getCurrentDate());
    List<LotMovementItem> lotMovementItemListWrapper = stockMovementItem.getLotMovementItemListWrapper();
    long movementQuantity = 0;
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      if (lotViewModel.getAmount() != null) {
        movementQuantity += lotViewModel.getAmount();
      }
      LotMovementItem lotMovementItem = lotViewModel.convertToMovement();
      lotMovementItem.setReason(movementReasonCode);
      lotMovementItem.setDocumentNumber(documentNumber);
      lotMovementItem.setStockMovementItemAndUpdateMovementQuantity(stockMovementItem);
      lotMovementItemListWrapper.add(lotMovementItem);
    }
    stockMovementItem.setMovementQuantity(movementQuantity);
    stockMovementItem.setStockOnHand(stockCard.getStockOnHand() - movementQuantity);
    stockCard.setStockOnHand(stockMovementItem.getStockOnHand());
    return stockMovementItem;
  }

  public boolean validate() {
    updateErrorBannerFlag();
    if (isAllLotsExpired() || isEmptyIssue()) {
      return false;
    }
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      if (lotViewModel.isBiggerThanSoh()) {
        return false;
      }
    }
    setDone(true);
    return true;
  }

  public void setDone(boolean isDone) {
    this.done = isDone;
    if (lotViewModels == null) {
      return;
    }
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      lotViewModel.setDone(isDone);
    }
  }

  public List<BulkIssueLotViewModel> getFilteredLotViewModels() {
    return FluentIterable
        .from(lotViewModels)
        .filter(lotViewModel -> done ? lotViewModel.shouldDisplayWhenDone() : lotViewModel.shouldDisplayWhenEdit())
        .toList();
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }

  @Override
  public int compareTo(BulkIssueProductViewModel o) {
    return stockCard.compareTo(o.getStockCard());
  }

  public boolean hasChanged() {
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      if (lotViewModel.hasChanged()) {
        return true;
      }
    }
    // self check
    Long draftRequested = draftProduct == null ? null : draftProduct.getRequested();
    Long currentRequested = requested == null ? null : requested;
    boolean requestedChanged = ObjectUtils.notEqual(draftRequested, currentRequested);
    boolean statusChanged = (draftProduct == null && done) || (draftProduct != null && draftProduct.isDone() != done);
    return requestedChanged || statusChanged;
  }

  public boolean shouldShowError() {
    return showErrorFlag || isAllLotsExpired();
  }

  public void updateBannerRes() {
    updateWarningBannerRes();
    updateErrorBannerFlag();
  }

  private void updateWarningBannerRes() {
    if (isAllLotsExpired()) {
      return;
    }
    if (!lotViewModels.isEmpty() && lotViewModels.get(0).isExpired()) {
      warningRes = R.string.alert_issue_with_expired;
      return;
    }
    boolean hasEarlierConsumableLot = false;
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      if (lotViewModel.getAmount() != null && lotViewModel.getAmount() > 0) {
        if (hasEarlierConsumableLot) {
          warningRes = R.string.alert_soonest_expire;
          return;
        }
        if (lotViewModel.isSmallerThanSoh()) {
          hasEarlierConsumableLot = true;
        }
      } else {
        hasEarlierConsumableLot = true;
      }
    }
    warningRes = 0;
  }

  private void updateErrorBannerFlag() {
    showErrorFlag = isEmptyIssue();
  }

  private boolean isAllLotsExpired() {
    if (lotViewModels.isEmpty()) {
      return true;
    }
    return lotViewModels.get(lotViewModels.size() - 1).isExpired();
  }

  private boolean isEmptyIssue() {
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      if (lotViewModel.isExpired()) {
        continue;
      }
      if (lotViewModel.getAmount() != null && lotViewModel.getAmount() > 0) {
        return false;
      }
    }
    return true;
  }
}
