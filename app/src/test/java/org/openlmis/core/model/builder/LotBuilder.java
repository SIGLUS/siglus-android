package org.openlmis.core.model.builder;

import java.util.Date;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.Product;

public class LotBuilder {

  private final Lot lot;

  public LotBuilder() {
    lot = new Lot();
  }

  public static LotBuilder create() {
    return new LotBuilder();
  }

  public LotBuilder setProduct(Product product) {
    lot.setProduct(product);
    return this;
  }

  public LotBuilder setLotNumber(String lotNumber) {
    lot.setLotNumber(lotNumber);
    return this;
  }

  public LotBuilder setExpirationDate(Date expirationDate) {
    lot.setExpirationDate(expirationDate);
    return this;
  }

  public Lot build() {
    return lot;
  }
}
