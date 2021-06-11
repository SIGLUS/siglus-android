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
import java.util.List;
import lombok.Data;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;

@Data
@SuppressWarnings("PMD")
public class BulkInitialInventoryViewModel extends InventoryViewModel {

  private static final String TAG = BulkInitialInventoryViewModel.class.getSimpleName();

  private DraftInitialInventory draftInventory;
  private boolean done;
  private String from;

  public BulkInitialInventoryViewModel(StockCard stockCard) {
    super(stockCard);
  }

  public BulkInitialInventoryViewModel(Product product) {
    super(product);
  }

  @Override
  public boolean validate() {
    return done;
  }

  @Override
  public boolean isDataChanged() {
    if (draftInventory == null) {
      return hasLotInInventoryModelChanged();
    }

    return isDifferentFromDraft();
  }

  private boolean isDifferentFromDraft() {
    // 数据库中读取的和当前界面上的值是否有改动
    List<DraftInitialInventoryLotItem> existingDraftLotItems = draftInventory
        .getDraftLotItemListWrapper();
    for (DraftInitialInventoryLotItem draftLotItem : existingDraftLotItems) {
      for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
        if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
          if (!String.valueOf(draftLotItem.getQuantity() == null ? "" : draftLotItem.getQuantity())
              .equals(existingLotMovementViewModel.getQuantity())) {
            return true;
          }
        }
      }
    }
    return newLotMovementViewModelList.size() != 0;
  }

  private boolean hasLotInInventoryModelChanged() {
    for (LotMovementViewModel viewModel : getExistingLotMovementViewModelList()) {
      if (viewModel.getQuantity() != null && !viewModel.getQuantity().isEmpty()) {
        return true;
      }
    }
    if (newLotMovementViewModelList.size() > 0) {
      return true;
    }
    for (LotMovementViewModel viewModel : getNewLotMovementViewModelList()) {
      if (viewModel.getQuantity() != null && !viewModel.getQuantity().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public String getFormattedProductName() {
    return product.getFormattedProductNameWithoutStrengthAndType();
  }

  public String getFormattedProductUnit() {
    return product.getStrength() + " " + product.getType();
  }

  public SpannableStringBuilder getGreenName() {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
        getFormattedProductName());
    spannableStringBuilder.setSpan(new ForegroundColorSpan(
            LMISApp.getInstance().getResources().getColor(R.color.color_primary)), 0,
        getFormattedProductName().length(), Spanned.SPAN_POINT_MARK);
    return spannableStringBuilder;
  }

  public SpannableStringBuilder getGreenUnit() {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
        getFormattedProductUnit());
    spannableStringBuilder.setSpan(new ForegroundColorSpan(
            LMISApp.getInstance().getResources().getColor(R.color.color_primary)), 0,
        getFormattedProductUnit().length(), Spanned.SPAN_POINT_MARK);
    return spannableStringBuilder;
  }

  public void setInitialDraftInventory(DraftInitialInventory draftInventory) {
    this.draftInventory = draftInventory;
    done = draftInventory.isDone();
    populateLotMovementModelWithDraftLotItem();
  }

  private void populateLotMovementModelWithDraftLotItem() {
    for (DraftInitialInventoryLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
      LotMovementViewModel existLotMovementViewModel = new LotMovementViewModel();
      existLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
      existLotMovementViewModel.setLotNumber(draftLotItem.getLotNumber());
      existLotMovementViewModel.setExpiryDate(DateUtil
          .formatDate(draftLotItem.getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
      getExistingLotMovementViewModelList().add(existLotMovementViewModel);
    }
  }

  private String formatQuantity(Long quantity) {
    return quantity == null ? "" : quantity.toString();
  }
}
