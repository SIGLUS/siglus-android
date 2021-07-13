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
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.utils.ListUtil;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Getter
@Setter
@DatabaseTable(tableName = "draft_inventory")
@NoArgsConstructor
public class DraftInventory extends BaseModel {

  /**
   * @deprecated expireDate move to lot field, use {@link DraftLotItem#expirationDate}
   */
  @Deprecated
  @DatabaseField
  String expireDates;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  StockCard stockCard;

  @DatabaseField(defaultValue = "false")
  boolean done;

  @DatabaseField
  Long quantity;

  @ForeignCollectionField()
  private ForeignCollection<DraftLotItem> foreignDraftLotItems;

  private List<DraftLotItem> draftLotItemListWrapper;

  public DraftInventory(PhysicalInventoryViewModel viewModel) {
    this.stockCard = viewModel.getStockCard();
    this.quantity = viewModel.getLotListQuantityTotalAmount();
    done = viewModel.isDone();
    setupDraftLotList(viewModel.getExistingLotMovementViewModelList(),
        viewModel.getNewLotMovementViewModelList());
  }

  public List<DraftLotItem> getDraftLotItemListWrapper() {
    draftLotItemListWrapper = ListUtil.wrapOrEmpty(foreignDraftLotItems, draftLotItemListWrapper);
    return draftLotItemListWrapper;
  }

  private void setupDraftLotList(List<LotMovementViewModel> existingLotMovementViewModelList,
      List<LotMovementViewModel> lotMovementViewModelList) {
    getDraftLotItemListWrapper().addAll(
        FluentIterable.from(existingLotMovementViewModelList).transform(lotMovementViewModel -> {
          DraftLotItem draftLotItem = new DraftLotItem(lotMovementViewModel, stockCard.getProduct(),
              false);
          draftLotItem.setDraftInventory(DraftInventory.this);
          return draftLotItem;
        }).toList());
    getDraftLotItemListWrapper()
        .addAll(FluentIterable.from(lotMovementViewModelList).transform(lotMovementViewModel -> {
          DraftLotItem draftLotItem = new DraftLotItem(lotMovementViewModel, stockCard.getProduct(),
              true);
          draftLotItem.setDraftInventory(DraftInventory.this);
          return draftLotItem;
        }).toList());
  }
}
