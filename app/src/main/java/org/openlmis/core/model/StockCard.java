/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.model;


import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "stock_cards")
public class StockCard extends BaseModel{

    public static final String DIVIDER = ",";

    @DatabaseField
    String stockCardId;

    @DatabaseField
    String expireDates;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Product product;

    @ForeignCollectionField()
    private ForeignCollection<StockItem> stockItems;

    @DatabaseField
    int stockOnHand;

    public String getEarliestExpireDate(){
        if (!StringUtils.isEmpty(expireDates)){
            List<String> stringList = Arrays.asList(expireDates.split(DIVIDER));
            Collections.sort(stringList);
            return stringList.get(0);
        }
        return null;
    }
}
