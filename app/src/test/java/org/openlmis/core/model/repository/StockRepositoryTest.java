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


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class StockRepositoryTest extends LMISRepositoryUnitTest {

    StockRepository stockRepository;
    ProductRepository productRepository;
    Product product;

    @Before
    public void setup() throws LMISException{

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        productRepository.create(product);
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
        StockCard stockCard = StockCardBuilder.buildStockCardWithOneMovement(stockRepository);
        StockMovementItem stockMovementItem = stockCard.getStockMovementItems().iterator().next();

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
        StockCard stockCard = StockCardBuilder.buildStockCardWithOneMovement(stockRepository);
        StockMovementItem stockMovementItem = new StockMovementItem();

        stockCard.setStockOnHand(100L);
        stockMovementItem.setStockOnHand(-1);
        stockMovementItem.setMovementQuantity(50L);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
        stockMovementItem.setMovementDate(DateUtil.today());

        stockRepository.addStockMovementAndUpdateStockCard(stockCard, stockMovementItem);
        assertThat(stockMovementItem.getStockOnHand(), is(150L));

        stockCard.setStockOnHand(100L);
        stockMovementItem.setStockOnHand(-1);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.ISSUE);
        stockRepository.addStockMovementAndUpdateStockCard(stockCard, stockMovementItem);

        assertThat(stockMovementItem.getStockOnHand(), is(50L));
    }

    @Test
    public void shouldListUnsyncedStockMovementItems() throws LMISException {
        StockCard stockCard = StockCardBuilder.buildStockCardWithOneMovement(stockRepository);

        StockMovementItem item = new StockMovementItem();
        item.setMovementQuantity(100L);
        item.setStockOnHand(-1);
        item.setMovementDate(DateUtil.today());
        item.setMovementType(StockMovementItem.MovementType.RECEIVE);

        item.setSynced(true);

        assertThat(stockCard.getStockMovementItems().size(), is(1));
        stockRepository.addStockMovementAndUpdateStockCard(stockCard, item);
        stockRepository.refresh(stockCard);

        assertThat(stockCard.getStockMovementItems().size(), is(2));

        assertThat(stockRepository.listUnSynced(), notNullValue());
        assertThat(stockRepository.listUnSynced().size(), is(1));
    }

    @Test
    public void shouldBatchUpdateStockMovements() throws LMISException {
        StockCard stockCard = StockCardBuilder.buildStockCardWithOneMovement(stockRepository);
        StockMovementItem item = new StockMovementItem();
        item.setMovementQuantity(100L);
        item.setStockOnHand(-1);
        item.setMovementDate(DateUtil.today());
        item.setMovementType(StockMovementItem.MovementType.RECEIVE);

        stockRepository.addStockMovementAndUpdateStockCard(stockCard, item);
        stockRepository.refresh(stockCard);

        List<StockMovementItem> items = newArrayList(stockCard.getStockMovementItems());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(false));
        assertThat(items.get(1).isSynced(), is(false));

        for (StockMovementItem entry : items){
            entry.setSynced(true);
        }

        stockRepository.batchUpdateStockMovements(items);

        stockCard = stockRepository.list().get(0);
        items = newArrayList(stockCard.getStockMovementItems());

        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced() , is(true));
        assertThat(items.get(1).isSynced(), is(true));
    }

    @Test
    public void shouldInitStockMovementFromStockCard() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setStockOnHand(200);
        StockMovementItem stockMovementItem = stockRepository.initStockMovementItem(stockCard);
        assertThat(stockMovementItem.getMovementQuantity(),is(200L));
        assertThat(stockMovementItem.getReason(), is(MovementReasonManager.INVENTORY));
        assertThat(stockMovementItem.getMovementType(), is(StockMovementItem.MovementType.PHYSICAL_INVENTORY));
    }

    @Test
    public void shouldListDraftInventory() throws Exception {
        saveDraftInventory();

        List<DraftInventory> draftInventories = stockRepository.listDraftInventory();
        assertThat(draftInventories.get(0).getQuantity(),is(10L));
        assertThat(draftInventories.get(0).getExpireDates(),is("11/10/2015"));
        assertThat(draftInventories.get(1).getQuantity(),is(20L));
        assertThat(draftInventories.get(1).getExpireDates(),is("12/10/2015"));
    }

    private void saveDraftInventory() throws LMISException {
        DraftInventory draftInventory1 = new DraftInventory();
        draftInventory1.setQuantity(10L);
        draftInventory1.setExpireDates("11/10/2015");
        DraftInventory draftInventory2 = new DraftInventory();
        draftInventory2.setQuantity(20L);
        draftInventory2.setExpireDates("12/10/2015");

        stockRepository.saveDraftInventory(draftInventory1);
        stockRepository.saveDraftInventory(draftInventory2);
    }

    @Test
    public void shouldClearDraftInventory() throws Exception {
        saveDraftInventory();
        Assert.assertThat(stockRepository.listDraftInventory().size(), is(2));
        stockRepository.clearDraftInventory();
        Assert.assertThat(stockRepository.listDraftInventory().size(),is(0));
    }

}
