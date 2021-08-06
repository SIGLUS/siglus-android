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

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ListUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "stock_cards")
public class StockCard extends BaseModel implements Comparable<StockCard> {

    public static final String DIVIDER = ",";

    @Deprecated
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
    private Collection<StockMovementItem> foreignStockMovementItems;

    @Expose
    @DatabaseField
    @SerializedName("stockOnHand")
    long stockOnHand;

    @ForeignCollectionField()
    private ForeignCollection<LotOnHand> foreignLotOnHandList;

    @Expose
    @SerializedName("lotsOnHand")
    private List<LotOnHand> lotOnHandListWrapper;

    @DatabaseField
    private float avgMonthlyConsumption = -1;

    @Override
    public int compareTo(@NonNull StockCard another) {
        return product == null ? 0 : product.compareTo(another.getProduct());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StockCard) {
            return ((StockCard) o).getProduct().equals(getProduct());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (getProduct() != null && getProduct().getCode() != null) {
            return getProduct().hashCode();
        }
        return super.hashCode();
    }

    public StockMovementItem generateInitialStockMovementItem() {
        StockMovementItem initInventory = new StockMovementItem(this);
        initInventory.setReason(MovementReasonManager.INVENTORY);
        initInventory.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        initInventory.setMovementQuantity(stockOnHand);
        return initInventory;
    }

    public List<StockMovementItem> getStockMovementItemsWrapper() {
        stockMovementItemsWrapper = ListUtil.wrapOrEmpty(foreignStockMovementItems, stockMovementItemsWrapper);
        return stockMovementItemsWrapper;
    }

    public float getCMM() {
        return (int) (this.avgMonthlyConsumption * 100 + 0.5) / 100f;
    }

    public boolean isOverStock() {
        return product.isHiv() ? stockOnHand > (int) Math.ceil(this.avgMonthlyConsumption * 3) : stockOnHand > (int) Math.ceil(this.avgMonthlyConsumption * 2);
    }

    public boolean isLowStock() {
        return stockOnHand < (int) Math.ceil(this.avgMonthlyConsumption * 1);
    }

    public long calculateSOHFromLots() {
        List<LotOnHand> lots = getLotOnHandListWrapper();
        if (CollectionUtils.isEmpty(lots) || product.isKit) {
            return stockOnHand;
        }

        long totalSOHFromLots = 0L;
        for (LotOnHand lotOnHand : lots) {
            totalSOHFromLots += lotOnHand.getQuantityOnHand();
        }
        return totalSOHFromLots;
    }

    public long calculateSOHFromLots(Map<String, String> lotsOnHands) {
        long totalSOHFromLots;
        String quantityOnHand = lotsOnHands.get(Long.toString(this.getId()));
        if (quantityOnHand == null) {
            totalSOHFromLots = stockOnHand;
        } else {
            totalSOHFromLots = Integer.parseInt(quantityOnHand);
        }
        return totalSOHFromLots;
    }

    public List<LotOnHand> getLotOnHandListWrapper() {
        lotOnHandListWrapper = ListUtil.wrapOrEmpty(foreignLotOnHandList, lotOnHandListWrapper);
        return lotOnHandListWrapper;
    }

    public Date getEarliestLotExpiryDate() {
        List<LotOnHand> lotOnHandList = FluentIterable.from(getNonEmptyLotOnHandList()).filter(lotOnHand -> lotOnHand.getQuantityOnHand() > 0).toList();
        if (lotOnHandList.isEmpty()) {
            return null;
        }
        return Collections.min(lotOnHandList, (lhs, rhs) -> lhs.getLot().getExpirationDate().compareTo(rhs.getLot().getExpirationDate())).getLot().getExpirationDate();
    }

    public Date getLastStockMovementDate() {
        if (getStockMovementItemsWrapper().isEmpty()) {
            return null;
        }
        return Collections.max(getStockMovementItemsWrapper(), (lhs, rhs) -> lhs.getMovementDate().compareTo(rhs.getMovementDate())).getMovementDate();
    }

    public Date getLastStockMovementCreatedTime() {
        if (getStockMovementItemsWrapper().isEmpty()) {
            return null;
        }
        return Collections.max(getStockMovementItemsWrapper(), (lhs, rhs) -> lhs.getCreatedTime().compareTo(rhs.getCreatedTime())).getCreatedTime();
    }

    public List<LotOnHand> getNonEmptyLotOnHandList() {
        return FluentIterable.from(getLotOnHandListWrapper()).filter(lotOnHand -> lotOnHand.getQuantityOnHand() > 0).toList();
    }

    public List<LotOnHand> getNoneExpiredLotOnHandList() {
        DateTime now = new DateTime(DateUtil.getCurrentDate());
        return FluentIterable.from(getLotOnHandListWrapper())
                .filter(lotOnHand -> now.isBefore(lotOnHand.getLot().getExpirationDate().getTime()))
                .toList();
    }

    @Override
    public String toString() {
        return "[[product=" + product + ","
                + "stockOnHand=" + stockOnHand
                + "]"
                + super.toString()
                + "]";
    }
}
