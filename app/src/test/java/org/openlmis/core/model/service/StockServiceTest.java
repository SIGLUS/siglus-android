package org.openlmis.core.model.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;

import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.openlmis.core.model.StockMovementItem.MovementType.ISSUE;
import static org.openlmis.core.model.StockMovementItem.MovementType.RECEIVE;

@RunWith(LMISTestRunner.class)
public class StockServiceTest extends LMISRepositoryUnitTest {

    StockService stockService;

    private Date lastFirstMonthDate;
    private Date lastSecondMonthDate;
    private Date lastThirdMonthDate;
    private Date lastForthMonthDate;
    private StockCard stockCard;

    @Before
    public void setup() throws LMISException {
        stockService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockService.class);

        Date today = DateUtil.today();
        lastFirstMonthDate = DateUtil.generatePreviousMonthDateBy(today);
        lastSecondMonthDate = DateUtil.generatePreviousMonthDateBy(lastFirstMonthDate);
        lastThirdMonthDate = DateUtil.generatePreviousMonthDateBy(lastSecondMonthDate);
        lastForthMonthDate = DateUtil.generatePreviousMonthDateBy(lastThirdMonthDate);
        stockCard = new StockCard();
    }


    @Test
    public void shouldGetAverageMonthlyConsumptionCorrectly() throws LMISException {
        //given
        stockCard.setStockOnHand(200);
        stockService.stockRepository.save(stockCard);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(RECEIVE, 400, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);

        //when
        long consumption = stockService.getCmm(stockCard);

        //then
        assertEquals(100, consumption);
    }


    private StockMovementItem createMovementItem(StockMovementItem.MovementType type, long quantity, StockCard stockCard, Date createdTime, Date movementDate, boolean synced) throws LMISException {
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
        stockService.stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
        stockService.stockRepository.refresh(stockCard);

        return stockMovementItem;
    }

    @Test
    public void shouldGetLowStockAvgIsZeroWhenOnlyTwoValidPeriod() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(300);
        stockService.stockRepository.save(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);

        int lowStockAvg = stockService.getLowStockAvg(stockCard);
        assertEquals(2, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(0, lowStockAvg);
    }

    @Test
    public void shouldGetLowStockAvgCorrectly() throws Exception {

        stockCard.setStockOnHand(400);
        stockService.stockRepository.save(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);

        int lowStockAvg = stockService.getLowStockAvg(stockCard);
        assertEquals(3, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(5, lowStockAvg);
    }


    @Test
    public void shouldGetLowStockAvgWhenLastMonthSOHIsZero() throws Exception {
        stockCard.setStockOnHand(300);
        stockService.stockRepository.save(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);

        int lowStockAvg = stockService.getLowStockAvg(stockCard);
        assertEquals(3, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(0, lowStockAvg);
    }


    @Test
    public void shouldGetLowStockAvgWhenLastMonthHaveNoStockItem() throws Exception {
        stockCard.setStockOnHand(400);
        stockService.stockRepository.save(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);

        int lowStockAvg = stockService.getLowStockAvg(stockCard);
        assertEquals(3, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertEquals(4, lowStockAvg);
    }


}