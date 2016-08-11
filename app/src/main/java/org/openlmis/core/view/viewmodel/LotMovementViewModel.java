package org.openlmis.core.view.viewmodel;

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

    public boolean isValid() {
        return lotNumber != null && expiryDate != null && quantity != null;
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

    public LotMovementViewModel() {
    }

    public LotMovementViewModel(String lotNumber, String expiryDate) {
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
    }
}
