package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "MIMIA_product_items")
public class MIMIAProductItem extends BaseModel{
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Product product;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private MIMIAForm form;

    @DatabaseField
    private int initialAmount;
    @DatabaseField
    private int received;
    @DatabaseField
    private int issued;
    @DatabaseField
    private int adjustment;
    @DatabaseField
    private int inventory;
    @DatabaseField
    private String validate;
}
