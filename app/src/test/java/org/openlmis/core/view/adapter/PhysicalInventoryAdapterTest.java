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

package org.openlmis.core.view.adapter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryAdapterTest {

    PhysicalInventoryAdapter adapter;
    Product product;
    StockCard stockCard;

    @Before
    public void setup() {
        adapter = new PhysicalInventoryAdapter(new ArrayList<StockCardViewModel>());

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        stockCard = new StockCard();
        stockCard.setExpireDates(StringUtils.EMPTY);
        stockCard.setProduct(product);
    }

    @Test
    public void shouldReturnFirstInvalidItemPosition() {
        List<StockCardViewModel> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockCardViewModel model = new StockCardViewModel(stockCard);
            list.add(model);

            if (i == 5) {
                continue;
            }
            model.setQuantity(i + "");
        }

        adapter.refreshList(list);
        assertThat(adapter.validateAll(), is(5));
    }

    @Test
    public void shouldFilterTheListByProductName() {
        List<StockCardViewModel> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockCardViewModel model = new StockCardViewModel(stockCard);
            list.add(model);
            final Product product = new Product();
            product.setPrimaryName("Product" + i);
            model.setProduct(product);
        }

        adapter.refreshList(list);

        //+1 for footer view (Done Btn)
        assertThat(adapter.getItemCount(), is(10 + 1));
        adapter.filter("1");
        assertThat(adapter.getItemCount(), is(1 + 1));
    }

}
