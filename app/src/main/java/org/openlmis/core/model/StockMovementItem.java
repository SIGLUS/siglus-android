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

import static org.openlmis.core.utils.DateUtil.DATE_TIME_FORMAT_WITH_MS;
import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ListUtil;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

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

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
  StockCard stockCard;

  @DatabaseField
  long stockOnHand;

  @Expose
  @DatabaseField
  String signature;

  @Expose
  String expireDates;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  private java.util.Date movementDate;

  @DatabaseField
  private boolean synced = false;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DATE_TIME_FORMAT_WITH_MS)
  private java.util.Date createdTime;

  @ForeignCollectionField()
  private ForeignCollection<LotMovementItem> foreignLotMovementItems;

  private List<LotMovementItem> lotMovementItemListWrapper;
  private List<LotMovementItem> newAddedLotMovementItemListWrapper;

  public StockMovementItem(StockCard stockCard, InventoryViewModel model) {
    this.stockCard = stockCard;
    this.movementDate = DateUtil.getCurrentDate();
    this.reason = MovementReasonManager.INVENTORY;
    this.movementType = MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
    populateLotQuantitiesAndCalculateNewSOH(model.getNewLotMovementViewModelList());
  }

  public StockMovementItem(StockCard stockCard, InventoryViewModel model, boolean fromInitialInventory) {
    this.stockCard = stockCard;
    this.movementDate = DateUtil.getCurrentDate();
    this.reason = MovementReasonManager.INVENTORY;
    this.movementType = MovementReasonManager.MovementType.PHYSICAL_INVENTORY;

    List<LotMovementViewModel> movementViewModelList = new ArrayList<>();
    movementViewModelList.addAll(model.getExistingLotMovementViewModelList());
    movementViewModelList.addAll(model.getNewLotMovementViewModelList());
    populateLotQuantitiesAndCalculateNewSOH(
        fromInitialInventory ? movementViewModelList : model.getNewLotMovementViewModelList());
  }

  public StockMovementItem(StockCard stockCard) {
    this.stockCard = stockCard;
    this.stockOnHand = stockCard.calculateSOHFromLots();
    this.movementDate = DateUtil.getCurrentDate();
  }

  public boolean isPositiveMovement() {
    return MovementType.RECEIVE.equals(movementType)
        || MovementType.POSITIVE_ADJUST.equals(movementType)
        || MovementReasonManager.INVENTORY_POSITIVE.equals(reason);
  }

  public boolean isNegativeMovement() {
    return MovementType.ISSUE.equals(movementType)
        || MovementType.NEGATIVE_ADJUST.equals(movementType)
        || MovementReasonManager.INVENTORY_NEGATIVE.equals(reason);
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

  public List<LotMovementItem> getLotMovementItemListWrapper() {
    lotMovementItemListWrapper = ListUtil
        .wrapOrEmpty(foreignLotMovementItems, lotMovementItemListWrapper);
    return lotMovementItemListWrapper;
  }

  public List<LotMovementItem> getNewAddedLotMovementItemListWrapper() {
    newAddedLotMovementItemListWrapper = ListUtil
        .wrapOrEmpty(foreignLotMovementItems, newAddedLotMovementItemListWrapper);
    return newAddedLotMovementItemListWrapper;
  }

  public void populateLotQuantitiesAndCalculateNewSOH(List<LotMovementViewModel> lotMovementViewModelList) {
    final StockMovementItem stockMovementItem = this;
    if (!lotMovementViewModelList.isEmpty()) {
      long calculateMovementQuantity = 0;
      for (LotMovementViewModel lotMovementViewModel : lotMovementViewModelList) {
        if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
          calculateMovementQuantity += Long.parseLong(lotMovementViewModel.getQuantity());
        }
      }
      setMovementQuantity(calculateMovementQuantity);
      if (isNegativeMovement()) {
        setStockOnHand(getStockOnHand() - calculateMovementQuantity);
      } else {
        setStockOnHand(getStockOnHand() + calculateMovementQuantity);
      }
      setLotMovementItemListWrapper(FluentIterable.from(lotMovementViewModelList)
          .transform(lotMovementViewModel -> {
            LotMovementItem lotItem = lotMovementViewModel.convertViewToModel(getStockCard().getProduct());
            lotItem.setStockMovementItemAndUpdateMovementQuantity(stockMovementItem);
            return lotItem;
          }).toList());
    }
  }

  public void populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment(
      List<LotMovementViewModel> existingLotMovementList,
      List<LotMovementViewModel> newAddedLotMovementList) {
    final StockMovementItem stockMovementItem = this;
    ImmutableList<LotMovementItem> existingLotMovementItemList = FluentIterable
        .from(existingLotMovementList)
        .transform(lotMovementViewModel -> {
          LotMovementItem lotItem;
          if (lotMovementViewModel.getFrom() != null && lotMovementViewModel.getFrom()
              .equals(BulkEntriesActivity.KEY_FROM_BULK_ENTRIES_COMPLETE)) {
            lotItem = lotMovementViewModel.convertViewToModelAndResetSOHFromBulkEntries(getStockCard().getProduct());
          } else {
            lotItem = lotMovementViewModel.convertViewToModelAndResetSOH(getStockCard().getProduct());
          }
          lotItem.setStockMovementItem(stockMovementItem);
          lotItem.setStockOnHandReset(true);
          return lotItem;
        }).toList();

    ImmutableList<LotMovementItem> newAddedLotMovementItemList = FluentIterable
        .from(newAddedLotMovementList)
        .transform(lotMovementViewModel -> {
          lotMovementViewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
          LotMovementItem lotItem = lotMovementViewModel.convertViewToModel(getStockCard().getProduct());
          lotItem.setStockMovementItem(stockMovementItem);
          return lotItem;
        }).toList();
    setLotMovementItemListWrapper(existingLotMovementItemList);
    setNewAddedLotMovementItemListWrapper(newAddedLotMovementItemList);
  }

  public void buildLotMovementReasonAndDocumentNumber() {
    for (LotMovementItem lotMovementItem : getLotMovementItemListWrapper()) {
      lotMovementItem.setReason(reason);
      lotMovementItem.setDocumentNumber(documentNumber);
    }
    for (LotMovementItem lotMovementItem : getNewAddedLotMovementItemListWrapper()) {
      lotMovementItem.setReason(reason);
      lotMovementItem.setDocumentNumber(documentNumber);
    }
  }

  @Override
  public String toString() {
    return "[documentNumber=" + documentNumber
        + ",reason=" + reason
        + ",movementType=" + movementType
        + ",productCode=" + stockCard.getProduct().getCode()
        + ",stockOnHand=" + stockOnHand
        + ",movementQuantity=" + movementQuantity
        + ",stockCard.StockOnHand=" + stockCard.getStockOnHand()
        + ",stockCard.calculateSOHFromLots=" + stockCard.calculateSOHFromLots()
        + "]";
  }
}
