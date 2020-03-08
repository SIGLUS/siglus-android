package org.openlmis.core.view.viewmodel;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

import lombok.Data;

@Data
public class BulkInitialInventoryViewModel extends InventoryViewModel {
    private static final String TAG = BulkInitialInventoryViewModel.class.getSimpleName();

    private DraftInitialInventory draftInventory;
    private boolean done;

    public BulkInitialInventoryViewModel(StockCard stockCard) {
        super(stockCard);
    }
    public BulkInitialInventoryViewModel(Product product) {
        super(product);
    }

    @Override
    public boolean validate() {
        Log.e(TAG,"validate done = " +done);
//        valid = !checked || (validateNewLotList() && validateExistingLot()) || product.isArchived();
//        done = valid;
        return done;
    }

    @Override
    public boolean isDataChanged() {
        if (draftInventory == null) {
            return hasLotInInventoryModelChanged();
        }
        return !draftInventory.getDraftLotItemListWrapper().isEmpty() && isDifferentFromDraft();
    }

    private boolean isDifferentFromDraft() {
        List<DraftInitialInventoryLotItem> newAddedDraftLotItems = FluentIterable.from(draftInventory.getDraftLotItemListWrapper()).filter(draftLotItem -> draftLotItem.isNewAdded()).toList();
        List<DraftInitialInventoryLotItem> existingDraftLotItems = FluentIterable.from(draftInventory.getDraftLotItemListWrapper()).filter(draftLotItem -> !draftLotItem.isNewAdded()).toList();
        for (DraftInitialInventoryLotItem draftLotItem : existingDraftLotItems) {
            for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
                if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
                    if (!String.valueOf(draftLotItem.getQuantity() == null ? "" : draftLotItem.getQuantity()).equals(existingLotMovementViewModel.getQuantity())) {
                        return true;
                    }
                }
            }
        }
        for (DraftInitialInventoryLotItem draftLotItem : newAddedDraftLotItems) {
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

    public String getFormattedProductName() {
        return product.getFormattedProductNameWithoutStrengthAndType();
    }

    public String getFormattedProductUnit() {
        return product.getStrength() + " " + product.getType();
    }

    public SpannableStringBuilder getGreenName() {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getFormattedProductName());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(LMISApp.getInstance().getResources().getColor(R.color.color_primary)), 0, getFormattedProductName().length(), Spanned.SPAN_POINT_MARK);
        return spannableStringBuilder;
    }

    public SpannableStringBuilder getGreenUnit() {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getFormattedProductUnit());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(LMISApp.getInstance().getResources().getColor(R.color.color_primary)), 0, getFormattedProductUnit().length(), Spanned.SPAN_POINT_MARK);
        return spannableStringBuilder;
    }

    public void setInitialDraftInventory(DraftInitialInventory draftInventory) {
        this.draftInventory = draftInventory;
        done = draftInventory.isDone();
        populateLotMovementModelWithDraftLotItem();
    }

    private void populateLotMovementModelWithDraftLotItem() {
        for (DraftInitialInventoryLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
            if (draftLotItem.isNewAdded()) {
                if (isNotInExistingLots(draftLotItem)) {
                    LotMovementViewModel newLotMovementViewModel = new LotMovementViewModel();
                    newLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
                    newLotMovementViewModel.setLotNumber(draftLotItem.getLotNumber());
                    newLotMovementViewModel.setExpiryDate(DateUtil.formatDate(draftLotItem.getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                    getNewLotMovementViewModelList().add(newLotMovementViewModel);
                }
            } else {
                for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
                    if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
                        existingLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
                    }
                }
            }
        }
    }

    private boolean isNotInExistingLots(DraftInitialInventoryLotItem draftLotItem) {
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (draftLotItem.getLotNumber().toUpperCase().equals(lotMovementViewModel.getLotNumber().toUpperCase())) {
                return false;
            }
        }

        for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
            if (draftLotItem.getLotNumber().toUpperCase().equals(lotMovementViewModel.getLotNumber().toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    private String formatQuantity(Long quantity) {
        return quantity == null ? "" : quantity.toString();
    }
}
