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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

@Data
public class BulkEntriesViewModel extends InventoryViewModel {

  public enum InvalidType {
    NO_LOT,
    EXISTING_LOT_ALL_BLANK,
    NEW_LOT_BLANK,
    DEFAULT
  }

  private boolean done;

  private Product product;

  private Long quantity;

  private List<LotMovementViewModel> lotMovementViewModels;

  private InvalidType invalidType;

  public BulkEntriesViewModel(Product product) {
    super(product);
    this.product = product;
  }

  public BulkEntriesViewModel(StockCard stockCard) {
    super(stockCard);
    this.product = stockCard.getProduct();
  }

  public BulkEntriesViewModel(Product product, boolean done, Long quantity,
      List<LotMovementViewModel> lotMovementViewModels) {
    super(product);
    this.done = done;
    this.product = product;
    this.quantity = quantity;
    this.lotMovementViewModels = lotMovementViewModels;
  }

  public SpannableStringBuilder getGreenName() {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getFormattedProductName());
    spannableStringBuilder.setSpan(new ForegroundColorSpan(
            ContextCompat.getColor(LMISApp.getInstance(), R.color.color_primary)),
        0, getFormattedProductName().length(), Spanned.SPAN_POINT_MARK);
    return spannableStringBuilder;
  }

  @Override
  public boolean validate() {
    if (newLotMovementViewModelList.isEmpty()) {
      return validExistingLotMovementViewModelList();
    } else {
      return validNewLotMovementViewModelList();
    }
  }

  private boolean validExistingLotMovementViewModelList() {
    if (existingLotMovementViewModelList.isEmpty()) {
      invalidType = InvalidType.NO_LOT;
      return false;
    } else {
      for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
        if (lotMovementViewModel.getQuantity() != null && !lotMovementViewModel.getQuantity().equals("")) {
          invalidType = InvalidType.DEFAULT;
          return true;
        }
      }
    }
    invalidType = InvalidType.EXISTING_LOT_ALL_BLANK;
    return false;
  }


  private boolean validNewLotMovementViewModelList() {
    for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
      if (lotMovementViewModel.getQuantity() == null || lotMovementViewModel.getQuantity().isEmpty()) {
        lotMovementViewModel.setValid(false);
        invalidType = InvalidType.NEW_LOT_BLANK;
        return false;
      }
    }
    invalidType = InvalidType.DEFAULT;
    return true;
  }
}
