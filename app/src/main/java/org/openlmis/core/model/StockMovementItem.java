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


import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ListUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "stock_items")
public class StockMovementItem extends BaseModel {

    @Expose
    @DatabaseField
    String documentNumber;

    @Expose
    @DatabaseField
    long movementQuantity;

    @Expose
    @DatabaseField
    Long requested;

    @DatabaseField
    String reason;

    @DatabaseField
    MovementReasonManager.MovementType movementType;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    StockCard stockCard;

    @DatabaseField
    long stockOnHand;

    @Expose
    @DatabaseField
    String signature;

    @Expose
    String expireDates;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DB_DATE_FORMAT)
    private java.util.Date movementDate;

    @DatabaseField
    private boolean synced = false;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DATE_TIME_FORMAT)
    private java.util.Date createdTime;

    @ForeignCollectionField()
    private ForeignCollection<LotMovementItem> foreignLotMovementItems;

    private List<LotMovementItem> lotMovementItemListWrapper;

    public StockMovementItem(StockCard stockCard, InventoryViewModel model) {
        this.stockCard = stockCard;
        this.movementDate = new Date();
        this.reason = MovementReasonManager.INVENTORY;
        this.movementType = MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
        populateLotQuantitiesAndCalculateNewSOH(model.getLotMovementViewModelList(), movementType);
    }

    public boolean isPositiveMovement() {
        return movementType.equals(MovementReasonManager.MovementType.RECEIVE) || movementType.equals(MovementReasonManager.MovementType.POSITIVE_ADJUST);
    }

    public boolean isNegativeMovement() {
        return movementType.equals(MovementReasonManager.MovementType.ISSUE) || movementType.equals(MovementReasonManager.MovementType.NEGATIVE_ADJUST);
    }

    public Period getMovementPeriod() {
        return Period.of(movementDate);
    }

    public long calculatePreviousSOH() {
        if (isNegativeMovement()) {
            return stockOnHand + movementQuantity;
        } else if (isPositiveMovement()) {
            return stockOnHand - movementQuantity;
        } else {
            return stockOnHand;
        }
    }

    public StockMovementItem(StockCard stockCard) {
        this.stockCard = stockCard;
        this.stockOnHand = stockCard.getStockOnHand();
        this.movementDate = new Date();
    }

    public List<LotMovementItem> getLotMovementItemListWrapper() {
        lotMovementItemListWrapper = ListUtil.wrapOrEmpty(foreignLotMovementItems, lotMovementItemListWrapper);
        return lotMovementItemListWrapper;
    }

    public void populateLotQuantitiesAndCalculateNewSOH(List<LotMovementViewModel> lotMovementViewModelList, MovementReasonManager.MovementType movementType) {
        final StockMovementItem stockMovementItem = this;
        if (!lotMovementViewModelList.isEmpty()) {
            long movementQuantity = 0;

            setLotMovementItemListWrapper(FluentIterable.from(lotMovementViewModelList).transform(new Function<LotMovementViewModel, LotMovementItem>() {
                @Override
                public LotMovementItem apply(LotMovementViewModel lotMovementViewModel) {
                    LotMovementItem lotItem = lotMovementViewModel.convertViewToModel(getStockCard().getProduct());
                    lotItem.setStockMovementItem(stockMovementItem);
                    return lotItem;
                }
            }).toList());

            for (LotMovementViewModel lotMovementViewModel: lotMovementViewModelList) {
                movementQuantity += Long.parseLong(lotMovementViewModel.getQuantity());
            }
            setMovementQuantity(movementQuantity);
            if (movementType.equals(MovementReasonManager.MovementType.ISSUE) || movementType.equals(MovementReasonManager.MovementType.NEGATIVE_ADJUST)) {
                setStockOnHand(getStockOnHand() - movementQuantity);
            } else {
                setStockOnHand(getStockOnHand() + movementQuantity);
            }
        }
    }
}
