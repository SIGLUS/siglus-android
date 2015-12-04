package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockCardAdapterTest {

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
    }

    @Test
    public void shouldDeserializeStockCard() throws Exception {

        Product product = ProductBuilder.buildAdultProduct();
        product.setCode("08S42");
        when(mockProductRepository.getByCode("08S42")).thenReturn(product);

        String json = JsonFileReader.readJson(getClass(),"StockCardWithMovement.json");
        StockCard stockCard = stockCardAdapter.deserialize(new JsonParser().parse(json), null, null);
        assertThat(json).isNotNull();

        assertThat(stockCard.getStockOnHand()).isEqualTo(480);
        assertThat(stockCard.getProduct().getCode()).isEqualTo("08S42");

        List<StockMovementItem> stockMovementItemsWrapper = stockCard.getStockMovementItemsWrapper();
        assertThat(stockMovementItemsWrapper.size()).isEqualTo(3);
    }

    @Test
    public void shouldSetupMovementStockOnHand() throws Exception {
        stockCard.setStockOnHand(100);

        item1.setMovementQuantity(37);
        item1.setMovementType(StockMovementItem.MovementType.ISSUE);
        item2.setMovementQuantity(12);
        item2.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);
        item3.setMovementQuantity(17);
        item3.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);

        stockCardAdapter.setupMovementStockOnHand(stockCard, wrapper);

        assertThat(item1.getStockOnHand()).isEqualTo(71);
        assertThat(item2.getStockOnHand()).isEqualTo(83);
        assertThat(item3.getStockOnHand()).isEqualTo(100);
    }

    @Test
    public void shouldSetupExpireDate() {
        item1.setExpireDates("2015-02-15");
        item2.setExpireDates("2015-03-15");
        item3.setExpireDates("2015-04-15");
        stockCardAdapter.setupStockCardExpireDates(stockCard, wrapper);

        assertThat(stockCard.getExpireDates()).isEqualTo(item3.getExpireDates());
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }
}