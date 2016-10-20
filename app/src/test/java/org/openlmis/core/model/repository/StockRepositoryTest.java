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


import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.ISSUE;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;
import static org.openlmis.core.model.builder.StockCardBuilder.saveStockCardWithOneMovement;
import static org.openlmis.core.utils.DateUtil.DATE_TIME_FORMAT;
import static org.openlmis.core.utils.DateUtil.SIMPLE_DATE_FORMAT;
import static org.openlmis.core.utils.DateUtil.parseString;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class StockRepositoryTest extends LMISRepositoryUnitTest {

    StockRepository stockRepository;
    ProductRepository productRepository;
    LotRepository lotRepository;
    Product product;
    private ProgramRepository programRepository;
    private ProductProgramRepository productProgramRepository;
    private StockCard stockCard;

    @Before
    public void setup() throws LMISException {

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        programRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramRepository.class);
        productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductProgramRepository.class);
        lotRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(LotRepository.class);

        saveTestProduct();

        stockCard = new StockCard();
    }

    @Test
    public void shouldSaveStockCardsSuccessful() throws LMISException {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(1);
        stockCard.setProduct(product);

        stockRepository.createOrUpdate(stockCard);

        assertThat(stockRepository.list().size(), is(1));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }

    @Test
    public void shouldBatchSaveStockCardAndMovementSuccessful() throws LMISException {
        ArrayList<StockCard> stockCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockCard stockCard = new StockCard();
            stockCard.setStockOnHand(i);
            product.setArchived(true);
            stockCard.setProduct(product);
            stockCard.setStockMovementItemsWrapper(Arrays.asList(stockCard.generateInitialStockMovementItem()));

            stockCards.add(stockCard);
        }

        stockRepository.batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(stockCards);

        List<StockCard> stockCardList = stockRepository.list();

        assertThat(stockCardList.size(), is(10));
        assertThat(stockCardList.get(0).getProduct(), is(NotNull.NOT_NULL));
        assertThat(stockCardList.get(0).getForeignStockMovementItems().size(), is(1));
        assertTrue(stockCardList.get(0).getProduct().isArchived());
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
        createMovementItem(RECEIVE, 100, stockCard, new Date(), DateUtil.today(), true);

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
        createMovementItem(RECEIVE, 100, stockCard, new Date(), DateUtil.today(), false);

        //then
        List<StockMovementItem> items = newArrayList(stockCard.getForeignStockMovementItems());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(false));
        assertThat(items.get(1).isSynced(), is(false));

        //when
        for (StockMovementItem entry : items) {
            entry.setSynced(true);
        }
        stockRepository.batchCreateOrUpdateStockMovementsAndLotInfo(items);

        //then
        stockCard = stockRepository.list().get(0);
        items = newArrayList(stockCard.getForeignStockMovementItems());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(true));
        assertThat(items.get(1).isSynced(), is(true));
    }

    @Test
    public void shouldGetFirstPeriodDate() throws Exception {
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);

        Date firstMovementDate = DateUtil.parseString("10/09/2014", DateUtil.SIMPLE_DATE_FORMAT);
        createMovementItem(ISSUE, 100, stockCard, new Date(), firstMovementDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), DateUtil.parseString("11/10/2015", DateUtil.SIMPLE_DATE_FORMAT), false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), DateUtil.parseString("12/11/2015", DateUtil.SIMPLE_DATE_FORMAT), false);

        Date firstPeriodBegin = stockRepository.queryFirstPeriodBegin(stockCard);
        assertThat(firstPeriodBegin, is(parseString("21/08/2014", SIMPLE_DATE_FORMAT)));
    }

    @Test
    public void shouldGetStockCardsBeforePeriodEndDate() throws Exception {
        Program program1 = new ProgramBuilder().setProgramCode("code1").build();
        Program program2 = new ProgramBuilder().setProgramCode("code2").setParentCode("code1").build();
        Program program3 = new ProgramBuilder().setProgramCode("code3").build();
        generateTestDataForGetStockCards("P1", true, false, program1, "1969-11-11");
        generateTestDataForGetStockCards("P2", true, false, program1, "1970-11-11");
        generateTestDataForGetStockCards("P3", true, false, program2, "1969-11-11");
        generateTestDataForGetStockCards("P4", true, false, program3, "1969-11-11");

        DateTime periodBegin = new DateTime(DateUtil.parseString("1970-01-01 10:10:10", DateUtil.DATE_TIME_FORMAT));
        DateTime periodEnd = new DateTime(DateUtil.parseString("1970-02-21 10:10:10", DateUtil.DATE_TIME_FORMAT));
        RnRForm rnRForm = RnRForm.init(program1, new Period(periodBegin, periodEnd), false);
        List<StockCard> stockCardsBeforeTimeLine = stockRepository.getStockCardsBeforePeriodEnd(rnRForm);
        assertThat(stockCardsBeforeTimeLine.size(), is(2));
        assertThat(stockCardsBeforeTimeLine.get(0).getProduct().getCode(), is("P1"));
        assertThat(stockCardsBeforeTimeLine.get(1).getProduct().getCode(), is("P3"));
    }

    @Test
    public void shouldGetActiveAndNotArchivedStockCardsBeforePeriodEndDate() throws Exception {
        Program program1 = new ProgramBuilder().setProgramCode("code1").build();
        Program program2 = new ProgramBuilder().setProgramCode("code2").setParentCode("code1").build();
        generateTestDataForGetStockCards("P1", true, false, program1, "1969-11-11");
        generateTestDataForGetStockCards("P2", false, false, program1, "1969-11-11");
        generateTestDataForGetStockCards("P3", true, false, program2, "1969-11-11");
        generateTestDataForGetStockCards("P4", true, true, program2, "1969-11-11");

        DateTime periodBegin = new DateTime(DateUtil.parseString("1970-01-01 10:10:10", DateUtil.DATE_TIME_FORMAT));
        DateTime periodEnd = new DateTime(DateUtil.parseString("1970-02-21 10:10:10", DateUtil.DATE_TIME_FORMAT));
        RnRForm rnRForm = RnRForm.init(program1, new Period(periodBegin, periodEnd), false);
        List<StockCard> stockCardsBeforeTimeLine = stockRepository.getStockCardsBeforePeriodEnd(rnRForm);
        assertThat(stockCardsBeforeTimeLine.size(), is(2));
        assertThat(stockCardsBeforeTimeLine.get(0).getProduct().getCode(), is("P1"));
        assertThat(stockCardsBeforeTimeLine.get(1).getProduct().getCode(), is("P3"));
    }

    private void generateTestDataForGetStockCards(String productCode, boolean isActive, boolean isArchived, Program program, String movementDate) throws LMISException {
        Product product = new ProductBuilder().create().setCode(productCode).setIsActive(isActive).setIsArchived(isArchived).build();

        productRepository.createOrUpdate(product);
        programRepository.createOrUpdate(program);
        createNewProductProgram(program.getProgramCode(), product.getCode());

        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockRepository.createOrUpdate(stockCard);

        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setStockCard(stockCard);
        stockMovementItem.setMovementDate(DateUtil.parseString(movementDate, "yyyy-MM-dd"));

        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
        stockRepository.refresh(stockCard);
    }

    @Test
    public void shouldGetFirstItemWhenMovementDateDiff() throws Exception {
        //given
        DateTime dateTime = new DateTime();

        stockRepository.createOrUpdate(stockCard);

        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

        stockMovementItems.add(getStockMovementItem(dateTime, dateTime));

        DateTime earlierTime = dateTime.minusMonths(1);
        StockMovementItem movementEarlierItem = getStockMovementItem(dateTime, earlierTime);
        stockMovementItems.add(movementEarlierItem);
        stockRepository.batchCreateOrUpdateStockMovementsAndLotInfo(stockMovementItems);

        //when
        StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);

        //then
        assertThat(stockMovementItem.getId(), is(movementEarlierItem.getId()));
    }

    @Test
    public void shouldGetFirstItemWhenCreatedTimeDiff() throws Exception {
        //given
        DateTime dateTime = new DateTime();
        stockRepository.createOrUpdate(stockCard);

        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

        stockMovementItems.add(getStockMovementItem(dateTime, dateTime));

        DateTime earlierTime = dateTime.minusMonths(1);
        StockMovementItem createdEarlierMovementItem = getStockMovementItem(earlierTime, dateTime);
        stockMovementItems.add(createdEarlierMovementItem);

        stockRepository.batchCreateOrUpdateStockMovementsAndLotInfo(stockMovementItems);

        //when
        StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);

        //then
        assertThat(stockMovementItem.getId(), is(createdEarlierMovementItem.getId()));
    }

    @Test
    public void shouldGetStockMovementsCreatedBetweenTwoDatesExclusiveOfBeginDate() throws LMISException {
        stockCard.setStockOnHand(100);
        stockRepository.createOrUpdate(stockCard);
        StockMovementItem movementItem = createMovementItem(ISSUE, 1, stockCard, DateUtil.parseString("2020-01-21 13:00:00", DATE_TIME_FORMAT), DateUtil.parseString("2020-01-21 13:00:00", DATE_TIME_FORMAT), false);
        createMovementItem(RECEIVE, 2, stockCard, DateUtil.parseString("2020-02-01 11:00:00", DATE_TIME_FORMAT), DateUtil.parseString("2020-02-01 11:00:00", DATE_TIME_FORMAT), false);
        createMovementItem(ISSUE, 3, stockCard, DateUtil.parseString("2020-02-22 20:00:00", DATE_TIME_FORMAT), DateUtil.parseString("2020-02-22 20:00:00", DATE_TIME_FORMAT), false);
        createMovementItem(ISSUE, 4, stockCard, DateUtil.parseString("2020-02-25 13:00:00", DATE_TIME_FORMAT), DateUtil.parseString("2020-02-25 13:00:00", DATE_TIME_FORMAT), false);

        List<StockMovementItem> stockMovementItems = stockRepository.queryStockItemsByPeriodDates(stockCard,
                movementItem.getCreatedTime(), DateUtil.parseString("2020-02-22 20:00:00", DATE_TIME_FORMAT));

        Assert.assertThat(stockMovementItems.size(), is(2));
    }

    private StockMovementItem getStockMovementItem(DateTime createdTime, DateTime movementDate) {
        StockMovementItem movementItem = new StockMovementItem();
        movementItem.setStockCard(stockCard);
        movementItem.setCreatedTime(createdTime.toDate());
        movementItem.setMovementDate(movementDate.toDate());
        return movementItem;
    }

    private StockCard createNewStockCard(String code, String parentCode, Product product, boolean isEmergency) throws LMISException {
        StockCard stockCard = new StockCard();
        Program program = createNewProgram(code, parentCode, isEmergency);
        programRepository.createOrUpdate(program);
        productRepository.createOrUpdate(product);

        ProductProgram productProgram = new ProductProgramBuilder()
                .setProductCode(product.getCode())
                .setProgramCode(program.getProgramCode())
                .setActive(true).build();

        productProgramRepository.createOrUpdate(productProgram);

        stockCard.setProduct(product);
        stockCard.setCreatedAt(new Date());
        stockRepository.createOrUpdate(stockCard);

        return stockCard;
    }

    private void createNewProductProgram(String code, String productCode) throws LMISException {
        ProductProgram productProgram = new ProductProgramBuilder().setProgramCode(code).setProductCode(productCode).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram);
    }

    @NonNull
    private Program createNewProgram(String code, String parentCode, boolean isSupportEmergency) throws LMISException {
        Program program = new ProgramBuilder().setProgramCode(code).setParentCode(parentCode).setSupportEmergency(isSupportEmergency).build();
        programRepository.createOrUpdate(program);
        return program;
    }

    @Test
    public void shouldUpdateStockCardAndProduct() throws Exception {
        StockCard stockCard = new StockCard();
        product.setArchived(true);
        stockCard.setProduct(product);
        stockCard.setExpireDates("01/01/2016");

        stockRepository.createOrUpdate(stockCard);

        stockCard.setExpireDates("");
        product.setArchived(false);
        stockRepository.updateStockCardWithProduct(stockCard);

        assertThat(stockCard.getExpireDates(), is(""));

    }

    @Test
    public void shouldUpdateProductOfStockCard() throws Exception {
        StockCard stockCard = new StockCard();
        product.setArchived(true);
        stockCard.setProduct(product);
        stockCard.setExpireDates("01/01/2016");

        stockRepository.createOrUpdate(stockCard);

        stockCard.setExpireDates("");
        product.setArchived(false);
        stockRepository.updateProductOfStockCard(stockCard.getProduct());

        assertThat(stockCard.getProduct().isArchived(), is(false));
    }

    @Test
    public void shouldQueryEarliestStockMovementItemCreatedTimeByProgram() throws Exception {
        Program mmia = new ProgramBuilder().setProgramCode("MMIA").build();
        Program via = new ProgramBuilder().setProgramCode("VIA").build();
        programRepository.createOrUpdate(mmia);
        programRepository.createOrUpdate(via);
        Product mmiaProduct = new ProductBuilder().setCode("B1").build();
        productRepository.createOrUpdate(mmiaProduct);
        Product viaProduct = new ProductBuilder().setCode("A1").build();
        productRepository.createOrUpdate(viaProduct);

        ArrayList<ProductProgram> productPrograms = new ArrayList<>();
        ProductProgram viaProductProgram = new ProductProgram();
        viaProductProgram.setProductCode(viaProduct.getCode());
        viaProductProgram.setProgramCode(via.getProgramCode());
        viaProductProgram.setActive(true);
        productPrograms.add(viaProductProgram);

        ProductProgram mmiaProductProgram = new ProductProgram();
        mmiaProductProgram.setProductCode(viaProduct.getCode());
        mmiaProductProgram.setProgramCode(via.getProgramCode());
        mmiaProductProgram.setActive(true);
        productPrograms.add(mmiaProductProgram);

        productProgramRepository.batchSave(productPrograms);

        StockCard stockCard = new StockCard();
        stockCard.setProduct(mmiaProduct);
        stockRepository.createOrUpdate(stockCard);
        StockCard stockCard2 = new StockCard();
        stockCard2.setProduct(viaProduct);
        stockRepository.createOrUpdate(stockCard2);

        createMovementItem(ISSUE, 100, stockCard, new DateTime("2017-01-01").toDate(), new DateTime("2017-01-01").toDate(), false);
        createMovementItem(ISSUE, 100, stockCard2, new DateTime("2018-01-01").toDate(), new DateTime("2018-01-01").toDate(), false);
        DateTime dateTime = new DateTime("2018-01-01");
        Date expectedDate = new DateTime().withDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth()).toDate();
        createMovementItem(ISSUE, 100, stockCard2, new DateTime("2018-03-02").toDate(), new DateTime("2018-03-02").toDate(), false);

        Date earliestDate = stockRepository.queryEarliestStockMovementDateByProgram(via.getProgramCode());
        Assert.assertThat(DateUtil.cutTimeStamp(new DateTime(earliestDate)), is(DateUtil.cutTimeStamp(new DateTime(expectedDate))));
    }

    @Test
    public void shouldLoadEmergencyProducts() throws Exception {
        //when
        createNewStockCard("code", null, ProductBuilder.create().setCode("p1").setIsActive(true).setIsKit(false).build(), true);
        createNewStockCard("otherCode", "parentCode", ProductBuilder.create().setCode("p2").setIsActive(true).setIsKit(false).build(), false);

        Product product = ProductBuilder.buildAdultProduct();
        product.setKit(true);
        productRepository.createOrUpdate(product);

        //then
        List<StockCard> stockCardsBeforeTimeLine = stockRepository.listEmergencyStockCards();
        assertThat(stockCardsBeforeTimeLine.size(), is(1));
    }

    private void saveTestProduct() throws LMISException {
        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");
        product.setCode("test code");

        productRepository.createOrUpdate(product);
    }

    private StockMovementItem createMovementItem(MovementReasonManager.MovementType type, long quantity, StockCard stockCard, Date createdTime, Date movementDate, boolean synced) throws LMISException {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementQuantity(quantity);
        stockMovementItem.setMovementType(type);
        stockMovementItem.setMovementDate(movementDate);
        stockMovementItem.setStockCard(stockCard);
        stockMovementItem.setSynced(synced);
        LMISTestApp.getInstance().setCurrentTimeMillis(createdTime.getTime());

        if (stockMovementItem.isPositiveMovement()) {
            stockMovementItem.setStockOnHand(stockCard.getStockOnHand() + quantity);
        } else {
            stockMovementItem.setStockOnHand(stockCard.getStockOnHand() - quantity);
        }

        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
        stockRepository.refresh(stockCard);

        return stockMovementItem;
    }

    @Test
    public void shouldSaveStockCardAndBatchUpdateMovements() throws Exception {
        Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true).setIsKit(false).build();
        productRepository.createOrUpdate(product);

        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setProduct(product);

        Lot lot1 = new Lot();
        lot1.setProduct(product);
        lot1.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
        lot1.setLotNumber("AAA");

        LotOnHand lotOnHand1 = new LotOnHand();
        lotOnHand1.setLot(lot1);
        lotOnHand1.setStockCard(stockCard);
        lotOnHand1.setQuantityOnHand(10L);

        stockCard.setLotOnHandListWrapper(Arrays.asList(lotOnHand1));

        StockMovementItem stockMovementItem = new StockMovementItemBuilder()
                .withStockOnHand(200)
                .withMovementType(MovementReasonManager.MovementType.RECEIVE)
                .withMovementDate("2015-12-31")
                .withQuantity(10)
                .build();
        stockMovementItem.setStockCard(stockCard);
        LotMovementItem lotMovementItem = new LotMovementItemBuilder()
                .setStockMovementItem(stockMovementItem)
                .setLot(lot1)
                .setMovementQuantity(2L)
                .setStockOnHand(12L).build();

        stockMovementItem.setLotMovementItemListWrapper(Arrays.asList(lotMovementItem));

        stockCard.setStockMovementItemsWrapper(Arrays.asList(stockMovementItem));

        stockRepository.saveStockCardAndBatchUpdateMovements(stockCard);

        StockCard queriedStockCard = stockRepository.queryStockCardById(stockCard.getId());
        assertThat(queriedStockCard.getProduct().getCode(), is(product.getCode()));
        assertThat(queriedStockCard.getLotOnHandListWrapper().get(0).getQuantityOnHand(), is(10L));

        StockMovementItem queriedStockMovementItem = queriedStockCard.getStockMovementItemsWrapper().get(0);
        assertThat(queriedStockMovementItem.getMovementQuantity(), is(10L));
        assertThat(queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getMovementQuantity(), is(2L));
        assertThat(queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getLot().getLotNumber(), is("AAA"));
    }

    @Test
    public void shouldDeleteDataOver13Months() throws Exception {
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);

        StockMovementItem stockMovementItem = createMovementItem(MovementReasonManager.MovementType.POSITIVE_ADJUST, 100L, stockCard, new Date(), DateUtil.parseString("2015-06-01", DateUtil.DB_DATE_FORMAT), true);

        Lot lot1 = new Lot();
        lot1.setProduct(product);
        lot1.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
        lot1.setLotNumber("AAA");

        LotMovementItem lotMovementItem = new LotMovementItemBuilder()
                .setStockMovementItem(stockMovementItem)
                .setLot(lot1)
                .setMovementQuantity(2L).build();

        lotRepository.batchCreateLotsAndLotMovements(Arrays.asList(lotMovementItem));

        stockRepository.deleteOldData();

        StockCard stockCardQueried = stockRepository.queryStockCardById(stockCard.getId());

        assertEquals(1, stockCardQueried.getStockMovementItemsWrapper().size());
        assertNotEquals(MovementReasonManager.MovementType.POSITIVE_ADJUST, stockCardQueried.getStockMovementItemsWrapper().get(0).getMovementType());
        assertEquals(0, stockCardQueried.getStockMovementItemsWrapper().get(0).getLotMovementItemListWrapper().size());
    }
}
