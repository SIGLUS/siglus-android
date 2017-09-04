package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;

import java.io.Serializable;
import java.text.ParseException;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LotMovementViewModel implements Serializable {

    private String lotNumber;
    private String expiryDate;
    private String quantity;
    private String lotSoh;
    private MovementReasonManager.MovementType movementType;

    boolean valid = true;
    boolean quantityLessThanSoh = true;
    boolean isDataChanged = false;

    public LotMovementViewModel(String lotNumber, String expiryDate, MovementReasonManager.MovementType movementType) {
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.movementType = movementType;
    }

    public LotMovementViewModel(String lotNumber, String expiryDate, String quantityOnHand, MovementReasonManager.MovementType movementType) {
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.lotSoh = quantityOnHand;
        this.movementType = movementType;
    }

    public LotMovementViewModel(String lotNumber, String expiryDate, MovementReasonManager.MovementType movementType, String quantity) {
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.movementType = movementType;
        this.quantity = quantity;
    }

    public boolean validateQuantityNotGreaterThanSOH() {
        if (movementType.isNegative()) {
            quantityLessThanSoh = StringUtils.isBlank(quantity) || Long.parseLong(quantity) <= Long.parseLong(lotSoh);
        }
        return quantityLessThanSoh;
    }

    public LotMovementItem convertViewToModel(Product product) {
        LotMovementItem lotMovementItem = new LotMovementItem();
        Lot lot = new Lot();
        lot.setProduct(product);
        lot.setLotNumber(lotNumber);
        lot.setExpirationDate(DateUtil.getActualMaximumDate(DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
        lotMovementItem.setLot(lot);

        lotMovementItem.setMovementQuantity(Long.parseLong(quantity));
        return lotMovementItem;
    }

    public boolean quantityGreaterThanZero() {
        return !StringUtils.isBlank(quantity) && Long.parseLong(quantity) > 0;
    }

    public LotMovementItem convertViewToModelAndResetSOH(Product product) {
        LotMovementItem lotMovementItem = new LotMovementItem();
        long previousStockOnHand = Long.parseLong(getLotSoh());
        long currentStockOnHand = Long.parseLong(getQuantity());
        Lot lot = new Lot();
        lot.setProduct(product);
        lot.setLotNumber(lotNumber);
        lot.setExpirationDate(DateUtil.getActualMaximumDate(DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
        lotMovementItem.setLot(lot);
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
        return valid;
    }

    public boolean validateLotWithNoEmptyFields() {
        valid = StringUtils.isNumeric(quantity)
                && !StringUtils.isBlank(lotNumber)
                && !StringUtils.isBlank(expiryDate)
                && !StringUtils.isBlank(quantity);
        return valid;
    }

    public boolean isNewAdded() {
        return StringUtils.isBlank(lotSoh);
    }

    public static String generateLotNumberForProductWithoutLot(String productCode, String expiryDate) {
        try {
            return "SEM-LOTE-" + productCode.toUpperCase() + "-" + DateUtil.convertDate(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR, DateUtil.DATE_DIGIT_FORMAT_ONLY_MONTH_AND_YEAR);
        } catch (ParseException e) {
            new LMISException(e).reportToFabric();
        }
        return null;
    }

    public int getAdjustmentQuantity() {
        if (StringUtils.isBlank(lotSoh)) {
            return Integer.parseInt(quantity);
        }
        return (Integer.parseInt(this.getQuantity()) - Integer.parseInt(this.getLotSoh()));
    }
}
