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

package org.openlmis.core.model.repository;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.Robolectric;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class StockRepositoryTest extends LMISRepositoryUnitTest {

    StockRepository stockRepository;
    ProductRepository productRepository;
    Product product;

    @Before
    public void setup() throws LMISException{

        stockRepository = RoboGuice.getInjector(Robolectric.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(Robolectric.application).getInstance(ProductRepository.class);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        productRepository.create(product);

    }



    @Test
    public void shouldSaveStockCardsSuccessful() throws LMISException{

        StockCard stockCard = new StockCard();
        stockCard.setStockCardId("ID");
        stockCard.setStockOnHand(1);
        stockCard.setProduct(product);
        stockRepository.save(stockCard);


        assertThat(stockRepository.list().size(), is(1));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }


    @Test
    public void shouldBathSaveSuccessful() throws LMISException{

        ArrayList<StockCard> stockCards = new ArrayList<>();

        for (int i =0; i < 10;i++){
            StockCard stockCard = new StockCard();
            stockCard.setStockCardId("ID" + i);
            stockCard.setStockOnHand(i);
            stockCard.setProduct(product);

            stockCards.add(stockCard);
        }

        stockRepository.batchSave(stockCards);

        assertThat(stockRepository.list().size(), is(10));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }

    @Test
    public void shouldGetStockItemsInPeriod() throws Exception{
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(1000);
        stockCard.setProduct(product);

        stockRepository.save(stockCard);

        Date date1 = DateUtil.parseString("2015-08-01", DateUtil.DEFAULT_DATE_FORMAT);
        Date date2 = DateUtil.parseString("2015-08-02", DateUtil.DEFAULT_DATE_FORMAT);

        ArrayList<StockItem> stockItems = new ArrayList<>();
        for (int i= 0; i < 10; i++){
            StockItem item = new StockItem();
            item.setStockOnHand(i);
            item.setProduct(product);
            item.setStockCard(stockCard);

            item.setAmount(i);

            if (i%2 == 0) {
                item.setMovementType(StockItem.MovementType.RECEIVE);
            }else {
                item.setMovementType(StockItem.MovementType.ISSUE);
            }

            item.setDocumentNumber("DOC" + i);

            if (i < 5){
                item.setCreatedAt(date1);
            } else {
                item.setCreatedAt(date2);
            }
            stockItems.add(item);
        }

        stockRepository.saveStockItems(stockItems);
        List<StockItem> retItems = stockRepository.listStockItems();

        assertThat(retItems.size(), is(10));

        long sum1 = stockRepository.sum(StockItem.MovementType.ISSUE, stockCard, date1, date1);
        long sum2 = stockRepository.sum(StockItem.MovementType.RECEIVE, stockCard, date1, date2);

        assertThat(sum1, is(4L));
        assertThat(sum2, is(20L));
    }
}
