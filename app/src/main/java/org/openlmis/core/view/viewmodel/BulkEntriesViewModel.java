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
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

@Data
public class BulkEntriesViewModel extends InventoryViewModel {

  public enum ValidationType {
    NO_LOT,
    EXISTING_LOT_ALL_BLANK,
    NEW_LOT_BLANK,
    VALID
  }

  private boolean done;

  private Long quantity;

  private List<LotMovementViewModel> lotMovementViewModels;

  private ValidationType validationType;

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
    boolean existingLotsValidation = validExistingLotMovementViewModelList();
    boolean newLotsValidation = validNewLotMovementViewModelList();
    return existingLotsValidation && newLotsValidation;
  }

  private boolean validExistingLotMovementViewModelList() {
    if (newLotMovementViewModelList.isEmpty() && existingLotMovementViewModelList.isEmpty()) {
      validationType = ValidationType.NO_LOT;
      return false;
    } else if (!newLotMovementViewModelList.isEmpty() && existingLotMovementViewModelList.isEmpty()) {
      return true;
    } else {
      boolean productFlag = false;
      boolean lotFlag = true;
      for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
        if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
          productFlag = true;
          if(!lotMovementViewModel.validateLot()) {
            lotFlag = false;
            lotMovementViewModel.setValid(false);
          }
        }
      }
      if (productFlag && lotFlag) {
        validationType = ValidationType.VALID;
        return true;
      } else if (!productFlag && newLotMovementViewModelList.isEmpty()) {
        validationType = ValidationType.EXISTING_LOT_ALL_BLANK;
        return false;
      }
    }
    return false;
  }


  private boolean validNewLotMovementViewModelList() {
    if (newLotMovementViewModelList.isEmpty()) {
      return true;
    }
    boolean flag = true;
    for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
      if (!lotMovementViewModel.validateLot()) {
        lotMovementViewModel.setValid(false);
        flag = false;
        validationType = ValidationType.NEW_LOT_BLANK;
      }
    }
    if (!flag) {
      return false;
    }
    validationType = ValidationType.VALID;
    return true;
  }
}
