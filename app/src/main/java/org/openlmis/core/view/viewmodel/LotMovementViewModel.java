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

import java.util.Calendar;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LotMovementViewModel {

  private String lotNumber;
  private String expiryDate;
  private String quantity;
  private String lotSoh;
  private MovementReasonManager.MovementType movementType;
  private String movementReason;
  private String documentNumber;
  boolean valid = true;
  boolean quantityLessThanSoh = true;
  boolean isDataChanged = false;
  private String from;

  public LotMovementViewModel(String lotNumber, String expiryDate, MovementReasonManager.MovementType movementType) {
    this.lotNumber = lotNumber;
    this.expiryDate = expiryDate;
    this.movementType = movementType;
  }

  public LotMovementViewModel(String lotNumber, String expiryDate, String quantityOnHand,
      MovementReasonManager.MovementType movementType) {
    this.lotNumber = lotNumber;
    this.expiryDate = expiryDate;
    this.lotSoh = quantityOnHand;
    this.movementType = movementType;
  }

  public boolean validateQuantityNotGreaterThanSOH() {
    if (movementType.isNegative()) {
      quantityLessThanSoh = StringUtils.isBlank(quantity) || Long.parseLong(quantity) <= Long.parseLong(lotSoh);
    }
    return quantityLessThanSoh;
  }

  public LotMovementItem convertViewToModel(Product product) {
    Lot lot = new Lot();
    lot.setProduct(product);
    lot.setLotNumber(lotNumber);
    lot.setExpirationDate(DateUtil.getActualMaximumDate(
        DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
    LotMovementItem lotMovementItem = new LotMovementItem();
    lotMovementItem.setLot(lot);

    if (StringUtils.isNoneBlank(quantity)) {
      lotMovementItem.setMovementQuantity(Long.parseLong(quantity));
    } else {
      lotMovementItem.setMovementQuantity(null);
    }
    return lotMovementItem;
  }

  public boolean quantityGreaterThanZero() {
    return !StringUtils.isBlank(quantity) && Long.parseLong(quantity) > 0;
  }

  public LotMovementItem convertViewToModelAndResetSOH(Product product) {
    Lot lot = new Lot();
    lot.setProduct(product);
    lot.setLotNumber(lotNumber);
    lot.setExpirationDate(
        DateUtil.getActualMaximumDate(DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
    LotMovementItem lotMovementItem = new LotMovementItem();
    lotMovementItem.setLot(lot);
    long currentStockOnHand = Long.parseLong(getQuantity());
    long previousStockOnHand = Long.parseLong(getLotSoh());
    lotMovementItem.setStockOnHand(currentStockOnHand);
    lotMovementItem.setMovementQuantity(currentStockOnHand - previousStockOnHand);
    return lotMovementItem;
  }

  public boolean validateLotWithPositiveQuantity() {
    valid = StringUtils.isNumeric(quantity)
        && !StringUtils.isBlank(lotNumber)
        && !StringUtils.isBlank(expiryDate)
        && !StringUtils.isBlank(quantity)
        && Long.parseLong(quantity) > 0;
    return valid || (getMovementType() == MovementReasonManager.MovementType.ISSUE
        && !isExpiredLot());
  }

  public boolean validateLotWithNoEmptyFields() {
    valid = StringUtils.isNumeric(quantity)
        && !StringUtils.isBlank(lotNumber)
        && !StringUtils.isBlank(expiryDate)
        && !StringUtils.isBlank(quantity);
    return valid || (getMovementType() == MovementReasonManager.MovementType.ISSUE
        && !isExpiredLot());
  }

  public boolean validateLot() {
    valid = StringUtils.isNumeric(quantity)
        && !StringUtils.isBlank(lotNumber)
        && !StringUtils.isBlank(expiryDate)
        && !StringUtils.isBlank(quantity)
        && !StringUtils.isBlank(movementReason)
        && !StringUtils.isBlank(documentNumber);
    return valid;
  }

  public boolean isExpiredLot() {
    if (getExpiryDate() == null) {
      return true;
    }
    Calendar nowCalender = Calendar.getInstance();

    Date expireDate = DateUtil
        .parseString(getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
    if (expireDate != null) {
      Calendar expireCalender = Calendar.getInstance();
      expireCalender.setTime(expireDate);
      expireCalender.add(Calendar.MONTH, 1);
      expireCalender.set(Calendar.DAY_OF_MONTH, 1);
      return expireCalender.before(nowCalender);
    }
    return false;
  }

  public boolean isNewAdded() {
    return StringUtils.isBlank(lotSoh);
  }

  public static String generateLotNumberForProductWithoutLot(String productCode,
      String expiryDate) {
    try {
      return "SEM-LOTE-" + productCode.toUpperCase() + "-" + DateUtil
          .convertDate(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR,
              DateUtil.DATE_DIGIT_FORMAT_ONLY_MONTH_AND_YEAR);
    } catch (Exception e) {
      new LMISException(e, "LotMovementViewModel.generateLotNumberForProductWithoutLot").reportToFabric();
    }
    return null;
  }

  public long getAdjustmentQuantity() {
    long returnLong = 0;
    try {
      if (StringUtils.isBlank(lotSoh)) {
        return Long.parseLong(quantity);
      }
      returnLong = (Long.parseLong(this.getQuantity()) - Long.parseLong(this.getLotSoh()));
    } catch (NumberFormatException e) {
      new LMISException(e, "LotMovementViewModel.getAdjustmentQuantity").reportToFabric();
    }
    return returnLong;
  }

  @Override
  public String toString() {
    return "LotMovementViewModel<"
        + "lotNumber='" + lotNumber + '\''
        + ", expiryDate='" + expiryDate + '\''
        + ", quantity='" + quantity + '\''
        + ", lotSoh='" + lotSoh + '\''
        + ", movementType=" + movementType
        + ", valid=" + valid
        + ", quantityLessThanSoh=" + quantityLessThanSoh
        + ", isDataChanged=" + isDataChanged
        + ", isExpire=" + isExpiredLot()
        + '>';
  }
}
