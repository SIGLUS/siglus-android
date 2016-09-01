package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;

import java.io.Serializable;

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
    boolean quantityValid = true;
    boolean hasDataChanged = false;

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

    public boolean validate() {
        valid = StringUtils.isNumeric(quantity)
                && !StringUtils.isBlank(lotNumber)
                && !StringUtils.isBlank(expiryDate)
                && !StringUtils.isBlank(quantity)
                && Long.parseLong(quantity) > 0;
        return valid;
    }

    public boolean validateQuantity(MovementReasonManager.MovementType movementType) {
        if (MovementReasonManager.MovementType.ISSUE.equals(movementType)
                || MovementReasonManager.MovementType.NEGATIVE_ADJUST.equals(movementType)) {
            quantityValid = StringUtils.isBlank(quantity) || Long.parseLong(quantity) <= Long.parseLong(lotSoh);
        }
        return quantityValid;
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
        if(lotSoh==null) setLotSoh("0");
        this.setLotSoh(quantity);
        LotMovementItem lotMovementItem = new LotMovementItem();
        Lot lot = new Lot();
        lot.setProduct(product);
        lot.setLotNumber(lotNumber);
        lot.setExpirationDate(DateUtil.getActualMaximumDate(DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
        lotMovementItem.setLot(lot);
        lotMovementItem.setStockOnHand(Long.parseLong(lotSoh));
        lotMovementItem.setMovementQuantity(0L);
        return lotMovementItem;
    }

    public boolean validateExistingLot() {
        valid = StringUtils.isNumeric(quantity)
                && !StringUtils.isBlank(lotNumber)
                && !StringUtils.isBlank(expiryDate)
                && !StringUtils.isBlank(quantity);
        return valid;
    }
}
