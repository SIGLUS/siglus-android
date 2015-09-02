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
import android.text.style.ForegroundColorSpan;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import java.util.List;

import lombok.Data;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Data
public class StockCardViewModel {
    String productName;
    String fnm;
    String strength;
    String type;

    String quantity;

    List<String> expiry_dates;

    long stockCardId;
    long stockOnHand;

    SpannableStringBuilder styledName;
    SpannableStringBuilder styledUnit;

    public StockCardViewModel(StockCard stockCard){
        this.productName = stockCard.getProduct().getPrimaryName();
        this.fnm = stockCard.getProduct().getCode();
        this.strength = stockCard.getProduct().getStrength();
        this.stockCardId = stockCard.getId();

        Product product = stockCard.getProduct();
        formatProductDisplay(product);

        this.expiry_dates = newArrayList(stockCard.getExpireDates().split(StockCard.DIVIDER));
        this.stockOnHand = stockCard.getStockOnHand();
    }

    private void formatProductDisplay(Product product) {
        String productName = product.getPrimaryName() + " [" + product.getCode() + "]";
        styledName = new SpannableStringBuilder(productName);
        styledName.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.secondary_text)),
                product.getPrimaryName().length(), productName.length(), Spannable.SPAN_POINT_MARK);

        String unit = product.getStrength() + " " + product.getType();
        styledUnit = new SpannableStringBuilder(unit);
        int length = 0;
        if (product.getStrength() != null) {
            length = product.getStrength().length();
        }
        styledUnit.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.secondary_text)),
                length, unit.length(), Spannable.SPAN_POINT_MARK);
    }


    public boolean validate(){
        return StringUtils.isNumeric(quantity);
    }
}
