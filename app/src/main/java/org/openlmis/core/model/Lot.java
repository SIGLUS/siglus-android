package org.openlmis.core.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import lombok.Data;
import org.openlmis.core.utils.DateUtil;

@Data
@DatabaseTable(tableName = "lots")
public class Lot extends BaseModel {

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField
  String lotNumber;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DB_DATE_FORMAT)
  Date expirationDate;
}
