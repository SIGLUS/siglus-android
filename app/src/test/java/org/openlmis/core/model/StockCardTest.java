package org.openlmis.core.model;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.network.adapter.StockCardAdapter;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@RunWith(LMISTestRunner.class)
public class StockCardTest {

    private StockCardAdapter stockCardAdapter;
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
        stockCardAdapter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockCardAdapter.class);

        stockCard = StockCardBuilder.buildStockCard();

        wrapper = new ArrayList<StockMovementItem>();

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
    public void shouldSetUpStockOnHandForMovements() throws Exception {

        item1.setMovementQuantity(37);
        item1.setMovementType(StockMovementItem.MovementType.ISSUE);
        item2.setMovementQuantity(12);
        item2.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);
        item3.setMovementQuantity(17);
        item3.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);

        stockCard.setUpStockOnHandForMovements(100L);

        assertThat(item1.getStockOnHand()).isEqualTo(71);
        assertThat(item2.getStockOnHand()).isEqualTo(83);
        assertThat(item3.getStockOnHand()).isEqualTo(100);
    }

    @Test
    public void shouldGetEarliestExpireDate() throws Exception {
        stockCard.setExpireDates("18/10/2015,18/10/2016,18/10/2017,18/10/2018");
        assertThat(stockCard.getEarliestExpireDate()).isEqualTo("18/10/2015");
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }
}