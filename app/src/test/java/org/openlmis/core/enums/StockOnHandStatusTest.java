/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */

package org.openlmis.core.enums;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import static org.junit.Assert.*;

public class StockOnHandStatusTest {

    @Test
    public void calculateStockOnHandLevel() {
        // given
        final Product product = new Product();
        product.setHiv(false);
        final StockCard regularStockCard = new StockCard();
        regularStockCard.setProduct(product);
        regularStockCard.setStockOnHand(100);
        regularStockCard.setAvgMonthlyConsumption(-1);

        final StockCard overStockCard = new StockCard();
        overStockCard.setProduct(product);
        overStockCard.setStockOnHand(201);
        overStockCard.setAvgMonthlyConsumption(100);

        final StockCard outStockCard = new StockCard();
        outStockCard.setProduct(product);
        outStockCard.setStockOnHand(0);
        outStockCard.setAvgMonthlyConsumption(-1);

        final StockCard lowStockCard = new StockCard();
        lowStockCard.setProduct(product);
        lowStockCard.setStockOnHand(100);
        lowStockCard.setAvgMonthlyConsumption(101);

        // when
        regularStockCard.setStockOnHandStatus(StockOnHandStatus.calculateStockOnHandLevel(regularStockCard));
        overStockCard.setStockOnHandStatus(StockOnHandStatus.calculateStockOnHandLevel(overStockCard));
        outStockCard.setStockOnHandStatus(StockOnHandStatus.calculateStockOnHandLevel(outStockCard));
        lowStockCard.setStockOnHandStatus(StockOnHandStatus.calculateStockOnHandLevel(lowStockCard));

        // then
        MatcherAssert.assertThat(regularStockCard.getStockOnHandStatus(), Matchers.is(StockOnHandStatus.REGULAR_STOCK));
        MatcherAssert.assertThat(overStockCard.getStockOnHandStatus(), Matchers.is(StockOnHandStatus.OVER_STOCK));
        MatcherAssert.assertThat(outStockCard.getStockOnHandStatus(), Matchers.is(StockOnHandStatus.STOCK_OUT));
        MatcherAssert.assertThat(lowStockCard.getStockOnHandStatus(), Matchers.is(StockOnHandStatus.LOW_STOCK));
    }
}