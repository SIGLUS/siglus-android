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

import android.text.SpannableStringBuilder;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.holder.StockCardViewHolder;

import lombok.Data;
import lombok.Setter;

@Data
public class InventoryViewModel extends BaseStockMovementViewModel {

    long productId;
    String productName;
    String fnm;

    String strength;
    String type;

    boolean isDataChanged;

    long stockCardId;

    long stockOnHand;

    long kitExpectQuantity;
    SpannableStringBuilder styledName;

    SpannableStringBuilder styledUnit;

    boolean valid = true;

    boolean checked = false;

    boolean dummyModel = false;

    boolean isBasic = false;

    @Setter
    private int viewType;

    private String signature;
    StockCard stockCard;

    public InventoryViewModel(StockCard stockCard) {
        this(stockCard.getProduct());

        this.stockCard = stockCard;
        this.stockCardId = stockCard.getId();
        this.stockOnHand = stockCard.getStockOnHand();
        this.checked = true;
    }

    public InventoryViewModel(Product product) {
        this.product = product;
        this.type = product.getType();
        this.isBasic = product.isBasic();
        setProductAttributes(product);
    }

    public SpannableStringBuilder getStyledName() {
        if (styledName == null) {
            styledName = TextStyleUtil.formatStyledProductName(product);
        }
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
        if (styledUnit == null) {
            styledUnit = TextStyleUtil.formatStyledProductUnit(product);
        }
        return styledUnit;
    }

    private void setProductAttributes(Product product) {
        this.productName = product.getPrimaryName();
        this.fnm = product.getCode();
        this.strength = product.getStrength();
        this.productId = product.getId();
    }

    public boolean validate() {
        valid = !checked || validateNewLotList() || product.isArchived();
        return valid;
    }

    boolean validateNewLotList() {
        for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
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
        }

        if (stockCard.isLowStock()) {
            return StockCardViewHolder.STOCK_ON_HAND_LOW_STOCK;
        } else if (stockCard.isOverStock()) {
            return StockCardViewHolder.STOCK_ON_HAND_OVER_STOCK;
        }
        return StockCardViewHolder.STOCK_ON_HAND_NORMAL;
    }

    public Long getLotListQuantityTotalAmount() {
        long lotTotalQuantity = 0L;
        for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
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

    public String getFormattedProductName() {
        return product.getFormattedProductNameWithoutStrengthAndType();
    }

    public String getFormattedProductUnit() {
        return product.getStrength() + " " + product.getType();
    }

    @Override
    public boolean equals(Object object) {
        return this.productId == ((InventoryViewModel) object).productId && this.productName.equals(((InventoryViewModel) object).productName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (productId ^ (productId >>> 32));
        result = 31 * result + (productName != null ? productName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InventoryViewModel{"
                + "productId="
                + productId
                + ", productName='" + productName + '\''
                + ", fnm='" + fnm + '\''
                + ", strength='" + strength + '\''
                + ", type='" + type + '\''
                + ", isDataChanged=" + isDataChanged
                + ", stockCardId=" + stockCardId
                + ", stockOnHand=" + stockOnHand
                + ", kitExpectQuantity=" + kitExpectQuantity
                + ", styledName=" + styledName
                + ", styledUnit=" + styledUnit
                + ", valid=" + valid
                + ", checked=" + checked
                + ", signature='" + signature + '\''
                + ", stockCard=" + stockCard
                + ", new movement=" + getNewMovementString()
                + '}';
    }

    private String getNewMovementString() {
        String list = "";
        for (LotMovementViewModel model : newLotMovementViewModelList) {
            list += " " + model.toString();
        }
        return list;
    }
}
