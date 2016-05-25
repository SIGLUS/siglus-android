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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.holder.StockCardViewHolder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    long stockCardId;

    long stockOnHand;

    long kitExpectQuantity;
    SpannableStringBuilder styledName;

    int lowStockAvg;

    int cmm;

    SpannableStringBuilder styledUnit;

    boolean valid = true;

    private boolean checked = false;

    private String signature;
    private StockCard stockCard;
    protected Product product;

    public InventoryViewModel(StockCard stockCard) {
        this(stockCard.getProduct());

        this.stockCard = stockCard;
        this.stockCardId = stockCard.getId();
        this.stockOnHand = stockCard.getStockOnHand();
        this.checked = true;
        this.lowStockAvg = stockCard.getLowStockAvg();
        this.cmm = stockCard.getCMM();

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

    private void sortByDate() {
        Collections.sort(expiryDates, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return DateUtil.parseString(lhs, DateUtil.SIMPLE_DATE_FORMAT).compareTo(DateUtil.parseString(rhs, DateUtil.SIMPLE_DATE_FORMAT));
            }
        });
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

    public boolean validate() {
        valid = !checked || StringUtils.isNumeric(quantity) || product.isArchived();
        return valid;
    }

    public DraftInventory parseDraftInventory() {
        DraftInventory draftInventory = new DraftInventory();
        draftInventory.setExpireDates(DateUtil.formatExpiryDateString(expiryDates));

        Long quantity;
        try {
            quantity = Long.parseLong(getQuantity());
        } catch (NumberFormatException e) {
            e.printStackTrace();//todo: ???
            quantity = null;
        }
        draftInventory.setQuantity(quantity);

        draftInventory.setStockCard(stockCard);
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

        if (cmm < 0) {
            return StockCardViewHolder.STOCK_ON_HAND_NORMAL;
        } else {
            if (stockOnHand > 2 * cmm) {
                return StockCardViewHolder.STOCK_ON_HAND_OVER_STOCK;
            } else if(stockOnHand > lowStockAvg) {
                return StockCardViewHolder.STOCK_ON_HAND_NORMAL;
            } else {
                return StockCardViewHolder.STOCK_ON_HAND_LOW_STOCK;
            }
        }
    }
}
