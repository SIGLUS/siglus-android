package org.openlmis.core.model.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;
import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import androidx.annotation.NonNull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotBuilder;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class InventoryRepositoryTest {

  private InventoryRepository repository;
  private InventoryRepository repositoryWithMockDao;
  private StockRepository stockRepository;
  private ProgramRepository programRepository;
  private ProductRepository productRepository;
  private ProductProgramRepository productProgramRepository;
  @Mock
  GenericDao<Inventory> inventoryDaoMock;

  @Before
  public void setup() throws LMISException {
    MockitoAnnotations.initMocks(this);
    repository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(InventoryRepository.class);
    repositoryWithMockDao = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(InventoryRepository.class);
    repositoryWithMockDao.genericDao = inventoryDaoMock;
    stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockRepository.class);
    programRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductProgramRepository.class);
  }

  @Test
  public void shouldQueryPeriodInventory() throws Exception {
    repository
        .save(getInventory(DateUtil.parseString("2016-01-22 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    repository
        .save(getInventory(DateUtil.parseString("2016-02-01 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    repository
        .save(getInventory(DateUtil.parseString("2016-02-18 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    repository
        .save(getInventory(DateUtil.parseString("2016-02-22 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    repository
        .save(getInventory(DateUtil.parseString("2016-02-25 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    repository
        .save(getInventory(DateUtil.parseString("2016-02-26 11:33:44", DateUtil.DATE_TIME_FORMAT)));

    List<Inventory> inventories = repository
        .queryPeriodInventory(new Period(new DateTime("2016-01-21"), new DateTime("2016-02-20")));

    assertThat(inventories.size(), is(3));
    assertThat(inventories.get(0).getCreatedAt(),
        is(DateUtil.parseString("2016-02-25 11:33:44", DateUtil.DATE_TIME_FORMAT)));
  }

  @Test
  public void shouldSaveDraftInventoryAndDraftLotItem() throws LMISException, ParseException {
    Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true)
        .setIsKit(false).build();
    StockCard stockCard = createNewStockCard("code", null, product, true);

    StockMovementItem stockMovementItem1 = new StockMovementItemBuilder()
        .withMovementDate("2016-10-10").withMovementType(RECEIVE).build();
    Lot lot1 = new LotBuilder().setLotNumber("A111").setProduct(stockCard.getProduct()).build();
    LotMovementItem lotMovementItem1 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem1).setLot(lot1).setMovementQuantity(100L)
        .setStockOnHand(0L).build();
    stockMovementItem1.setLotMovementItemListWrapper(newArrayList(lotMovementItem1));
    stockMovementItem1.setStockCard(stockCard);
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem1);

    PhysicalInventoryViewModel draftInventory = new PhysicalInventoryViewModel(stockCard, 20L);
    draftInventory.setStockCard(stockCard);
    LotMovementViewModel draftLotItem = new LotMovementViewModel();
    draftLotItem.setLotNumber("A111");
    draftLotItem.setExpiryDate("2015-02-28");
    draftLotItem.setQuantity("20");
    draftInventory.setExistingLotMovementViewModelList(Arrays.asList(draftLotItem));

    repository.createDraftInventory(Arrays.asList(draftInventory));
    DraftInventory draftInventoryQueried = repository.queryAllDraft().get(0);

    assertEquals("A111", draftInventoryQueried.getDraftLotItemListWrapper().get(0).getLotNumber());
    assertEquals(product, draftInventoryQueried.getDraftLotItemListWrapper().get(0).getProduct());
    assertEquals("2015-02-28",
        DateUtil.formatDate(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getExpirationDate(),
            DB_DATE_FORMAT));
    assertEquals(product, draftInventoryQueried.getStockCard().getProduct());
    assertFalse(draftInventoryQueried.getDraftLotItemListWrapper().get(0).isNewAdded());

    assertEquals(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getQuantity(), Long.valueOf(20));
  }

  @Test
  public void shouldDoNothingIfStockCardIsNullOrEmpty() throws LMISException {
    // when
    repository.recoverInventoryFormStockCard(null);
    repository.recoverInventoryFormStockCard(new ArrayList<>());

    // then
    verify(inventoryDaoMock, Mockito.never()).queryForAll();
  }

  @Test
  public void shouldNotRecoverInventoryForSameDate() throws LMISException {
    // given
    ArrayList<Inventory> existInventoryList = new ArrayList<>();
    Inventory existInventory = new Inventory();
    String movementDate = "2022-02-02";
    existInventory.setCreatedAt(DateUtil.parseString(movementDate, DB_DATE_FORMAT));
    existInventory.setUpdatedAt(DateUtil.parseString(movementDate, DB_DATE_FORMAT));
    existInventoryList.add(existInventory);
    when(inventoryDaoMock.queryForAll()).thenReturn(existInventoryList);
    Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true)
        .setIsKit(false).build();
    StockCard stockCard = createNewStockCard("code", null, product, false);
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate(movementDate).withMovementType(PHYSICAL_INVENTORY).build();
    stockCard.getStockMovementItemsWrapper().add(stockMovementItem);
    ArrayList<StockCard> stockCards = new ArrayList<>();
    stockCards.add(stockCard);

    // when
    repositoryWithMockDao.recoverInventoryFormStockCard(stockCards);

    // then
    Mockito.verify(inventoryDaoMock, Mockito.times(1)).queryForAll();
    Mockito.verify(inventoryDaoMock, Mockito.times(1)).create(new ArrayList<>());
  }

  @Test
  public void shouldRecoverInventoryForDifferentDate() throws LMISException {
    // given
    Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true)
        .setIsKit(false).build();
    StockCard stockCard = createNewStockCard("code", null, product, false);
    String movementDate = "2022-02-02";
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate(movementDate).withMovementType(PHYSICAL_INVENTORY).build();
    stockCard.getStockMovementItemsWrapper().add(stockMovementItem);
    ArrayList<StockCard> stockCards = new ArrayList<>();
    stockCards.add(stockCard);

    Inventory recoverInventory = new Inventory();
    recoverInventory.setCreatedAt(DateUtil.parseString(movementDate, DB_DATE_FORMAT));
    recoverInventory.setUpdatedAt(DateUtil.parseString(movementDate, DB_DATE_FORMAT));
    ArrayList<Inventory> recoverInventoryList = new ArrayList<>();
    recoverInventoryList.add(recoverInventory);

    // when
    repositoryWithMockDao.recoverInventoryFormStockCard(stockCards);

    // then
    Mockito.verify(inventoryDaoMock, Mockito.times(1)).queryForAll();
    Mockito.verify(inventoryDaoMock, Mockito.times(1)).create(recoverInventoryList);
  }

  @Test
  public void shouldSaveInitialDraftInventoryAndDraftLotItem()
      throws LMISException, ParseException {
    Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true)
        .setIsKit(false).build();
    StockCard stockCard = createNewStockCard("code", null, product, true);

    StockMovementItem stockMovementItem1 = new StockMovementItemBuilder()
        .withMovementDate("2016-10-10").withMovementType(RECEIVE).build();
    Lot lot1 = new LotBuilder().setLotNumber("A111").setProduct(stockCard.getProduct()).build();
    LotMovementItem lotMovementItem1 = new LotMovementItemBuilder()
        .setStockMovementItem(stockMovementItem1).setLot(lot1).setMovementQuantity(100L)
        .setStockOnHand(0L).build();
    stockMovementItem1.setLotMovementItemListWrapper(newArrayList(lotMovementItem1));
    stockMovementItem1.setStockCard(stockCard);
    stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem1);

    DraftInitialInventory draftInitialInventory = new DraftInitialInventory();
    DraftInitialInventoryLotItem draftInitialInventoryLotItem = new DraftInitialInventoryLotItem();
    draftInitialInventoryLotItem.setProduct(product);
    draftInitialInventoryLotItem.setLotNumber("A111");
    draftInitialInventoryLotItem.setExpirationDate(new Date());
    draftInitialInventoryLotItem.setDraftInitialInventory(draftInitialInventory);
    draftInitialInventoryLotItem.setQuantity(20L);
    draftInitialInventory.setDraftLotItemListWrapper(newArrayList(draftInitialInventoryLotItem));
    draftInitialInventory.setQuantity(20L);
    draftInitialInventory.setExpireDates("Feb 2015");

    repository.createInitialDraft(draftInitialInventory);
    DraftInitialInventory draftInventoryQueried = repository.queryAllInitialDraft().get(0);

    assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getLotNumber(),
        is("A111"));
    assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getProduct(), is(product));
    assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getExpirationDate(),
        is(DateUtil.parseString(DateUtil.formatDate(new Date(), DB_DATE_FORMAT),
            DB_DATE_FORMAT)));
    assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getQuantity(), is(20L));
  }

  @Test
  public void shouldClearInitialDraftInventory() throws Exception {
    saveInitialDraftInventory();
    Assert.assertThat(repository.queryAllInitialDraft().size(), Is.is(2));
    repository.clearInitialDraft();
    Assert.assertThat(repository.queryAllInitialDraft().size(), Is.is(0));
  }

  @Test
  public void shouldClearDraftInventory() throws Exception {
    saveDraftInventory();
    Assert.assertThat(repository.queryAllDraft().size(), Is.is(2));
    repository.clearDraft();
    Assert.assertThat(repository.queryAllDraft().size(), Is.is(0));
  }

  @NonNull
  private Inventory getInventory(Date date) {
    Inventory inventory = new Inventory();
    inventory.setCreatedAt(date);
    inventory.setUpdatedAt(date);
    return inventory;
  }


  private void saveInitialDraftInventory() throws LMISException {
    Product product = ProductBuilder.create().setIsBasic(true).setCode("basicCode").setProductId(1L)
        .setPrimaryName("basicName").build();
    DraftInitialInventory draftInitialInventory = new DraftInitialInventory();
    draftInitialInventory.setQuantity(13L);
    draftInitialInventory.setProduct(product);
    draftInitialInventory.setExpireDates("11/10/2020");

    Product product1 = ProductBuilder.create().setIsBasic(true).setCode("basicCode2")
        .setProductId(2L).setPrimaryName("basicName2").build();
    DraftInitialInventory draftInitialInventory1 = new DraftInitialInventory();
    draftInitialInventory1.setQuantity(33L);
    draftInitialInventory.setProduct(product1);
    draftInitialInventory1.setExpireDates("11/12/2020");

    repository.createInitialDraft(draftInitialInventory);
    repository.createInitialDraft(draftInitialInventory1);
  }

  private void saveDraftInventory() throws LMISException {
    Product product = new Product();
    StockCard stockCard = new StockCard();
    stockCard.setProduct(product);
    PhysicalInventoryViewModel viewModel = new PhysicalInventoryViewModel(stockCard, 10L);
    StockCard stockCard2 = new StockCard();
    stockCard2.setProduct(product);
    PhysicalInventoryViewModel viewModel2 = new PhysicalInventoryViewModel(stockCard2, 20L);
    repository.createDraftInventory(Arrays.asList(viewModel, viewModel2));
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

  @NonNull
  private Program createNewProgram(String code, String parentCode, boolean isSupportEmergency)
      throws LMISException {
    Program program = new ProgramBuilder().setProgramCode(code).setParentCode(parentCode)
        .setSupportEmergency(isSupportEmergency).build();
    programRepository.createOrUpdate(program);
    return program;
  }
}