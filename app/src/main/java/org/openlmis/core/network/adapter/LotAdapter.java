package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.utils.DateUtil;

public class LotAdapter implements JsonDeserializer<Lot> {

  @Override
  public Lot deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    return new Gson().fromJson(json, LotResponse.class).convertToLot();
  }

  private class LotResponse {

    String expirationDate;
    String lotCode;

    public Lot convertToLot() {
      Lot lot = new Lot();
      lot.setExpirationDate(DateUtil.parseString(expirationDate, DateUtil.DB_DATE_FORMAT));
      lot.setLotNumber(lotCode);
      return lot;
    }
  }
}
