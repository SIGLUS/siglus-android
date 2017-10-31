package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "treatment")
public class Treatment extends BaseModel {

    @DatabaseField(columnName = "implementation", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private Implementation implementation;

    @DatabaseField(columnName = "product", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private Product product;

    @DatabaseField
    private long amount;

    @DatabaseField
    private long stock;

    public Treatment(Product product, long amount, long stock) {
        this.product = product;
        this.amount = amount;
        this.stock = stock;
    }
}
