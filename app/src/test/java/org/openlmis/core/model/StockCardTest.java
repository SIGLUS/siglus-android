package org.openlmis.core.model;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(LMISTestRunner.class)
public class StockCardTest {

    private ProductRepository mockProductRepository;
    private ProgramRepository mockProgramRepository;
    private StockCard stockCard;
    private List<StockMovementItem> wrapper;
    private StockMovementItem item1;
    private StockMovementItem item2;
    private StockMovementItem item3;


    @Before
    public void setUp() throws Exception {
        mockProductRepository = mock(ProductRepository.class);
        mockProgramRepository = mock(ProgramRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        stockCard = StockCardBuilder.buildStockCard();

        wrapper = new ArrayList<>();

        StockMovementItemBuilder builder = new StockMovementItemBuilder();

        item1 = builder.build();
        item2 = builder.build();
        item3 = builder.build();

        wrapper.add(item1);
        wrapper.add(item2);
        wrapper.add(item3);

        stockCard.setStockMovementItemsWrapper(wrapper);
    }

    @Test
    public void shouldInitStockMovementFromStockCard() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setStockOnHand(200);
        StockMovementItem stockMovementItem = stockCard.generateInitialStockMovementItem();

        assertThat(stockMovementItem.getMovementQuantity(), is(200L));
        assertThat(stockMovementItem.getStockOnHand(), is(200L));
        assertThat(stockMovementItem.getReason(), is(MovementReasonManager.INVENTORY));
        assertThat(stockMovementItem.getMovementType(), is(MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
        assertThat(stockMovementItem.getStockCard(), is(stockCard));
    }

    @Test
    public void shouldGetCMM() throws Exception {
        stockCard.setAvgMonthlyConsumption(0.7777777f);
        assertThat(stockCard.getCMM(),is(0.78f));

        stockCard.setAvgMonthlyConsumption(12.3849f);
        assertThat(stockCard.getCMM(),is(12.38f));

        stockCard.setAvgMonthlyConsumption(2234f);
        assertThat(stockCard.getCMM(),is(2234f));
    }

    @Test
    public void shouldGetIsLowStockAvg() throws Exception {
        stockCard.setStockOnHand(1);
        stockCard.setAvgMonthlyConsumption(100.5f);
        assertTrue(stockCard.isLowStock());
    }

    @Test
    public void shouldGetOverStockAvg() throws Exception {
        stockCard.setStockOnHand(220);
        stockCard.setAvgMonthlyConsumption(100.5f);
        assertTrue(stockCard.isOverStock());
    }

    @Test
    public void shouldGetNonEmptyLotOnHandList() throws Exception {
        Lot lot1 = new Lot();
        lot1.setExpirationDate(DateUtil.parseString("Sep 2014",DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        LotOnHand lotOnHand1 = new LotOnHand();
        lotOnHand1.setLot(lot1);
        lotOnHand1.setQuantityOnHand(1L);
        Lot lot2 = new Lot();
        lot2.setExpirationDate(DateUtil.parseString("Jan 2013",DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        LotOnHand lotOnHand2 = new LotOnHand();
        lotOnHand2.setLot(lot2);
        lotOnHand2.setQuantityOnHand(0L);
        Lot lot3 = new Lot();
        lot3.setExpirationDate(DateUtil.parseString("Feb 2014",DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        LotOnHand lotOnHand3 = new LotOnHand();
        lotOnHand3.setLot(lot3);
        lotOnHand3.setQuantityOnHand(1L);
        stockCard.setLotOnHandListWrapper(Arrays.asList(lotOnHand1,lotOnHand2,lotOnHand3));

        assertThat(stockCard.getNonEmptyLotOnHandList().size(),is(2));
        assertThat(stockCard.getNonEmptyLotOnHandList().get(0),is(lotOnHand1));
        assertThat(stockCard.getNonEmptyLotOnHandList().get(1),is(lotOnHand3));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }

    @Test
    public void shouldGetEarliestLotExpirationDate() throws Exception {
        stockCard.setLotOnHandListWrapper(new ArrayList<LotOnHand>());

        assertNull(stockCard.getEarliestLotExpiryDate());
        Lot lot1 = new Lot();
        lot1.setExpirationDate(DateUtil.parseString("Sep 2014",DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        LotOnHand lotOnHand1 = new LotOnHand();
        lotOnHand1.setLot(lot1);
        lotOnHand1.setQuantityOnHand(1L);
        Lot lot2 = new Lot();
        lot2.setExpirationDate(DateUtil.parseString("Jan 2013",DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        LotOnHand lotOnHand2 = new LotOnHand();
        lotOnHand2.setLot(lot2);
        lotOnHand2.setQuantityOnHand(0L);
        Lot lot3 = new Lot();
        lot3.setExpirationDate(DateUtil.parseString("Feb 2014",DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        LotOnHand lotOnHand3 = new LotOnHand();
        lotOnHand3.setLot(lot3);
        lotOnHand3.setQuantityOnHand(1L);
        stockCard.setLotOnHandListWrapper(Arrays.asList(lotOnHand1,lotOnHand2,lotOnHand3));
        assertThat(DateUtil.formatDate(stockCard.getEarliestLotExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR), is("Feb 2014"));
    }
}