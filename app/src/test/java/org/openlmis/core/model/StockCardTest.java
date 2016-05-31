package org.openlmis.core.model;

import com.google.inject.AbstractModule;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
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
    public void shouldGetEarliestExpireDate() throws Exception {
        stockCard.setExpireDates("18/10/2015,18/10/2016,18/10/2017,18/10/2018");
        assertThat(stockCard.getEarliestExpireDate()).isEqualTo("18/10/2015");
    }

    @Test
    public void shouldInitStockMovementFromStockCard() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setStockOnHand(200);
        StockMovementItem stockMovementItem = stockCard.generateInitialStockMovementItem();

        MatcherAssert.assertThat(stockMovementItem.getMovementQuantity(), is(200L));
        MatcherAssert.assertThat(stockMovementItem.getStockOnHand(), is(200L));
        MatcherAssert.assertThat(stockMovementItem.getReason(), is(MovementReasonManager.INVENTORY));
        MatcherAssert.assertThat(stockMovementItem.getMovementType(), is(StockMovementItem.MovementType.PHYSICAL_INVENTORY));
        MatcherAssert.assertThat(stockMovementItem.getStockCard(), is(stockCard));
    }

    @Test
    public void shouldGetCMM() throws Exception {
        stockCard.setAvgMonthlyConsumption(0.77f);
        assertThat(stockCard.getCMM()).isEqualTo(1);
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

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }
}