package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
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

    public PhysicalInventoryViewModel(StockCard stockCard) {
        super(stockCard);
    }

    @Override
    public boolean validate() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            valid = !checked || (validateLotList() && validateExistingLot()) || product.isArchived();
        } else {
            valid = !checked || StringUtils.isNumeric(quantity) || product.isArchived();
        }
        return valid;
    }

    @Override
    public boolean isDataChanged() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            if (draftInventory == null) {
                return hasLotInInventoryModelChanged();
            }
            return !draftInventory.getDraftLotItemListWrapper().isEmpty() && isDifferentFromDraft();
        }
        return isDataChanged;
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
        for (DraftLotItem draftLotItem: existingDraftLotItems) {
            for (LotMovementViewModel existingLotMovementViewModel: existingLotMovementViewModelList) {
                if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
                    if (!String.valueOf(draftLotItem.getQuantity() == null ? "" : draftLotItem.getQuantity()).equals(existingLotMovementViewModel.getQuantity())) {
                        return true;
                    }
                }
            }
        }
        for (DraftLotItem draftLotItem: newAddedDraftLotItems) {
            if (newAddedDraftLotItems.size() != lotMovementViewModelList.size()) {
                return true;
            }
            for (LotMovementViewModel lotMovementViewModel: lotMovementViewModelList) {
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
        if (lotMovementViewModelList.size() > 0) return true;
        for (LotMovementViewModel viewModel : getLotMovementViewModelList()) {
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
