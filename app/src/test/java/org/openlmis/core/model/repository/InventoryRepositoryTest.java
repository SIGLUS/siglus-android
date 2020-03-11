package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
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
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class InventoryRepositoryTest {

    private InventoryRepository repository;
    private StockRepository stockRepository;
    private ProgramRepository programRepository;
    private ProductRepository productRepository;
    private ProductProgramRepository productProgramRepository;

    @Before
    public void setup() throws LMISException {
        repository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(InventoryRepository.class);
        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        programRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductProgramRepository.class);
    }

    @Test
    public void shouldQueryPeriodInventory() throws Exception {
        repository.save(getInventory(DateUtil.parseString("2016-01-22 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-01 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-18 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-22 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-25 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-26 11:33:44", DateUtil.DATE_TIME_FORMAT)));

        List<Inventory> inventories = repository.queryPeriodInventory(new Period(new DateTime("2016-01-21"), new DateTime("2016-02-20")));

        assertThat(inventories.size(), is(3));
        assertThat(inventories.get(0).getCreatedAt(), is(DateUtil.parseString("2016-02-25 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    }

    @Test
    public void shouldSaveDraftInventoryAndDraftLotItem() throws LMISException, ParseException {
        Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true).setIsKit(false).build();
        StockCard stockCard = createNewStockCard("code", null, product, true);

        StockMovementItem stockMovementItem1 = new StockMovementItemBuilder().withMovementDate("2016-10-10").withMovementType(RECEIVE).build();
        Lot lot1 = new LotBuilder().setLotNumber("A111").setProduct(stockCard.getProduct()).build();
        LotMovementItem lotMovementItem1 = new LotMovementItemBuilder().setStockMovementItem(stockMovementItem1).setLot(lot1).setMovementQuantity(100L).setStockOnHand(0L).build();
        stockMovementItem1.setLotMovementItemListWrapper(newArrayList(lotMovementItem1));
        stockMovementItem1.setStockCard(stockCard);
        stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem1);

        DraftInventory draftInventory = new DraftInventory();
        draftInventory.setStockCard(stockCard);
        DraftLotItem draftLotItem = new DraftLotItem();
        draftLotItem.setProduct(product);
        draftLotItem.setLotNumber("A111");
        draftLotItem.setExpirationDate(new Date());
        draftLotItem.setDraftInventory(draftInventory);
        draftLotItem.setNewAdded(false);
        draftLotItem.setQuantity(20L);
        draftInventory.setDraftLotItemListWrapper(newArrayList(draftLotItem));
        draftInventory.setQuantity(20L);
        draftInventory.setExpireDates("Feb 2015");

        repository.createDraft(draftInventory);
        DraftInventory draftInventoryQueried = repository.queryAllDraft().get(0);

        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getLotNumber(), is("A111"));
        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getProduct(), is(product));
        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getExpirationDate(), is(DateUtil.parseString(DateUtil.formatDate(new Date(), DateUtil.DB_DATE_FORMAT), DateUtil.DB_DATE_FORMAT)));
        assertThat(draftInventoryQueried.getStockCard().getProduct(), is(product));
        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).isNewAdded(), is(false));
        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getQuantity(), is(20L));
    }


    @Test
    public void shouldSaveInitialDraftInventoryAndDraftLotItem() throws LMISException, ParseException {
        Product product = ProductBuilder.create().setProductId(1L).setCode("p1").setIsActive(true).setIsKit(false).build();
        StockCard stockCard = createNewStockCard("code", null, product, true);

        StockMovementItem stockMovementItem1 = new StockMovementItemBuilder().withMovementDate("2016-10-10").withMovementType(RECEIVE).build();
        Lot lot1 = new LotBuilder().setLotNumber("A111").setProduct(stockCard.getProduct()).build();
        LotMovementItem lotMovementItem1 = new LotMovementItemBuilder().setStockMovementItem(stockMovementItem1).setLot(lot1).setMovementQuantity(100L).setStockOnHand(0L).build();
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

        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getLotNumber(), is("A111"));
        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getProduct(), is(product));
        assertThat(draftInventoryQueried.getDraftLotItemListWrapper().get(0).getExpirationDate(), is(DateUtil.parseString(DateUtil.formatDate(new Date(), DateUtil.DB_DATE_FORMAT), DateUtil.DB_DATE_FORMAT)));
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
        Product product = ProductBuilder.create().setIsBasic(true).setCode("basicCode").setProductId(1L).setPrimaryName("basicName").build();
        DraftInitialInventory draftInitialInventory = new DraftInitialInventory();
        draftInitialInventory.setQuantity(13L);
        draftInitialInventory.setProduct(product);
        draftInitialInventory.setExpireDates("11/10/2020");

        Product product1 = ProductBuilder.create().setIsBasic(true).setCode("basicCode2").setProductId(2L).setPrimaryName("basicName2").build();
        DraftInitialInventory draftInitialInventory1 = new DraftInitialInventory();
        draftInitialInventory1.setQuantity(33L);
        draftInitialInventory.setProduct(product1);
        draftInitialInventory1.setExpireDates("11/12/2020");

        repository.createInitialDraft(draftInitialInventory);
        repository.createInitialDraft(draftInitialInventory1);
    }

    private void saveDraftInventory() throws LMISException {
        DraftInventory draftInventory1 = new DraftInventory();
        draftInventory1.setQuantity(10L);
        draftInventory1.setExpireDates("11/10/2015");
        DraftInventory draftInventory2 = new DraftInventory();
        draftInventory2.setQuantity(20L);
        draftInventory2.setExpireDates("12/10/2015");

        repository.createDraft(draftInventory1);
        repository.createDraft(draftInventory2);
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

    @NonNull
    private Program createNewProgram(String code, String parentCode, boolean isSupportEmergency) throws LMISException {
        Program program = new ProgramBuilder().setProgramCode(code).setParentCode(parentCode).setSupportEmergency(isSupportEmergency).build();
        programRepository.createOrUpdate(program);
        return program;
    }
}