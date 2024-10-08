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


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.utils.DateUtil.today;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enumeration.StockOnHandStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Cmm;
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
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class StockRepositoryTest extends LMISRepositoryUnitTest {

  StockRepository stockRepository;
  ProductRepository productRepository;
  LotRepository lotRepository;
  Product product;
  private ProgramRepository programRepository;
  private ProductProgramRepository productProgramRepository;
  private StockCard stockCard;
  private StockMovementRepository stockMovementRepository;

  @Before
  public void setup() throws LMISException {
    Application application = ApplicationProvider.getApplicationContext();

    stockRepository = RoboGuice.getInjector(application)
        .getInstance(StockRepository.class);
    productRepository = RoboGuice.getInjector(application)
        .getInstance(ProductRepository.class);
    programRepository = RoboGuice.getInjector(application)
        .getInstance(ProgramRepository.class);
    productProgramRepository = RoboGuice.getInjector(application)
        .getInstance(ProductProgramRepository.class);
    lotRepository = RoboGuice.getInjector(application)
        .getInstance(LotRepository.class);
    stockMovementRepository = RoboGuice.getInjector(application)
        .getInstance(StockMovementRepository.class);

    saveTestProduct();

    stockCard = new StockCard();
  }

  // TODO fix sqlite exception
  @Ignore("sqlite insert delay")
  @Test
  public void shouldCreateOrUpdateStockCard() throws LMISException {
    StockCard stockCard = new StockCard();
    stockCard.setStockOnHand(1);
    stockCard.setProduct(product);

    stockRepository.createOrUpdate(stockCard);
    assertEquals(stockCard, stockRepository.list().get(0));

    stockCard.setStockOnHand(10000);
    stockRepository.createOrUpdate(stockCard);
    assertEquals(stockCard, stockRepository.list().get(0));
  }

  @Test
  public void shouldGetStockCardsBeforePeriodEndDate() throws Exception {
    // given
    Program program1 = new ProgramBuilder().setProgramCode("code1").build();
    Program program3 = new ProgramBuilder().setProgramCode("code3").build();
    generateTestDataForGetStockCards("P1", true, false, program1, "1969-11-11");
    generateTestDataForGetStockCards("P2", true, false, program1, "1970-11-11");
    generateTestDataForGetStockCards("P4", true, false, program3, "1969-11-11");
    DateTime periodBegin = new DateTime(
        DateUtil.parseString("1970-01-01 10:10:10", DateUtil.DATE_TIME_FORMAT));
    DateTime periodEnd = new DateTime(
        DateUtil.parseString("1970-02-21 10:10:10", DateUtil.DATE_TIME_FORMAT));
    RnRForm rnRForm = RnRForm.init(program1, new Period(periodBegin, periodEnd), false);

    // when
    List<StockCard> stockCardsBeforeTimeLine = stockRepository.getStockCardsBeforePeriodEnd(rnRForm);

    // then
    assertThat(stockCardsBeforeTimeLine.size(), is(1));
    assertThat(stockCardsBeforeTimeLine.get(0).getProduct().getCode(), is("P1"));
  }

  @Test
  public void shouldGetActiveAndNotArchivedStockCardsBeforePeriodEndDate() throws Exception {
    // given
    Program program1 = new ProgramBuilder().setProgramCode("code1").build();
    generateTestDataForGetStockCards("P1", true, false, program1, "1969-11-11");
    generateTestDataForGetStockCards("P2", false, false, program1, "1969-11-11");
    DateTime periodBegin = new DateTime(
        DateUtil.parseString("1970-01-01 10:10:10", DateUtil.DATE_TIME_FORMAT));
    DateTime periodEnd = new DateTime(
        DateUtil.parseString("1970-02-21 10:10:10", DateUtil.DATE_TIME_FORMAT));
    RnRForm rnRForm = RnRForm.init(program1, new Period(periodBegin, periodEnd), false);

    // when
    List<StockCard> stockCardsBeforeTimeLine = stockRepository.getStockCardsBeforePeriodEnd(rnRForm);

    // then
    assertEquals(1, stockCardsBeforeTimeLine.size());
    assertEquals("P1", stockCardsBeforeTimeLine.get(0).getProduct().getCode());
  }

  private void generateTestDataForGetStockCards(String productCode, boolean isActive,
      boolean isArchived, Program program, String movementDate) throws LMISException {
    Product product = ProductBuilder.create().setCode(productCode).setIsActive(isActive)
        .setIsArchived(isArchived).build();

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

  private StockCard createNewStockCard(String code, String parentCode, Product product,
      boolean isEmergency) throws LMISException {
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
    ProductProgram productProgram = new ProductProgramBuilder()
        .setProgramCode(code)
        .setProductCode(productCode)
        .setActive(true)
        .setShowInReport(true)
        .build();
    productProgramRepository.createOrUpdate(productProgram);
  }

  @NonNull
  private Program createNewProgram(String code, String parentCode, boolean isSupportEmergency)
      throws LMISException {
    Program program = new ProgramBuilder().setProgramCode(code).setParentCode(parentCode)
        .setSupportEmergency(isSupportEmergency).build();
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
  public void shouldLoadEmergencyProducts() throws Exception {
    //when
    createNewStockCard("code", null,
        ProductBuilder.create().setCode("p1").setIsActive(true).setIsKit(false).build(), true);
    createNewStockCard("otherCode", "parentCode",
        ProductBuilder.create().setCode("p2").setIsActive(true).setIsKit(false).build(), false);

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

  private StockMovementItem createMovementItem(MovementReasonManager.MovementType type,
      long quantity, StockCard stockCard, Date createdTime, Date movementDate, boolean synced)
      throws LMISException {
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
    Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true)
        .setIsKit(false).build();
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

    StockMovementItem queriedStockMovementItem = queriedStockCard.getStockMovementItemsWrapper()
        .get(0);
    assertThat(queriedStockMovementItem.getMovementQuantity(), is(10L));
    assertThat(
        queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getMovementQuantity(),
        is(2L));
    assertThat(
        queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getLot().getLotNumber(),
        is("AAA"));
  }

  @Test
  public void shouldDeleteDataOver13Months() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2016-08-11", DateUtil.DB_DATE_FORMAT).getTime());
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);

    StockMovementItem stockMovementItem = createMovementItem(
        MovementReasonManager.MovementType.POSITIVE_ADJUST, 100L, stockCard, new Date(), new Date(),
        true);

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
    assertEquals(MovementReasonManager.MovementType.POSITIVE_ADJUST,
        stockCardQueried.getStockMovementItemsWrapper().get(0).getMovementType());
    assertEquals(1,
        stockCardQueried.getStockMovementItemsWrapper().get(0).getLotMovementItemListWrapper()
            .size());
  }

  @Test
  public void shouldReturnStockCardProductByCode() throws LMISException {
    StockCard expectedStockCard = new StockCard();
    expectedStockCard.setProduct(product);
    expectedStockCard.setStockOnHand(100);

    stockRepository.saveStockCardAndBatchUpdateMovements(expectedStockCard);
    StockCard actualStockCard = stockRepository.queryStockCardByProductCode(product.getCode());

    assertThat(actualStockCard.getProduct().getCode(),
        is(expectedStockCard.getProduct().getCode()));
  }

  @Test
  public void queryStockCountGroupByStockOnHandStatusTest() throws LMISException {
    // given
    createStockAndProduct(1, 100, -1);
    createStockAndProduct(2, 0, -1);
    createStockAndProduct(3, 100, 101);
    createStockAndProduct(4, 201, 100);

    // when
    final Map<String, Integer> stockOnHandStatusMap = stockRepository
        .queryStockCountGroupByStockOnHandStatus();

    // then
    MatcherAssert.assertThat(stockOnHandStatusMap.get(StockOnHandStatus.REGULAR_STOCK.name()),
        Matchers.is(1));
    MatcherAssert
        .assertThat(stockOnHandStatusMap.get(StockOnHandStatus.LOW_STOCK.name()), Matchers.is(1));
    MatcherAssert
        .assertThat(stockOnHandStatusMap.get(StockOnHandStatus.STOCK_OUT.name()), Matchers.is(1));
    MatcherAssert
        .assertThat(stockOnHandStatusMap.get(StockOnHandStatus.OVER_STOCK.name()), Matchers.is(1));
  }

  @Test
  public void shouldSaveStockMovementWhenAddStockMovementsAndUpdateStockCardsIsCalled()
      throws LMISException {
    // given
    StockMovementItem mockedStockMovementItem = mock(StockMovementItem.class);
    when(mockedStockMovementItem.getStockCard()).thenReturn(createStockAndProduct(100, 10, 5));
    StockMovementRepository mockedStockMovementRepository = mock(StockMovementRepository.class);
    stockRepository.stockMovementRepository = mockedStockMovementRepository;
    doNothing().when(mockedStockMovementRepository)
        .batchCreateStockMovementItemAndLotItems(any(StockMovementItem.class), anyLong());
    // when
    stockRepository.addStockMovementsAndUpdateStockCards(newArrayList(mockedStockMovementItem));
    // then
    verify(mockedStockMovementRepository).batchCreateStockMovementItemAndLotItems(
        eq(mockedStockMovementItem), anyLong());
  }

  @Test
  public void createOrUpdateStockCardsWithCMM_shouldRollBackTransactionIfThereAreSqlExceptions()
      throws LMISException, InterruptedException {
    // given
    StockCard stockCard = createStockAndProduct(1, 100, (int) 100f);

    CmmRepository mockedCmmRepository = mock(CmmRepository.class);
    stockRepository.cmmRepository = mockedCmmRepository;
    doThrow(new LMISException("")).when(mockedCmmRepository).save(any(Cmm.class));
    // when
    try {
      stockRepository.createOrUpdateStockCardsWithCMM(stockCard, Period.of(today()));
    } catch (SQLException e) {
      // then
      verify(mockedCmmRepository).save(any(Cmm.class));

      Thread.sleep(1_000);
      assertEquals(0, stockRepository.list().size());
      return;
    }

    throw new AssertionError();
  }

  @Test
  public void createOrUpdateStockCardsWithCMM_shouldSaveBothStockCardAndCMM()
      throws LMISException, SQLException {
    // given
    float avgMonthlyConsumption = 100f;
    StockCard stockCard = createStockAndProduct(1, 100, (int) avgMonthlyConsumption);

    Period cmmPeriod = Period.of(today());

    CmmRepository mockedCmmRepository = mock(CmmRepository.class);
    stockRepository.cmmRepository = mockedCmmRepository;
    doNothing().when(mockedCmmRepository).save(any(Cmm.class));
    // when
    stockRepository.createOrUpdateStockCardsWithCMM(stockCard, cmmPeriod);
    // then
    verify(mockedCmmRepository).save(Cmm.initWith(stockCard, cmmPeriod));
    assertEqualsWithTimeout(1, () -> stockRepository.list().size(), 2);
  }

  private <T> void assertEqualsWithTimeout(
      T expectedValue, Callable<T> actualValue, long timeoutSeconds
  ) {
    long tempTime = 0L;
    int duration = 100;
    long timeoutMilliSeconds = timeoutSeconds * 1000;

    do {
      try {
        assertEquals(expectedValue, actualValue.call());
        return;
      } catch (AssertionError e) {
        tempTime += duration;
        try {
          Thread.sleep(duration);
        } catch (InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } while (tempTime < timeoutMilliSeconds);
  }

  private StockCard createStockAndProduct(int productId, int stockOnHand, int avg)
      throws LMISException {
    Product product = new Product();
    product.setCode(String.valueOf(productId));
    product.setId(productId);
    product.setActive(true);
    product.setArchived(false);
    productRepository.createOrUpdate(product);

    final StockCard stockCard = new StockCard();
    stockCard.setProduct(product);
    stockCard.setStockOnHand(stockOnHand);
    stockCard.setAvgMonthlyConsumption(avg);
    stockRepository.createOrUpdate(stockCard);
    return stockCard;
  }

  private StockCard saveStockCardWithOneMovement(StockRepository stockRepository,
      ProductRepository productRepository) throws LMISException {
    StockCard stockCard = new StockCard();
    Product product = new Product();
    int random = (int) (Math.random() * 10000000);
    product.setId(random);
    product.setCode(String.valueOf(random));
    Program program = new Program("MMIA", "MMIA", null, false, null, null);
    product.setProgram(program);
    productRepository.createOrUpdate(product);

    stockCard.setProduct(product);
    stockCard.setStockOnHand(90L);
    stockRepository.createOrUpdate(stockCard);

    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setMovementQuantity(10L);
    stockMovementItem.setStockOnHand(100L);
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementItem.setDocumentNumber("XXX123456");
    stockMovementItem.setReason("some reason");
    stockMovementItem.setMovementDate(DateUtil.parseString("2015-11-11", "yyyy-MM-dd"));
    stockMovementItem.setSynced(true);

    stockCard.setStockOnHand(stockMovementItem.getStockOnHand());
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
    stockRepository.refresh(stockCard);

    return stockCard;
  }
}
