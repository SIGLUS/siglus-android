package org.openlmis.core.model.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.ProductRepository;
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
import static org.openlmis.core.manager.MovementReasonManager.MovementType.ISSUE;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;

@RunWith(LMISTestRunner.class)
public class StockServiceTest extends LMISRepositoryUnitTest {

    StockService stockService;

    private Date lastFirstMonthDate;
    private Date lastSecondMonthDate;
    private Date lastThirdMonthDate;
    private Date lastForthMonthDate;
    private StockCard stockCard;

    private StockRepository mockedStockRepository;
    private ProductRepository productRepository;
    private CmmRepository mockedCmmRepository;

    @Before
    public void setup() throws LMISException {
        mockedStockRepository = mock(StockRepository.class);
        mockedCmmRepository = mock(CmmRepository.class);
        stockService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockService.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);

        Date today = DateUtil.today();
        lastFirstMonthDate = DateUtil.generatePreviousMonthDateBy(today);
        lastSecondMonthDate = DateUtil.generatePreviousMonthDateBy(lastFirstMonthDate);
        lastThirdMonthDate = DateUtil.generatePreviousMonthDateBy(lastSecondMonthDate);
        lastForthMonthDate = DateUtil.generatePreviousMonthDateBy(lastThirdMonthDate);
        stockCard = new StockCard();
        stockCard.setProduct(getRandomProduct());
    }

    private Product getRandomProduct() throws LMISException {

        try {
            Product product = new Product();
            int random =(int)(Math.random() * 10000000);
            product.setId(random);
            product.setCode(String.valueOf(random));
            Program program = new Program("MMIA", "MMIA", null, false, null,null);
            product.setProgram(program);
            productRepository.createOrUpdate(product);
            return  product;
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void shouldUpdateAverageMonthlyConsumption() throws Exception {
        List<StockCard> list = asList(new StockCard());
        stockService = spy(stockService);
        stockService.stockRepository = mockedStockRepository;
        stockService.cmmRepository = mockedCmmRepository;
        when(mockedStockRepository.list()).thenReturn(list);
        doReturn(0F).when(stockService).calculateAverageMonthlyConsumption(any(StockCard.class));

        stockService.monthlyUpdateAvgMonthlyConsumption();

        verify(mockedStockRepository).createOrUpdate(any(StockCard.class));
        verify(mockedCmmRepository).save(any(Cmm.class));
    }

    @Test
    public void shouldNotUpdateAverageMonthlyConsumption() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().getMillis());
        SharedPreferenceMgr.getInstance().updateLatestLowStockAvgTime();
        List<StockCard> list = asList(new StockCard());
        stockService.stockRepository = mockedStockRepository;
        stockService.cmmRepository = mockedCmmRepository;
        when(mockedStockRepository.list()).thenReturn(list);

        stockService.monthlyUpdateAvgMonthlyConsumption();

        verify(mockedStockRepository, never()).createOrUpdate(any(StockCard.class));
        verify(mockedCmmRepository, never()).save(any(Cmm.class));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionWithStockOutCorrectly() throws LMISException {
        //given
        StockCard stockCard = new StockCard();
        stockCard.setProduct(getRandomProduct());
        stockCard.setStockOnHand(200);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);//4 month ago soh:100

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);//3 month ago soh:0

        createMovementItem(RECEIVE, 400, stockCard, new Date(), lastSecondMonthDate, false);//2 month ago soh:400
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);//2 month ago soh:300

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);//1 month ago soh:200

        //when
        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);

        //then
        assertThat(averageMonthlyConsumption, is(100F));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionWithContinuedStockOutCorrectly() throws LMISException {
        //given
        StockCard stockCard = new StockCard();
        stockCard.setProduct(getRandomProduct());
        stockCard.setStockOnHand(200);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);//4 month ago soh:100
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);//4 month ago soh:0

        //3 month ago: no movement, so it inherits the stock out status of 4 month ago

        createMovementItem(RECEIVE, 400, stockCard, new Date(), lastSecondMonthDate, false);//2 month ago soh:400
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);//2 month ago soh:300

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);//1 month ago soh:200

        //when
        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);

        //then
        assertThat(averageMonthlyConsumption, is(100F));
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
        stockService.stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
        stockService.stockRepository.refresh(stockCard);

        return stockMovementItem;
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionLessThanZeroWhenOnlyTwoValidPeriod() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(getRandomProduct());
        stockCard.setStockOnHand(300);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(2, stockService.stockMovementRepository.listLastFiveStockMovements(stockCard.getId()).size());
        assertThat(100F, is(averageMonthlyConsumption));
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
        assertEquals(3, stockService.stockMovementRepository.listLastFiveStockMovements(stockCard.getId()).size());
        assertThat(100F, is(averageMonthlyConsumption));
    }


    @Test
    public void shouldCalculateAverageMonthlyConsumptionLessThanZeroWhenLastMonthSOHIsZero() throws Exception {
        stockCard.setStockOnHand(300);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastSecondMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(3, stockService.stockMovementRepository.listLastFiveStockMovements(stockCard.getId()).size());
        assertThat(100F, is(averageMonthlyConsumption));
    }

    @Test
    public void shouldCalculateAverageMonthlyConsumptionWhenLastMonthHaveNoStockItem() throws Exception {
        stockCard.setStockOnHand(400);
        stockService.stockRepository.createOrUpdate(stockCard);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastFirstMonthDate, false);

        createMovementItem(ISSUE, 100, stockCard, new Date(), lastThirdMonthDate, false);
        createMovementItem(ISSUE, 100, stockCard, new Date(), lastForthMonthDate, false);

        float averageMonthlyConsumption = stockService.calculateAverageMonthlyConsumption(stockCard);
        assertEquals(3, stockService.stockMovementRepository.listLastFiveStockMovements(stockCard.getId()).size());
        assertThat(200F / 3, is(averageMonthlyConsumption));
    }
}
