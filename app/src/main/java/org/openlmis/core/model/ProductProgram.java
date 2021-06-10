package org.openlmis.core.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "product_programs")
public class ProductProgram extends BaseModel {

  @DatabaseField
  private String programCode;

  @DatabaseField
  private String productCode;

  @DatabaseField
  @SerializedName("active")
  private boolean isActive;

  @DatabaseField
  private String category;

//    @DatabaseField
//    private long versionCode;
}
