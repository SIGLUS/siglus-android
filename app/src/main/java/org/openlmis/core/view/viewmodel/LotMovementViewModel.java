package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
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
    private String lotSoh;
    private String stockMovementId;

    boolean valid = true;

    public LotMovementViewModel() {
    }

    public LotMovementViewModel(String lotNumber, String expiryDate) {
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
    }

    public LotMovementViewModel(String lotNumber, String expiryDate, String quantityOnHand) {
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.lotSoh = quantityOnHand;
    }

    public boolean validate() {
        valid =  StringUtils.isNumeric(quantity)
                && !StringUtils.isBlank(lotNumber)
                && !StringUtils.isBlank(expiryDate)
                && !StringUtils.isBlank(quantity)
                && Long.parseLong(quantity) > 0;
        return valid;
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
