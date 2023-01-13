package org.openlmis.core.model.repository;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.RobolectricUtils;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class LotRepositoryTest extends LMISRepositoryUnitTest {

  private LotRepository lotRepository;

  ProductRepository productRepository;
  private StockRepository stockRepository;
  private Product product;
  private StockCard stockCard;


  @Before
  public void setup() throws LMISException {
    stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    lotRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(LotRepository.class);
    product = ProductBuilder.buildAdultProduct();
    productRepository.createOrUpdate(product);
    stockCard = new StockCardBuilder().setProduct(product).setStockOnHand(10L).build();
    stockRepository.createOrUpdate(stockCard);
  }

  @Test
  public void shouldSaveLotAndLotOnHandAndLotMovementForNewAndExistingLots()
      throws ParseException, LMISException {
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

    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withStockOnHand(100)
        .withMovementDate("2016-12-31")
        .withQuantity(10)
        .build();
    stockMovementItem.setStockCard(stockCard);
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);

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
    lotRepository.batchCreateLotsAndLotMovements(
        newArrayList(lotMovementItem, lotMovementItem2, lotMovementItem3));

    assertThat(
        lotRepository.getLotByLotNumberAndProductId("AAA", product.getId()).getExpirationDate(),
        is(lot1.getExpirationDate()));
    assertThat(
        lotRepository.getLotByLotNumberAndProductId("BBB", product.getId()).getExpirationDate(),
        is(lot2.getExpirationDate()));
    assertThat(
        lotRepository.getLotByLotNumberAndProductId("CCC", product.getId()).getExpirationDate(),
        is(lot3.getExpirationDate()));
    assertThat(lotRepository.getLotOnHandByLot(lot1).getQuantityOnHand(), is(2L));
    assertThat(lotRepository.getLotOnHandByLot(lot2).getQuantityOnHand(), is(3L));
    assertThat(lotRepository.getLotOnHandByLot(lot3).getQuantityOnHand(), is(5L));

    StockMovementItem stockMovementItem2 = new StockMovementItemBuilder()
        .withStockOnHand(100)
        .withMovementDate("2016-12-31")
        .withQuantity(10)
        .build();
    stockMovementItem.setStockCard(stockCard);
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);

    LotMovementItem lotMovementItem4 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem2)
        .setLot(lot1)
        .setMovementQuantity(-1L).build();

    LotMovementItem lotMovementItem5 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem2)
        .setLot(lot2)
        .setMovementQuantity(-3L).build();

    LotMovementItem lotMovementItem6 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem2)
        .setLot(lot3)
        .setMovementQuantity(-2L).build();

    lotRepository.batchCreateLotsAndLotMovements(
        newArrayList(lotMovementItem4, lotMovementItem5, lotMovementItem6));

    assertThat(lotRepository.getLotOnHandByLot(lot1).getQuantityOnHand(), is(1L));
    assertThat(lotRepository.getLotOnHandByLot(lot2).getQuantityOnHand(), is(0L));
    assertThat(lotRepository.getLotOnHandByLot(lot3).getQuantityOnHand(), is(3L));
  }

  @Test
  public void shouldCreateOrUpdateLotsInformation() throws Exception {
    Lot lot1 = new Lot();
    lot1.setProduct(product);
    lot1.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot1.setLotNumber("AAA");

    LotOnHand lotOnHand1 = new LotOnHand();
    lotOnHand1.setLot(lot1);
    lotOnHand1.setStockCard(stockCard);
    lotOnHand1.setQuantityOnHand(10L);

    Lot lot2 = new Lot();
    lot2.setProduct(product);
    lot2.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot2.setLotNumber("BBB");

    LotOnHand lotOnHand2 = new LotOnHand();
    lotOnHand2.setLot(lot2);
    lotOnHand2.setStockCard(stockCard);
    lotOnHand2.setQuantityOnHand(20L);

    List<LotOnHand> lotOnHandList = Arrays.asList(lotOnHand1, lotOnHand2);

    lotRepository.createOrUpdateLotsInformation(lotOnHandList);

    assertThat(lotRepository.getLotOnHandByLot(lot1).getQuantityOnHand(),
        is(lotOnHand1.getQuantityOnHand()));
    assertThat(lotRepository.getLotOnHandByLot(lot2).getQuantityOnHand(),
        is(lotOnHand2.getQuantityOnHand()));
  }

  @Test
  public void shouldCreateLotMovementItem() throws Exception {
    Lot lot1 = new Lot();
    lot1.setProduct(product);
    lot1.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot1.setLotNumber("AAA");

    LotOnHand lotOnHand1 = new LotOnHand();
    lotOnHand1.setLot(lot1);
    lotOnHand1.setStockCard(stockCard);
    lotOnHand1.setQuantityOnHand(10L);

    List<LotOnHand> lotOnHandList = Arrays.asList(lotOnHand1);
    lotRepository.createOrUpdateLotsInformation(lotOnHandList);

    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withStockOnHand(100)
        .withMovementType(MovementReasonManager.MovementType.RECEIVE)
        .withMovementDate("2016-12-31")
        .withQuantity(10)
        .build();
    stockMovementItem.setStockCard(stockCard);
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);

    LotMovementItem lotMovementItem = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot1)
        .setMovementQuantity(2L)
        .setStockOnHand(lotOnHand1.getQuantityOnHand() + 2L)
        .build();

    lotRepository.createLotMovementItem(lotMovementItem);
    assertEquals(1L, lotMovementItem.getId());

    StockCard stockCardQueried = stockRepository.queryStockCardById(stockCard.getId());
    StockMovementItem queriedStockMovementItem = stockCardQueried.getStockMovementItemsWrapper()
        .get(stockCardQueried.getStockMovementItemsWrapper().size() - 1);
    assertThat(
        queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getMovementQuantity(),
        is(2L));
    assertThat(queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getId(),
        is(lotMovementItem.getId()));
    assertThat(
        queriedStockMovementItem.getLotMovementItemListWrapper().get(0).getLot().getLotNumber(),
        is(lot1.getLotNumber()));
  }

  @Test
  public void shouldSaveLotIgnoreLotNumberCase() throws Exception {
    Lot lot1 = new Lot();
    lot1.setLotNumber("ABC");
    lot1.setExpirationDate(new Date());
    lot1.setProduct(product);
    lotRepository.createOrUpdateLot(lot1);

    Lot lowerCaseLot = lotRepository.getLotByLotNumberAndProductId("abc", product.getId());
    Lot upperCaseLot = lotRepository.getLotByLotNumberAndProductId("ABC", product.getId());

    assertEquals(lowerCaseLot, upperCaseLot);

  }

  @Test
  public void shouldGetLotNumberAndProductIdToLot() throws LMISException {
    product.setId(1L);

    Lot lot = new Lot();
    lot.setLotNumber("ABC");
    lot.setProduct(product);

    lotRepository.createOrUpdateLot(lot);
    Map<String, Lot> lotNumberAndProductIdToLot = lotRepository.getLotNumberAndProductIdToLot();

    assertNotNull(lotNumberAndProductIdToLot);
  }

  @Test
  public void shouldDeleteLotInfo() throws LMISException {
    String lotNumber = "ABC";
    long productId = 1L;
    product.setId(productId);


    Lot lot = new Lot();
    lot.setLotNumber(lotNumber);
    lot.setProduct(product);

    stockCard.setId(10L);
    stockCard.setProduct(product);

    lotRepository.createOrUpdateLot(lot);
    Lot shouldBeEmptyLot = lotRepository.getLotByLotNumberAndProductId(lotNumber, productId);
    assertNotNull(shouldBeEmptyLot);

    lotRepository.deleteLotInfo(stockCard);
    shouldBeEmptyLot = lotRepository.getLotByLotNumberAndProductId(lotNumber, productId);
    assertNull(shouldBeEmptyLot);
  }

  @Test
  public void shouldGetLotNumberAndProductIdToLotAndResetKeepLotMovementItems() throws LMISException {
    //given
    Lot lot1 = new Lot();
    lot1.setProduct(product);
    lot1.setExpirationDate(DateUtil.parseString("2017-12-31", DateUtil.DB_DATE_FORMAT));
    lot1.setLotNumber("AAA");

    LotOnHand lotOnHand1 = new LotOnHand();
    lotOnHand1.setLot(lot1);
    lotOnHand1.setStockCard(stockCard);
    lotOnHand1.setQuantityOnHand(10L);

    List<LotOnHand> lotOnHandList = Arrays.asList(lotOnHand1);
    lotRepository.createOrUpdateLotsInformation(lotOnHandList);

    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withStockOnHand(100)
        .withMovementType(MovementReasonManager.MovementType.RECEIVE)
        .withMovementDate("2016-12-31")
        .withQuantity(10)
        .build();
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setId(100L);
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);

    LotMovementItem lotMovementItem = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem)
        .setLot(lot1)
        .setMovementQuantity(2L)
        .setStockOnHand(lotOnHand1.getQuantityOnHand() + 2L)
        .build();
    lotRepository.createLotMovementItem(lotMovementItem);

    Map<String, Lot> expectedMap = new HashMap<>();
    expectedMap.put(lot1.getLotNumber() + lot1.getProduct().getId(), lot1);

    //when
    Map<String, Lot> lotNumberAndProductIdToLot = lotRepository.getLotNumberAndProductIdToLot();

    //then
    assertEquals(1, lotNumberAndProductIdToLot.size());
    assertEquals(expectedMap, lotNumberAndProductIdToLot);

    //when
    List<StockMovementItem> toRestStockMovementItems = new ArrayList<>();
    toRestStockMovementItems.add(stockMovementItem);
    Map<String, List<StockMovementItem>> stockMovementItemsMap = new HashMap<>();
    stockMovementItemsMap.put("UnusedStringKey", toRestStockMovementItems);

    List<LotMovementItem> shouldBeEmptyItems = lotRepository.resetKeepLotMovementItems(new HashMap<>());
    List<LotMovementItem> resetLotMovementItems = lotRepository.resetKeepLotMovementItems(stockMovementItemsMap);
    RobolectricUtils.waitLooperIdle();

    //then
    assertEquals(0, shouldBeEmptyItems.size());
    assertNotNull(resetLotMovementItems);
  }
}