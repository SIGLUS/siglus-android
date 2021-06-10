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
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Getter
@Setter
@DatabaseTable(tableName = "draft_initial_inventory")
@NoArgsConstructor
public class DraftInitialInventory extends BaseModel {

  private static final String TAG = DraftInitialInventory.class.getSimpleName();

  @Deprecated
  @DatabaseField
  String expireDates;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField(defaultValue = "false")
  boolean done;

  @DatabaseField
  Long quantity;

  @ForeignCollectionField()
  private ForeignCollection<DraftInitialInventoryLotItem> foreignDraftLotItems;

  private List<DraftInitialInventoryLotItem> draftLotItemListWrapper;

  public DraftInitialInventory(BulkInitialInventoryViewModel viewModel) {
    this.product = viewModel.getProduct();
    this.quantity = viewModel.getLotListQuantityTotalAmount();
    done = viewModel.isDone();
    setupDraftLotList(viewModel.getExistingLotMovementViewModelList(),
        viewModel.getNewLotMovementViewModelList());
  }

  public List<DraftInitialInventoryLotItem> getDraftLotItemListWrapper() {
    draftLotItemListWrapper = ListUtil.wrapOrEmpty(foreignDraftLotItems, draftLotItemListWrapper);
    return draftLotItemListWrapper;
  }

  private void setupDraftLotList(List<LotMovementViewModel> existingLotMovementViewModelList,
      List<LotMovementViewModel> lotMovementViewModelList) {
    getDraftLotItemListWrapper()
        .addAll(FluentIterable.from(existingLotMovementViewModelList)
            .transform(lotMovementViewModel -> {
              DraftInitialInventoryLotItem draftLotItem = new DraftInitialInventoryLotItem(
                  lotMovementViewModel,
                  product);
              draftLotItem.setDraftInitialInventory(DraftInitialInventory.this);
              return draftLotItem;
            }).toList());
    getDraftLotItemListWrapper()
        .addAll(FluentIterable.from(lotMovementViewModelList)
            .transform(lotMovementViewModel -> {
              DraftInitialInventoryLotItem draftLotItem = new DraftInitialInventoryLotItem(
                  lotMovementViewModel,
                  product);
              draftLotItem.setDraftInitialInventory(DraftInitialInventory.this);
              return draftLotItem;
            }).toList());
  }
}