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

package org.openlmis.core.view.viewmodel;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Data
public class InventoryViewModel {

    long productId;
    String productName;
    String fnm;

    String strength;
    String type;
    String quantity;
    boolean hasDataChanged;
    List<String> expiryDates = new ArrayList<>();

    List<LotMovementViewModel> lotMovementViewModelList = new ArrayList<>();
    List<LotMovementViewModel> existingLotMovementViewModelList = new ArrayList<>();

    long stockCardId;

    long stockOnHand;

    long kitExpectQuantity;
    SpannableStringBuilder styledName;

    SpannableStringBuilder styledUnit;

    boolean valid = true;

    private boolean checked = false;

    private String signature;
    private StockCard stockCard;
    protected Product product;
    boolean shouldShowEmptyLotWarning = false;
    boolean hasConfirmedNoStockReceived = false;
    private DraftInventory draftInventory = null;

    public InventoryViewModel(StockCard stockCard) {
        this(stockCard.getProduct());

        this.stockCard = stockCard;
        this.stockCardId = stockCard.getId();
        this.stockOnHand = stockCard.getStockOnHand();
        this.checked = true;

        initExpiryDates(stockCard.getExpireDates());
    }

    public InventoryViewModel(Product product) {
        this.product = product;
        this.type = product.getType();

        setProductAttributes(product);
        formatProductDisplay(product);
    }


    public void initExpiryDates(String expireDates) {
        if (!TextUtils.isEmpty(expireDates)) {
            this.expiryDates = newArrayList(expireDates.split(StockCard.DIVIDER));
        } else {
            this.expiryDates = new ArrayList<>();
        }
    }

    public SpannableStringBuilder getStyledName() {
        formatProductDisplay(product);
        return styledName;
    }

    public SpannableStringBuilder getStyleType() {
        if (type != null) {
            return new SpannableStringBuilder(type);
        } else {
            return new SpannableStringBuilder("Other"); //arbitrary default type in case server product form is null caused by human error
        }
    }

    public SpannableStringBuilder getStyledUnit() {
        formatProductDisplay(product);
        return styledUnit;
    }

    public void setExpiryDates(List<String> expireDates) {
        this.expiryDates = expireDates;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void clearExpiryDates() {
        this.expiryDates = new ArrayList<>();
    }

    public void clearLotMovementViewModelList() {
        this.lotMovementViewModelList.clear();
    }

    private void setProductAttributes(Product product) {
        this.productName = product.getPrimaryName();
        this.fnm = product.getCode();
        this.strength = product.getStrength();
        this.productId = product.getId();
    }

    private void formatProductDisplay(Product product) {
        String productName = product.getFormattedProductName();
        styledName = new SpannableStringBuilder(productName);
        styledName.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.color_text_secondary)),
                product.getPrimaryName().length(), productName.length(), Spannable.SPAN_POINT_MARK);

        String unit = product.getStrength() + " " + product.getType();
        styledUnit = new SpannableStringBuilder(unit);
        int length = 0;
        if (product.getStrength() != null) {
            length = product.getStrength().length();
        }
        styledUnit.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.color_text_secondary)),
                length, unit.length(), Spannable.SPAN_POINT_MARK);
    }

    public String optFirstExpiryDate() {
        if (expiryDates != null && expiryDates.size() > 0) {
            try {
                return DateUtil.convertDate(expiryDates.get(0), DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
            } catch (ParseException e) {
                new LMISException(e).reportToFabric();
                return StringUtils.EMPTY;
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

    public boolean addExpiryDate(String date) {
        return addExpiryDate(date, true);
    }

    public boolean addExpiryDate(String date, boolean append) {
        if (expiryDates == null) {
            expiryDates = new ArrayList<>();
        }
        if (!append) {
            expiryDates.clear();
        }
        return !isExpireDateExists(date) && expiryDates.add(date);
    }

    public void removeExpiryDate(String date) {
        if (expiryDates != null) {
            expiryDates.remove(date);
        }
    }

    public boolean isExpireDateExists(String expireDate) {
        return this.getExpiryDates().contains(expireDate);
    }

    public boolean validate(boolean archivedProductMandatoryQuantity) {
        if (archivedProductMandatoryQuantity) {
            valid = !checked || StringUtils.isNumeric(quantity);
        } else {
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                valid = !checked || validateLotList() || product.isArchived();
            } else {
                valid = !checked || StringUtils.isNumeric(quantity) || product.isArchived();
            }
        }
        return valid;
    }

    public boolean validatePhysical() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            valid = !checked || (validateLotList() && validateExistingLot()) || product.isArchived();
        } else {
            valid = !checked || StringUtils.isNumeric(quantity) || product.isArchived();
        }
        return valid;
    }

    private boolean validateExistingLot() {
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (!lotMovementViewModel.validateExistingLot()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateLotList() {
        for (LotMovementViewModel lotMovementViewModel : lotMovementViewModelList) {
            if (!lotMovementViewModel.validate()) {
                return false;
            }
        }
        return true;
    }

    public DraftInventory parseDraftInventory() {
        final DraftInventory draftInventory = new DraftInventory(stockCard);
        draftInventory.setExpireDates(DateUtil.formatExpiryDateString(expiryDates));

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            long quantity = getLotListQuantityTotalAmount();
            draftInventory.setQuantity(quantity);
            draftInventory.setupDraftLotList(getExistingLotMovementViewModelList(), getLotMovementViewModelList());
        } else {
            Long quantity;
            try {
                quantity = Long.parseLong(getQuantity());
            } catch (NumberFormatException e) {
                e.printStackTrace();//todo: ???
                quantity = null;
            }
            draftInventory.setQuantity(quantity);
        }

        return draftInventory;
    }

    public static InventoryViewModel buildEmergencyModel(StockCard stockCard) {
        InventoryViewModel viewModel = new InventoryViewModel(stockCard.getProduct());
        viewModel.stockCard = stockCard;
        return viewModel;
    }

    public int getStockOnHandLevel() {

        if (stockOnHand == 0) {
            return StockCardViewHolder.STOCK_ON_HAND_STOCK_OUT;
        }

        if (stockCard.getCMM() < 0) {
            return StockCardViewHolder.STOCK_ON_HAND_NORMAL;
        } else {
            if (stockCard.isOverStock()) {
                return StockCardViewHolder.STOCK_ON_HAND_OVER_STOCK;
            }
            if (stockCard.isLowStock()) {
                return StockCardViewHolder.STOCK_ON_HAND_LOW_STOCK;
            }
            return StockCardViewHolder.STOCK_ON_HAND_NORMAL;
        }
    }

    public void addLotMovementViewModel(LotMovementViewModel lotMovementViewModel) {
        lotMovementViewModelList.add(lotMovementViewModel);
    }

    public Long getLotListQuantityTotalAmount() {
        long lotTotalQuantity = 0L;
        if (!lotMovementViewModelList.isEmpty()) {
            for (LotMovementViewModel lotMovementViewModel: lotMovementViewModelList) {
                if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                    lotTotalQuantity += Long.parseLong(lotMovementViewModel.getQuantity());
                }
            }
        }
        if (!existingLotMovementViewModelList.isEmpty()) {
            for (LotMovementViewModel lotMovementViewModel: existingLotMovementViewModelList) {
                if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                    lotTotalQuantity += Long.parseLong(lotMovementViewModel.getQuantity());
                }
            }
        }
        return lotTotalQuantity;
    }

    public boolean hasConfirmedNoStockReceived() {
        return this.hasConfirmedNoStockReceived;
    }

    public boolean shouldShowEmptyLotWarning() {
        return this.shouldShowEmptyLotWarning;
    }

    public boolean hasLotChanged() {
        for (LotMovementViewModel lotMovementViewModel: lotMovementViewModelList) {
            if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                return true;
            }
        }
        for (LotMovementViewModel lotMovementViewModel: existingLotMovementViewModelList) {
            if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasDataChanged() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            if (draftInventory == null) {
                return hasLotInInventoryModelChanged();
            }
            return !draftInventory.getDraftLotItemListWrapper().isEmpty() && isDifferentFromDraft();
        }
        return hasDataChanged;
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
        for (LotMovementViewModel viewModel : existingLotMovementViewModelList) {
            if (!viewModel.getQuantity().isEmpty()) {
                return true;
            }
        }
        if (lotMovementViewModelList.size() > 0) return true;
        for (LotMovementViewModel viewModel : lotMovementViewModelList) {
            if (!viewModel.getQuantity().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
