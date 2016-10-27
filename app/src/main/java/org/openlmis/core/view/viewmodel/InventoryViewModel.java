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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.holder.StockCardViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;

@Data
public class InventoryViewModel {

    long productId;
    String productName;
    String fnm;

    String strength;
    String type;
    String quantity;
    boolean isDataChanged;
    final List<String> expiryDates = new ArrayList<>();

    List<LotMovementViewModel> lotMovementViewModelList = new ArrayList<>();
    List<LotMovementViewModel> existingLotMovementViewModelList = new ArrayList<>();

    long stockCardId;

    long stockOnHand;

    long kitExpectQuantity;
    SpannableStringBuilder styledName;

    SpannableStringBuilder styledUnit;

    boolean valid = true;

    boolean checked = false;

    private String signature;
    StockCard stockCard;
    protected Product product;

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
            this.expiryDates.clear();
            this.expiryDates.addAll(Arrays.asList(expireDates.split(StockCard.DIVIDER)));
        } else {
            this.expiryDates.clear();
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

    private void setProductAttributes(Product product) {
        this.productName = product.getPrimaryName();
        this.fnm = product.getCode();
        this.strength = product.getStrength();
        this.productId = product.getId();
    }

    private void formatProductDisplay(Product product) {
        String productName = product.getFormattedProductNameWithoutStrengthAndType();
        styledName = new SpannableStringBuilder(productName);
        styledName.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.color_text_secondary)),
                product.getProductNameWithoutStrengthAndType().length(), productName.length(), Spannable.SPAN_POINT_MARK);

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
        try {
            return DateUtil.convertDate(expiryDates.get(0), DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
        } catch (Exception e) {
            new LMISException(e).reportToFabric();
            return StringUtils.EMPTY;
        }
    }

    public boolean validate() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            valid = !checked || validateLotList() || product.isArchived();
        } else {
            valid = !checked || StringUtils.isNumeric(quantity) || product.isArchived();
        }
        return valid;
    }

    boolean validateLotList() {
        for (LotMovementViewModel lotMovementViewModel : lotMovementViewModelList) {
            if (!lotMovementViewModel.validateLotWithPositiveQuantity()) {
                return false;
            }
        }
        return true;
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
        for (LotMovementViewModel lotMovementViewModel : lotMovementViewModelList) {
            if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                lotTotalQuantity += Long.parseLong(lotMovementViewModel.getQuantity());
            }
        }
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                lotTotalQuantity += Long.parseLong(lotMovementViewModel.getQuantity());
            }
        }
        return lotTotalQuantity;
    }
}
