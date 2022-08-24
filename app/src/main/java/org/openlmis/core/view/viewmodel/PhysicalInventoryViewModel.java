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

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.ContextCompat;
import java.util.List;
import lombok.Data;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class PhysicalInventoryViewModel extends InventoryViewModel {

  private DraftInventory draftInventory;
  private boolean done;
  private String from;

  public PhysicalInventoryViewModel(StockCard stockCard) {
    super(stockCard);
  }

  public PhysicalInventoryViewModel(StockCard stockCard, long stockOnHand) {
    super(stockCard, stockOnHand);
  }

  @Override
  public boolean validate() {
    valid = !checked || (validateNewLotList() && validateExistingLot()) || product.isArchived();
    done = valid;
    return valid;
  }

  @Override
  public boolean isDataChanged() {
    if (draftInventory == null) {
      return hasLotInInventoryModelChanged();
    }
    return !draftInventory.getDraftLotItemListWrapper().isEmpty() && isDifferentFromDraft();
  }

  @SuppressWarnings("squid:S3776")
  private boolean isDifferentFromDraft() {
    List<DraftLotItem> newAddedDraftLotItems = FluentIterable
        .from(draftInventory.getDraftLotItemListWrapper())
        .filter(DraftLotItem::isNewAdded).toList();
    List<DraftLotItem> existingDraftLotItems = FluentIterable
        .from(draftInventory.getDraftLotItemListWrapper())
        .filter(draftLotItem -> !draftLotItem.isNewAdded()).toList();
    for (DraftLotItem draftLotItem : existingDraftLotItems) {
      for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
        boolean isLotNumberSame = draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber());
        boolean isLotQuantitySame = !String.valueOf(draftLotItem.getQuantity() == null
            ? "" : draftLotItem.getQuantity()).equals(existingLotMovementViewModel.getQuantity());
        if (isLotNumberSame && isLotQuantitySame) {
          return true;
        }
      }
    }
    for (DraftLotItem draftLotItem : newAddedDraftLotItems) {
      if (newAddedDraftLotItems.size() != newLotMovementViewModelList.size()) {
        return true;
      }
      for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
        boolean isLotNumberSame = draftLotItem.getLotNumber().equals(lotMovementViewModel.getLotNumber());
        boolean isLotQuantitySame = !String.valueOf(draftLotItem.getQuantity() == null
            ? "" : draftLotItem.getQuantity()).equals(lotMovementViewModel.getQuantity());
        if (isLotNumberSame && isLotQuantitySame) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasLotInInventoryModelChanged() {
    for (LotMovementViewModel viewModel : getExistingLotMovementViewModelList()) {
      if (viewModel.getQuantity() != null && !viewModel.getQuantity().isEmpty()) {
        return true;
      }
    }
    if (!newLotMovementViewModelList.isEmpty()) {
      return true;
    }
    for (LotMovementViewModel viewModel : getNewLotMovementViewModelList()) {
      if (viewModel.getQuantity() != null && !viewModel.getQuantity().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private boolean validateExistingLot() {
    for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
      if (!lotMovementViewModel.validateLotWithNoEmptyFields()) {
        return false;
      }
    }
    return true;
  }

  public SpannableStringBuilder getGreenName() {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
        getFormattedProductName());
    spannableStringBuilder.setSpan(new ForegroundColorSpan(
            ContextCompat.getColor(LMISApp.getInstance(), R.color.color_primary)), 0,
        getFormattedProductName().length(), Spanned.SPAN_POINT_MARK);
    return spannableStringBuilder;
  }

  public SpannableStringBuilder getGreenUnit() {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
        getFormattedProductUnit());
    spannableStringBuilder.setSpan(new ForegroundColorSpan(
            ContextCompat.getColor(LMISApp.getInstance(), R.color.color_primary)), 0,
        getFormattedProductUnit().length(), Spanned.SPAN_POINT_MARK);
    return spannableStringBuilder;
  }

  public void setDraftInventory(DraftInventory draftInventory) {
    this.draftInventory = draftInventory;
    done = draftInventory.isDone() && draftInventory.getDraftLotItemListWrapper().size()
        == (existingLotMovementViewModelList.size() + newLotMovementViewModelList.size());
    populateLotMovementModelWithDraftLotItem();
  }

  private void populateLotMovementModelWithDraftLotItem() {
    for (DraftLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
      if (draftLotItem.isNewAdded()) {
        if (isNotInExistingLots(draftLotItem)) {
          LotMovementViewModel newLotMovementViewModel = new LotMovementViewModel();
          newLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
          newLotMovementViewModel.setLotNumber(draftLotItem.getLotNumber());
          newLotMovementViewModel.setExpiryDate(DateUtil
              .formatDate(draftLotItem.getExpirationDate(),
                  DateUtil.DB_DATE_FORMAT));
          getNewLotMovementViewModelList().add(newLotMovementViewModel);
        }
      } else {
        for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
          if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
            existingLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
          }
        }
      }
    }
  }

  private boolean isNotInExistingLots(DraftLotItem draftLotItem) {
    for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
      if (draftLotItem.getLotNumber().equalsIgnoreCase(lotMovementViewModel.getLotNumber())) {
        return false;
      }
    }

    for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
      if (draftLotItem.getLotNumber().equalsIgnoreCase(lotMovementViewModel.getLotNumber())) {
        return false;
      }
    }
    return true;
  }

  private String formatQuantity(Long quantity) {
    return quantity == null ? "" : quantity.toString();
  }
}
