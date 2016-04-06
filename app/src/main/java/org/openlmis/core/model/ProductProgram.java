package org.openlmis.core.model;

import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "product_programs")
public class ProductProgram {
    private String programCode;
    private String productCode;
    private boolean isActive;
}
