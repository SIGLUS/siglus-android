package org.openlmis.core.model;

import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "products")
public class Kit extends Product {
    private List<Product> products;
}
