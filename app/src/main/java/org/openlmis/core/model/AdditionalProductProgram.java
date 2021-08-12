package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "additional_product_program")
public class AdditionalProductProgram extends BaseModel{

  @DatabaseField
  private String programCode;

  @DatabaseField
  private String productCode;

  @DatabaseField
  private String originProgramCode;
}
