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


import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.DateUtil;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "stock_cards")
public class StockCard extends BaseModel implements Comparable<StockCard> {

    public static final String DIVIDER = ",";

    @DatabaseField
    String expireDates;

    @Expose
    @SerializedName("product")
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Product product;

    @Expose
    @SerializedName("stockMovementItems")
    private List<StockMovementItem> stockMovementItemsWrapper;

    @ForeignCollectionField()
    private ForeignCollection<StockMovementItem> foreignStockMovementItems;

    @Expose
    @DatabaseField
    @SerializedName("stockOnHand")
    long stockOnHand;

    public String getEarliestExpireDate() {
        if (!StringUtils.isEmpty(expireDates)) {
            List<String> expireDateList = Arrays.asList(expireDates.split(DIVIDER));
            DateUtil.sortByDate(expireDateList);
            return expireDateList.get(0);
        }
        return null;
    }

    @Override
    public int compareTo(@NonNull StockCard another) {
        return product == null ? 0 : product.compareTo(another.getProduct());
    }

    public StockMovementItem generateInitialStockMovementItem() {
        StockMovementItem initInventory = new StockMovementItem(this);
        initInventory.setReason(MovementReasonManager.INVENTORY);
        initInventory.setMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY);
        initInventory.setMovementQuantity(stockOnHand);
        return initInventory;
    }
}
