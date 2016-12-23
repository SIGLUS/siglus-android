package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;
import static org.openlmis.core.model.builder.StockCardBuilder.saveStockCardWithOneMovement;

@RunWith(LMISTestRunner.class)
public class StockMovementRepositoryTest {


    StockRepository stockRepository;
    StockMovementRepository stockMovementRepository;

    StockMovementItem stockMovementItem;

    @Before
    public void setup() throws LMISException {
        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        stockMovementRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockMovementRepository.class);

        stockMovementItem = new StockMovementItem();
    }

    @Test
    public void shouldListUnSyncedStockMovementItems() throws LMISException, ParseException {
        //given one movement was saved but NOT SYNCED
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);
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
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);
        createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard, DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
        createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard, DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
        createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard, DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

        List<StockMovementItem> stockMovementItems = stockMovementRepository.listLastFiveStockMovements(stockCard.getId());
        assertEquals(4, stockMovementItems.size());
        assertEquals(stockCard.getStockMovementItemsWrapper().get(0), stockMovementItems.get(0));
        assertEquals(stockCard.getStockMovementItemsWrapper().get(1), stockMovementItems.get(1));
        assertEquals(stockCard.getStockMovementItemsWrapper().get(2), stockMovementItems.get(2));
        assertEquals(stockCard.getStockMovementItemsWrapper().get(3), stockMovementItems.get(3));
    }

    @Test
    public void shouldQueryStockMovementsByTimeRange() throws Exception {
        StockCard stockCard = saveStockCardWithOneMovement(stockRepository);
        createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1000, stockCard, DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), false);
        createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1001, stockCard, DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT), false);
        createMovementItem(MovementReasonManager.MovementType.PHYSICAL_INVENTORY, 1002, stockCard, DateUtil.parseString("2015-12-14", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-13", DateUtil.DB_DATE_FORMAT), false);

        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockMovementsByTimeRange(stockCard.getId(), DateUtil.parseString("2015-12-11", DateUtil.DB_DATE_FORMAT), DateUtil.parseString("2015-12-12", DateUtil.DB_DATE_FORMAT));
        assertEquals(2, stockMovementItems.size());
        assertEquals(stockCard.getStockMovementItemsWrapper().get(1), stockMovementItems.get(0));
        assertEquals(stockCard.getStockMovementItemsWrapper().get(2), stockMovementItems.get(1));
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
}