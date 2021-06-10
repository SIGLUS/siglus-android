package org.openlmis.core.network.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.utils.DateUtil;

@Data
@NoArgsConstructor
public class LotMovementEntry {

  private String lotNumber;

  private String expirationDate;

  private Long quantity;

  private Map<String, String> customProps = new HashMap<>();

  public LotMovementEntry(LotMovementItem lotMovementItem) {
    this.lotNumber = lotMovementItem.getLot().getLotNumber();
    this.expirationDate = DateUtil
        .formatDate(lotMovementItem.getLot().getExpirationDate(), DateUtil.DB_DATE_FORMAT);
    this.quantity = lotMovementItem.getMovementQuantity();
    this.customProps.put("SOH", "" + lotMovementItem.getStockOnHand());
  }
}
