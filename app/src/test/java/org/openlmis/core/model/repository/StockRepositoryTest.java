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


import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.openlmis.core.model.StockMovementItem.MovementType.ISSUE;
import static org.openlmis.core.model.StockMovementItem.MovementType.RECEIVE;
import static org.openlmis.core.model.builder.StockCardBuilder.saveStockCardWithOneMovement;
import static org.openlmis.core.utils.DateUtil.SIMPLE_DATE_FORMAT;
import static org.openlmis.core.utils.DateUtil.parseString;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class StockRepositoryTest extends LMISRepositoryUnitTest {

    StockRepository stockRepository;
    ProductRepository productRepository;
    Product product;
    private Date lastFirstMonthDate;
    private Date lastSecondMonthDate;
    private Date lastThirdMonthDate;
    private Date lastForthMonthDate;
    private ProgramRepository programRepository;
    private StockCard stockCard;

    @Before
    public void setup() throws LMISException {

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        programRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramRepository.class);
        saveTestProduct();

        Date today = DateUtil.today();
        lastFirstMonthDate = DateUtil.generatePreviousMonthDateBy(today);
        lastSecondMonthDate = DateUtil.generatePreviousMonthDateBy(lastFirstMonthDate);
        lastThirdMonthDate = DateUtil.generatePreviousMonthDateBy(lastSecondMonthDate);
        lastForthMonthDate = DateUtil.generatePreviousMonthDateBy(lastThirdMonthDate);

        stockCard = new StockCard();
    }

    @Test
    public void shouldSaveStockCardsSuccessful() throws LMISException {
        stockCard.setStockOnHand(1);
        stockCard.setProduct(product);

        stockRepository.save(stockCard);

        assertThat(stockRepository.list().size(), is(1));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }

    @Test
    public void shouldBatchSaveStockcardAndMovementSuccessful() throws LMISException {
        ArrayList<StockCard> stockCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockCard stockCard = new StockCard();
            stockCard.setStockOnHand(i);
            stockCard.setProduct(product);
            stockCard.setStockMovementItemsWrapper(Arrays.asList(stockCard.generateInitialStockMovementItem()));

            stockCards.add(stockCard);
        }

        stockRepository.batchSaveStockCardsWithMovementItems(stockCards);

        assertThat(stockRepository.list().size(), is(10));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
        assertThat(stockRepository.list().get(0).getForeignStockMovementItems().size(), is(1));
    }

    @Test
    public void shouldGetCorrectDataAfterSavedStockMovementItem() throws Exception {
        //given saved
        StockCard savedStockCard = saveStockCardWithOneMovement(stockRepository);
        StockMovementItem savedMovementItem = savedStockCard.getForeignStockMovementItems().iterator().next();

        //when retrieve
        List<StockMovementItem> retrievedStockMovementItems = stockRepository.listLastFive(savedStockCard.getId());
        StockMovementItem retrievedMovementItem = retrievedStockMovementItems.get(retrievedStockMovementItems.size() - 1);

        //then
        assertEquals(savedMovementItem.getId(), retrievedMovementItem.getId());
        assertEquals(savedMovementItem.getMovementQuantity(), retrievedMovementItem.getMovementQuantity());
        assertEquals(savedMovementItem.getStockOnHand(), retrievedMovementItem.getStockOnHand());
        assertEquals(savedMovementItem.getMovementType(), retrievedMovementItem.getMovementType());
        assertEquals(savedMovementItem.getDocumentNumber(), retrievedMovementItem.getDocumentNumber());
        assertEquals(savedMovementItem.getReason(), retrievedMovementItem.getReason());
    }

    @Test
    public void shouldListUnsyncedStockMovementItems() throws LMISException, ParseException {
        //given one movement was saved but NOT SYNCED
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);
        assertThat(stockCard.getForeignStockMovementItems().size(), is(1));
        assertThat(stockRepository.listUnSynced().size(), is(1));

        //when save another SYNCED movement
        StockMovementItem item = createMovementItem(RECEIVE, 100, stockCard);
        item.setSynced(true);
        stockCard.setStockOnHand(item.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(item);
        stockRepository.refresh(stockCard);

        //then
        assertThat(stockCard.getForeignStockMovementItems().size(), is(2));
        assertThat(stockRepository.listUnSynced(), notNullValue());
        assertThat(stockRepository.listUnSynced().size(), is(1));
    }

    @Test
    public void shouldBatchUpdateStockMovements() throws LMISException, ParseException {
        //given
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);

        //when
        StockMovementItem item = createMovementItem(RECEIVE, 100, stockCard);
        stockCard.setStockOnHand(item.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(item);
        stockRepository.refresh(stockCard);

        //then
        List<StockMovementItem> items = newArrayList(stockCard.getForeignStockMovementItems());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(false));
        assertThat(items.get(1).isSynced(), is(false));

        //when
        for (StockMovementItem entry : items) {
            entry.setSynced(true);
        }
        stockRepository.batchCreateOrUpdateStockMovements(items);

        //then
        stockCard = stockRepository.list().get(0);
        items = newArrayList(stockCard.getForeignStockMovementItems());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(true));
        assertThat(items.get(1).isSynced(), is(true));
    }

    @Test
    public void shouldListDraftInventory() throws Exception {
        saveDraftInventory();

        List<DraftInventory> draftInventories = stockRepository.listDraftInventory();
        assertThat(draftInventories.get(0).getQuantity(), is(10L));
        assertThat(draftInventories.get(0).getExpireDates(), is("11/10/2015"));
        assertThat(draftInventories.get(1).getQuantity(), is(20L));
        assertThat(draftInventories.get(1).getExpireDates(), is("12/10/2015"));
    }

    @Test
    public void shouldClearDraftInventory() throws Exception {
        saveDraftInventory();
        Assert.assertThat(stockRepository.listDraftInventory().size(), is(2));
        stockRepository.clearDraftInventory();
        Assert.assertThat(stockRepository.listDraftInventory().size(), is(0));
    }

    @Test
    public void shouldGetFirstPeriodDate() throws Exception {
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);

        Date firstMovementDate = DateUtil.parseString("10/09/2014", DateUtil.SIMPLE_DATE_FORMAT);
        createStockMovementItem(stockCard, firstMovementDate);
        createStockMovementItem(stockCard, DateUtil.parseString("11/10/2015", DateUtil.SIMPLE_DATE_FORMAT));
        createStockMovementItem(stockCard, DateUtil.parseString("12/11/2015", DateUtil.SIMPLE_DATE_FORMAT));

        Date firstPeriodBegin = stockRepository.queryFirstPeriodBegin(stockCard);
        assertThat(firstPeriodBegin, is(parseString("21/08/2014", SIMPLE_DATE_FORMAT)));
    }

    @Test
    public void shouldGetLowStockAvgIsZeroWhenOnlyTwoValidPeriod() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(300);
        stockRepository.save(stockCard);

        createStockMovementItem(stockCard, lastFirstMonthDate);
        createStockMovementItem(stockCard, lastSecondMonthDate);

        int lowStockAvg = stockRepository.getLowStockAvg(stockCard);
        assertEquals(2, stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(0, lowStockAvg);
    }

    @Test
    public void shouldGetLowStockAvgCorrectly() throws Exception {

        stockCard.setStockOnHand(400);
        stockRepository.save(stockCard);

        createStockMovementItem(stockCard, lastFirstMonthDate);
        createStockMovementItem(stockCard, lastSecondMonthDate);
        createStockMovementItem(stockCard, lastThirdMonthDate);

        int lowStockAvg = stockRepository.getLowStockAvg(stockCard);
        assertEquals(3, stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(5, lowStockAvg);
    }

    @Test
    public void shouldGetStockCardsByProgramIdWithoutKitAndDeacitivated() throws Exception {
        //when
        long programId1 = createNewStockCard("code1", ProductBuilder.create().setCode("p1").setIsActive(true).setIsKit(false).build());
        createNewStockCard("code1", ProductBuilder.create().setCode("p3").setIsActive(false).setIsKit(false).build());

        long programId2 = createNewStockCard("code2", ProductBuilder.create().setCode("p2").setIsActive(true).setIsKit(false).build());
        createNewStockCard("code2", ProductBuilder.create().setCode("p4").setIsActive(true).setIsKit(true).build());

        Product product = ProductBuilder.buildAdultProduct();
        product.setKit(true);
        productRepository.createOrUpdate(product);

        //then
        List<StockCard> stockCardsBeforeTimeLine = stockRepository.listActiveStockCardsByProgramId(programId1);
        assertThat(stockCardsBeforeTimeLine.size(), is(1));

        List<StockCard> stockCardsBeforeTimeLine2 = stockRepository.listActiveStockCardsByProgramId(programId2);
        assertThat(stockCardsBeforeTimeLine2.size(), is(2));
    }

    @Test
    public void shouldGetFirstItemWhenMovementDateDiff() throws Exception {
        //given
        DateTime dateTime = new DateTime();

        stockRepository.save(stockCard);

        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

        stockMovementItems.add(getStockMovementItem(dateTime, dateTime));

        DateTime earlierTime = dateTime.minusMonths(1);
        StockMovementItem movementEarlierItem = getStockMovementItem(dateTime, earlierTime);
        stockMovementItems.add(movementEarlierItem);
        stockRepository.batchCreateOrUpdateStockMovements(stockMovementItems);

        //when
        StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);

        //then
        assertThat(stockMovementItem.getId(), is(movementEarlierItem.getId()));
    }

    @Test
    public void shouldGetFirstItemWhenCreatedTimeDiff() throws Exception {
        //given
        DateTime dateTime = new DateTime();
        stockRepository.save(stockCard);

        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

        stockMovementItems.add(getStockMovementItem(dateTime, dateTime));

        DateTime earlierTime = dateTime.minusMonths(1);
        StockMovementItem createdEarlierMovementItem = getStockMovementItem(earlierTime, dateTime);
        stockMovementItems.add(createdEarlierMovementItem);

        stockRepository.batchCreateOrUpdateStockMovements(stockMovementItems);

        //when
        StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);

        //then
        assertThat(stockMovementItem.getId(), is(createdEarlierMovementItem.getId()));
    }

    @Test
    public void shouldGetLowStockAvgWhenLastMonthHaveNoStockItem() throws Exception {
        stockCard.setStockOnHand(400);
        stockRepository.save(stockCard);

        createStockMovementItem(stockCard, lastFirstMonthDate);
        createStockMovementItem(stockCard, lastThirdMonthDate);
        createStockMovementItem(stockCard, lastForthMonthDate);

        int lowStockAvg = stockRepository.getLowStockAvg(stockCard);
        assertEquals(3, stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(4, lowStockAvg);
    }

    @Test
    public void shouldGetAverageMonthlyConsumptionCorrectly() throws LMISException {
        //given
        stockCard.setStockOnHand(200);
        stockRepository.save(stockCard);
        createStockMovementItem(stockCard, lastForthMonthDate);
        createStockMovementItem(stockCard, lastThirdMonthDate);

        StockMovementItem item = createMovementItem(RECEIVE, 400, stockCard);
        item.setMovementDate(lastSecondMonthDate);
        stockCard.setStockOnHand(item.getStockOnHand());

        createStockMovementItem(stockCard, lastSecondMonthDate);
        createStockMovementItem(stockCard, lastFirstMonthDate);

        //when
        long consumption = stockRepository.getCmm(stockCard);

        //then
        assertEquals(100, consumption);
    }

    private StockMovementItem getStockMovementItem(DateTime createdTime, DateTime movementDate) {
        StockMovementItem movementItem = new StockMovementItem();
        movementItem.setStockCard(stockCard);
        movementItem.setCreatedTime(createdTime.toDate());
        movementItem.setMovementDate(movementDate.toDate());
        return movementItem;
    }

    private long createNewStockCard(String code, Product product) throws LMISException {
        Program program = new Program();
        program.setProgramCode(code);
        programRepository.createOrUpdate(program);

        product.setProgram(program);
        productRepository.createOrUpdate(product);

        stockCard.setProduct(product);
        stockCard.setCreatedAt(new Date());
        stockRepository.save(stockCard);

        return program.getId();
    }

    private long createStockCardWithDeactivatedProduct() throws LMISException {
        Program program = new Program();
        program.setProgramCode("MMIA");
        programRepository.createOrUpdate(program);

        Product product = new Product();
        product.setProgram(program);
        product.setCode("some code");
        product.setActive(false);
        productRepository.createOrUpdate(product);

        stockCard.setProduct(product);
        stockCard.setCreatedAt(new Date());
        stockRepository.save(stockCard);

        return program.getId();
    }

    @Test
    public void shouldGetLowStockAvgWhenLastMonthSOHIsZero() throws Exception {
        stockCard.setStockOnHand(300);
        stockRepository.save(stockCard);

        createStockMovementItem(stockCard, lastFirstMonthDate);
        createStockMovementItem(stockCard, lastThirdMonthDate);
        createStockMovementItem(stockCard, lastSecondMonthDate);

        int lowStockAvg = stockRepository.getLowStockAvg(stockCard);
        assertEquals(3, stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(0, lowStockAvg);
    }

    private void createStockMovementItem(StockCard stockCard, Date lastThirdMonthDate) throws LMISException {
        StockMovementItem item = createMovementItem(ISSUE, 100, stockCard);
        item.setMovementDate(lastThirdMonthDate);
        stockCard.setStockOnHand(item.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(item);
        stockRepository.refresh(stockCard);
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

    private void saveTestProduct() throws LMISException {
        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");
        product.setCode("test code");

        productRepository.createOrUpdate(product);
    }

    private StockMovementItem createMovementItem(StockMovementItem.MovementType type, long quantity, StockCard stockCard) {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementQuantity(quantity);
        stockMovementItem.setMovementType(type);
        stockMovementItem.setMovementDate(DateUtil.today());
        stockMovementItem.setStockCard(stockCard);

        if (stockMovementItem.isPositiveMovement()) {
            stockMovementItem.setStockOnHand(stockCard.getStockOnHand() + quantity);
        } else {
            stockMovementItem.setStockOnHand(stockCard.getStockOnHand() - quantity);
        }

        return stockMovementItem;
    }
}
