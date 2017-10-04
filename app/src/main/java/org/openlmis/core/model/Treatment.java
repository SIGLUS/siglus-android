package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "treatment")
public class Treatment extends BaseModel {

    public Treatment() {
    }

    public Treatment(long amount, long stock) {
        this.amount = amount;
        this.stock = stock;
    }

    @DatabaseField(columnName = "implementation", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private Implementation implementation;

    @DatabaseField(columnName = "product", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private Product product;

    @DatabaseField
    private long amount;

    @DatabaseField
    private long stock;
}
