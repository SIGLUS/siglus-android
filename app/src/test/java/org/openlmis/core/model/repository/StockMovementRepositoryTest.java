package org.openlmis.core.model.repository;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.ISSUE;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;
import static org.openlmis.core.model.builder.StockCardBuilder.saveStockCardWithOneMovement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class StockMovementRepositoryTest {


  StockRepository stockRepository;
  StockMovementRepository stockMovementRepository;

  StockMovementItem stockMovementItem;
  private ProgramRepository programRepository;
  private ProductRepository productRepository;
  private ProductProgramRepository productProgramRepository;

  Product product = ProductBuilder.buildAdultProduct();

  @Before
  public void setup() throws LMISException {
    stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockRepository.class);
    stockMovementRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockMovementRepository.class);
    programRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductProgramRepository.class);

    stockMovementItem = new StockMovementItem();
  }

  @Test
  public void shouldListUnSyncedStockMovementItems() throws LMISException, ParseException {
    //given one movement was saved but NOT SYNCED
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    assertThat(stockCard.getForeignStockMovementItems().size(), is(1));
    assertThat(stockMovementRepository.listUnSynced().size(), is(1));

    //when save another SYNCED movement
    createMovementItem(RECEIVE, 100, stockCard, new Date(), DateUtil.today(), true);

    //then
    assertThat(stockCard.getForeignStockMovementItems().size(), is(2));
    assertThat(stockMovementRepository.listUnSynced(), notNullValue());
    assertThat(stockMovementRepository.listUnSynced().size(), is(1));
  }


  @Test
  public void shouldListLastFiveStockMovementsOrderByDateInDescOrder() throws Exception {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard,
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard,
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard,
        DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .listLastFiveStockMovements(stockCard.getId());
    assertEquals(4, stockMovementItems.size());
    assertEquals(stockCard.getStockMovementItemsWrapper().get(0), stockMovementItems.get(0));
    assertEquals(stockCard.getStockMovementItemsWrapper().get(1), stockMovementItems.get(1));
    assertEquals(stockCard.getStockMovementItemsWrapper().get(2), stockMovementItems.get(2));
    assertEquals(stockCard.getStockMovementItemsWrapper().get(3), stockMovementItems.get(3));
  }

  @Test
  public void shouldQueryStockMovementsByMovementDate() throws Exception {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard,
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard,
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard,
        DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .queryStockMovementsByMovementDate(stockCard.getId(),
            DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT),
            DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT));
    assertEquals(2, stockMovementItems.size());
    assertEquals(stockCard.getStockMovementItemsWrapper().get(1), stockMovementItems.get(0));
    assertEquals(stockCard.getStockMovementItemsWrapper().get(2), stockMovementItems.get(1));
  }

  @Test
  public void shouldQueryStockMovementsByCreatedDate() throws Exception {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard,
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard,
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard,
        DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .queryStockItemsByCreatedDate(stockCard.getId(),
            DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT),
            DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT));
    assertEquals(1, stockMovementItems.size());
    assertEquals(stockCard.getStockMovementItemsWrapper().get(1), stockMovementItems.get(0));
  }

  @Test
  public void shouldQueryStockMovementHistory() throws Exception {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard,
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard,
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard,
        DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .queryStockMovementHistory(stockCard.getId(), 1, 1);
    assertEquals(1, stockMovementItems.size());
    assertEquals(stockCard.getStockMovementItemsWrapper().get(1), stockMovementItems.get(0));

    List<StockMovementItem> stockMovementItems1 = stockMovementRepository
        .queryStockMovementHistory(stockCard.getId(), 1, 3);
    assertEquals(3, stockMovementItems1.size());
    assertEquals(stockCard.getStockMovementItemsWrapper().get(3), stockMovementItems1.get(2));
  }

  @Test
  public void shouldGetFirstStockMovement() throws Exception {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard,
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard,
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
    createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard,
        DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT),
        DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

    StockMovementItem stockMovementItem = stockMovementRepository
        .queryFirstStockMovementByStockCardId(stockCard.getId());
    assertEquals(stockCard.getStockMovementItemsWrapper().get(0), stockMovementItem);
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
    mmiaProductProgram.setProductCode(mmiaProduct.getCode());
    mmiaProductProgram.setProgramCode(mmia.getProgramCode());
    mmiaProductProgram.setActive(true);
    productPrograms.add(mmiaProductProgram);

    productProgramRepository.batchSave(viaProduct, productPrograms);

    StockCard stockCard = new StockCard();
    stockCard.setProduct(mmiaProduct);
    stockRepository.createOrUpdate(stockCard);
    StockCard stockCard2 = new StockCard();
    stockCard2.setProduct(viaProduct);
    stockRepository.createOrUpdate(stockCard2);

    createMovementItem(ISSUE, 100, stockCard, new DateTime("2017-01-01").toDate(),
        new DateTime("2017-01-01").toDate(), false);
    createMovementItem(ISSUE, 100, stockCard2, new DateTime("2018-01-01").toDate(),
        new DateTime("2018-01-01").toDate(), false);
    DateTime dateTime = new DateTime("2018-01-01");
    Date expectedDate = new DateTime()
        .withDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth()).toDate();
    createMovementItem(ISSUE, 100, stockCard2, new DateTime("2018-03-02").toDate(),
        new DateTime("2018-03-02").toDate(), false);

    Date earliestDate = stockMovementRepository
        .queryEarliestStockMovementDateByProgram(via.getProgramCode());
    Assert.assertThat(DateUtil.cutTimeStamp(new DateTime(earliestDate)),
        is(DateUtil.cutTimeStamp(new DateTime(expectedDate))));
  }

  @Test
  public void shouldGetFirstMovement() throws Exception {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);
    StockCard stockCard2 = saveStockCardWithOneMovement(stockRepository, productRepository);

    createMovementItem(ISSUE, 100, stockCard, new DateTime("2017-01-01").toDate(),
        new DateTime("2017-01-01").toDate(), false);
    createMovementItem(ISSUE, 100, stockCard2, new DateTime("2016-01-01").toDate(),
        new DateTime("2016-01-01").toDate(), false);
    createMovementItem(ISSUE, 100, stockCard2, new DateTime("2018-03-02").toDate(),
        new DateTime("2018-03-02").toDate(), false);

    StockMovementItem stockMovementItem = stockMovementRepository.getFirstStockMovement();
    assertTrue(stockMovementItem.getMovementDate().before(new DateTime("2016-01-01").toDate()));
  }

  @Test
  public void shouldBatchUpdateStockMovements() throws LMISException, ParseException {
    StockCard stockCard = saveStockCardWithOneMovement(stockRepository, productRepository);

    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementQuantity(100);
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);
    stockMovementItem.setMovementDate(DateUtil.parseString("2016-03-11", DateUtil.DB_DATE_FORMAT));
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setSynced(false);

    Lot lot1 = new Lot();
    lot1.setProduct(product);
    lot1.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot1.setLotNumber("AAA");

    Lot lot2 = new Lot();
    lot2.setProduct(product);
    lot2.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot2.setLotNumber("BBB");

    Lot lot3 = new Lot();
    lot3.setProduct(product);
    lot3.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot3.setLotNumber("CCC");

    LotMovementItem lotMovementItem = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot1)
        .setMovementQuantity(2L).build();

    LotMovementItem lotMovementItem2 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot2)
        .setMovementQuantity(3L).build();

    LotMovementItem lotMovementItem3 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot3)
        .setMovementQuantity(5L).build();

    stockMovementItem.setLotMovementItemListWrapper(
        Lists.newArrayList(lotMovementItem, lotMovementItem2, lotMovementItem3));
    stockMovementItem.setStockOnHand(stockCard.getStockOnHand() + 100);
    stockCard.setStockOnHand(stockMovementItem.getStockOnHand());

    stockMovementRepository
        .batchCreateOrUpdateStockMovementsAndLotInfo(Lists.newArrayList(stockMovementItem));

    stockRepository.refresh(stockCard);

    StockMovementItem stockMovementItem1 = stockCard.getStockMovementItemsWrapper().get(1);
    stockMovementItem1.getLotMovementItemListWrapper();
    stockMovementItem1.setForeignLotMovementItems(null);
    assertEquals(3, stockMovementItem.getLotMovementItemListWrapper().size());
    assertEquals(stockMovementItem.getStockOnHand(), stockMovementItem1.getStockOnHand());
    assertEquals(stockMovementItem.getMovementDate(), stockMovementItem1.getMovementDate());

    stockMovementItem.setSynced(true);
    stockMovementRepository
        .batchCreateOrUpdateStockMovementsAndLotInfo(Lists.newArrayList(stockMovementItem));

    stockRepository.refresh(stockCard);
    stockCard.setStockMovementItemsWrapper(null);
    assertTrue(stockCard.getStockMovementItemsWrapper().get(1).isSynced());
  }
}