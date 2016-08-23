package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;

import java.io.Serializable;

import lombok.Data;

@Data
public class LotMovementViewModel implements Serializable {

    private String lotNumber;
    private String expiryDate;
    private String quantity;
    private String lotSoh = "0";
    private MovementReasonManager.MovementType movementType;

    boolean valid = true;
    boolean quantityValid = true;

    public LotMovementViewModel() {
    }

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
            if (!StringUtils.isBlank(quantity)) {
                quantityValid = Long.parseLong(quantity) <= Long.parseLong(lotSoh);
            }
        }
        return quantityValid;
    }

    public LotMovementItem convertViewToModel(Product product) {
        LotMovementItem lotMovementItem = new LotMovementItem();
        Lot lot = new Lot();
        lot.setProduct(product);
        lot.setLotNumber(lotNumber);
        lot.setExpirationDate(DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        lotMovementItem.setLot(lot);
        lotMovementItem.setMovementQuantity(Long.parseLong(quantity));
        return lotMovementItem;
    }

    public boolean hasQuantityChanged() {
        return !StringUtils.isBlank(quantity) && Long.parseLong(quantity) > 0;
    }
}
