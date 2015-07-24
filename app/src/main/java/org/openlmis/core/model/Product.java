package org.openlmis.core.model;


import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Product {
    String name;
    String unit;
    List<Date> expiredDateList;
}
