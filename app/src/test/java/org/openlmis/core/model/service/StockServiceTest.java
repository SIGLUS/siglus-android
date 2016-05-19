package org.openlmis.core.model.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private StockRepository stockRepository;

    @Before
    public void setup() throws LMISException {
        RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SharedPreferenceMgr.class);
        stockService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockService.class);
        stockRepository = mock(StockRepository.class);

        Date today = DateUtil.today();
        lastFirstMonthDate = DateUtil.generatePreviousMonthDateBy(today);
        lastSecondMonthDate = DateUtil.generatePreviousMonthDateBy(lastFirstMonthDate);
        lastThirdMonthDate = DateUtil.generatePreviousMonthDateBy(lastSecondMonthDate);
        lastForthMonthDate = DateUtil.generatePreviousMonthDateBy(lastThirdMonthDate);
        stockCard = new StockCard();
    }

    @Test
    public void shouldUpdateAverageMonthlyConsumption() throws Exception {
        List<StockCard> list = asList(new StockCard());
        stockService = spy(stockService);
        stockService.stockRepository = stockRepository;
        when(stockRepository.list()).thenReturn(list);
        doReturn(0F).when(stockService).calculateAverageMonthlyConsumption(any(StockCard.class));

        stockService.monthlyUpdateAvgMonthlyConsumption();

        verify(stockRepository).createOrUpdate(any(StockCard.class));
    }

    @Test
    public void shouldNotUpdateAverageMonthlyConsumption() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().getMillis());
        SharedPreferenceMgr.getInstance().updateLatestLowStockAvgTime();
        List<StockCard> list = asList(new StockCard());
        stockService.stockRepository = stockRepository;
        when(stockRepository.list()).thenReturn(list);

        stockService.monthlyUpdateAvgMonthlyConsumption();

        verify(stockRepository,never()).createOrUpdate(any(StockCard.class));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionWithStockOutCorrectly() throws LMISException {
        //given
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(200);
        stockService.stockRepository.createOrUpdate(stockCard);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(RECEIVE, 400, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);

        //when
        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);

        //then
        assertThat(100F, is(averageMonthlyConsumption));
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
    public void shouldCalculateAverageMonthlyConsumptionLessThanZeroWhenOnlyTwoValidPeriod() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(300);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(2, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertThat(-1F, is(averageMonthlyConsumption));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionLessThanZeroWhenThereIsNoStockMovement() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setPrimaryName("product");
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(300);
        stockCard.setProduct(product);

        stockService.stockRepository.createOrUpdate(stockCard);
        when(stockRepository.queryFirstStockMovementItem(stockCard)).thenReturn(null);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertThat(-1F, is(averageMonthlyConsumption));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionCorrectly() throws Exception {
        stockCard.setStockOnHand(400);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(3, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertThat(100F, is(averageMonthlyConsumption));
    }


    @Test
    public void shouldCalculateAverageMonthlyConsumptionLessThanZeroWhenLastMonthSOHIsZero() throws Exception {
        stockCard.setStockOnHand(300);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(3, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertThat(-1F, is(averageMonthlyConsumption));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionWhenLastMonthHaveNoStockItem() throws Exception {
        stockCard.setStockOnHand(400);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(3, stockService.stockRepository.listLastFive(stockCard.getId()).size());
        assertThat(200F/3, is(averageMonthlyConsumption));
    }


}