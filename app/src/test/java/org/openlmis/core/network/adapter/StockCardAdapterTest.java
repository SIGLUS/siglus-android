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
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;

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

    @Before
    public void setUp() throws Exception {
        mockProductRepository = mock(ProductRepository.class);
        mockProgramRepository = mock(ProgramRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        stockCardAdapter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockCardAdapter.class);
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

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }
}