package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.StockCard;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

import lombok.Data;

@Data
public class PhysicalInventoryViewModel extends InventoryViewModel {
    private DraftInventory draftInventory;
    private boolean done;

    public PhysicalInventoryViewModel(StockCard stockCard) {
        super(stockCard);
    }

    @Override
    public boolean validate() {
        valid = !checked || (validateLotList() && validateExistingLot()) || product.isArchived();
        return valid;
    }

    @Override
    public boolean isDataChanged() {
        if (draftInventory == null) {
            return hasLotInInventoryModelChanged();
        }
        return !draftInventory.getDraftLotItemListWrapper().isEmpty() && isDifferentFromDraft();
    }

    private boolean isDifferentFromDraft() {
        List<DraftLotItem> newAddedDraftLotItems = FluentIterable.from(draftInventory.getDraftLotItemListWrapper()).filter(new Predicate<DraftLotItem>() {
            @Override
            public boolean apply(DraftLotItem draftLotItem) {
                return draftLotItem.isNewAdded();
            }
        }).toList();
        List<DraftLotItem> existingDraftLotItems = FluentIterable.from(draftInventory.getDraftLotItemListWrapper()).filter(new Predicate<DraftLotItem>() {
            @Override
            public boolean apply(DraftLotItem draftLotItem) {
                return !draftLotItem.isNewAdded();
            }
        }).toList();
        for (DraftLotItem draftLotItem : existingDraftLotItems) {
            for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
                if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
                    if (!String.valueOf(draftLotItem.getQuantity() == null ? "" : draftLotItem.getQuantity()).equals(existingLotMovementViewModel.getQuantity())) {
                        return true;
                    }
                }
            }
        }
        for (DraftLotItem draftLotItem : newAddedDraftLotItems) {
            if (newAddedDraftLotItems.size() != newLotMovementViewModelList.size()) {
                return true;
            }
            for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
                if (draftLotItem.getLotNumber().equals(lotMovementViewModel.getLotNumber())) {
                    if (!String.valueOf(draftLotItem.getQuantity() == null ? "" : draftLotItem.getQuantity()).equals(lotMovementViewModel.getQuantity())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasLotInInventoryModelChanged() {
        for (LotMovementViewModel viewModel : getExistingLotMovementViewModelList()) {
            if (viewModel.getQuantity() != null && !viewModel.getQuantity().isEmpty()) {
                return true;
            }
        }
        if (newLotMovementViewModelList.size() > 0) return true;
        for (LotMovementViewModel viewModel : getNewLotMovementViewModelList()) {
            if (viewModel.getQuantity() != null && !viewModel.getQuantity().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean validateExistingLot() {
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (!lotMovementViewModel.validateLotWithNoEmptyFields()) {
                return false;
            }
        }
        return true;
    }
}
