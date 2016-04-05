package org.openlmis.core.model;

import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "program_products")
public class ProgramProduct {
    private String programCode;
    private String productCode;
    private boolean isActive;
}
