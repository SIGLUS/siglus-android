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
import org.openlmis.core.model.StockMovementItem;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@RunWith(LMISTestRunner.class)
public class StockRepositoryTest extends LMISRepositoryUnitTest {

    StockRepository stockRepository;
    ProductRepository productRepository;
    Product product;
    private StockMovementItem stockMovementItem;
    private StockCard stockCard;

    @Before
    public void setup() throws LMISException{

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        productRepository.create(product);

        stockMovementItem = new StockMovementItem();

        stockCard = new StockCard();

        stockMovementItem.setStockCard(stockCard);
        stockMovementItem.setMovementQuantity(10L);
        stockMovementItem.setStockOnHand(100L);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
        stockMovementItem.setDocumentNumber("XXX123456");
        stockMovementItem.setReason("some reason");
        stockMovementItem.setMovementDate(new Date());

        stockRepository.saveStockItem(stockMovementItem);
    }



    @Test
    public void shouldSaveStockCardsSuccessful() throws LMISException{

        StockCard stockCard = new StockCard();
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
            stockCard.setStockOnHand(i);
            stockCard.setProduct(product);

            stockCards.add(stockCard);
        }

        stockRepository.batchSave(stockCards);

        assertThat(stockRepository.list().size(), is(10));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }

    @Test
    public void shouldGetCorrectDataAfterSavedStockMovementItem() throws Exception {
        List<StockMovementItem> stockMovementItems = stockRepository.listLastFive(stockCard.getId());
        StockMovementItem stockMovementItemActual = stockMovementItems.get(stockMovementItems.size() - 1);

        assertEquals(stockMovementItem.getId(), stockMovementItemActual.getId());
        assertEquals(stockMovementItem.getMovementQuantity(), stockMovementItemActual.getMovementQuantity());
        assertEquals(stockMovementItem.getStockOnHand(), stockMovementItemActual.getStockOnHand());
        assertEquals(stockMovementItem.getMovementType(), stockMovementItemActual.getMovementType());
        assertEquals(stockMovementItem.getDocumentNumber(), stockMovementItemActual.getDocumentNumber());
        assertEquals(stockMovementItem.getReason(), stockMovementItemActual.getReason());
    }


    @Test
    public void shouldCalculateStockOnHandCorrectly() throws LMISException{
        stockCard.setStockOnHand(100L);
        stockMovementItem.setStockOnHand(-1);
        stockMovementItem.setMovementQuantity(50L);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);

        stockRepository.addStockMovementAndUpdateStockCard(stockCard, stockMovementItem);
        assertThat(stockMovementItem.getStockOnHand(), is(150L));

        stockCard.setStockOnHand(100L);
        stockMovementItem.setStockOnHand(-1);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.ISSUE);
        stockRepository.addStockMovementAndUpdateStockCard(stockCard, stockMovementItem);

        assertThat(stockMovementItem.getStockOnHand(), is(50L));
    }
}
