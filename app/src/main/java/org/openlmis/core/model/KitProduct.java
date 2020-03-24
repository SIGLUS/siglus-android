package org.openlmis.core.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "kit_products")
public class KitProduct extends BaseModel {
    @DatabaseField
    String kitCode;

    @DatabaseField
    String productCode;

    @DatabaseField
    private int quantity;

    @Override
    public String toString() {
        return "["
                + "kitCode=" + kitCode + ","
                + "productCode=" + productCode + ","
                + "quantity=" + quantity
                + "]";
    }
}
