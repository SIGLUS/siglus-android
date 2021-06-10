package org.openlmis.core.network.adapter;

import static org.openlmis.core.utils.DateUtil.DATE_TIME_FORMAT;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Date;
import org.openlmis.core.utils.DateUtil;

public class ReportDateAdapter implements JsonDeserializer<Date> {

  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return DateUtil.parseString(json.getAsJsonPrimitive().getAsString(), DATE_TIME_FORMAT);
  }

}
