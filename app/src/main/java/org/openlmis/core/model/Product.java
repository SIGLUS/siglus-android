package org.openlmis.core.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
@DatabaseTable(tableName = "products")
public class Product {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    String name;

    @DatabaseField
    String unit;

    List<Date> expiredDateList;
}
