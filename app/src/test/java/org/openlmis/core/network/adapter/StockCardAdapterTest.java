package org.openlmis.core.network.adapter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
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
    stockCardAdapter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockCardAdapter.class);

    stockCard = StockCardBuilder.buildStockCard();

    wrapper = new ArrayList<>();

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

    String json = JsonFileReader.readJson(getClass(), "StockCardWithMovement.json");
    StockCard stockCard = stockCardAdapter.deserialize(new JsonParser().parse(json), null, null);
    assertNotNull(json);

    assertThat(stockCard.getStockOnHand(), is(480L));
    assertThat(stockCard.getProduct().getCode(), is("08S42"));

    List<StockMovementItem> stockMovementItemsWrapper = stockCard.getStockMovementItemsWrapper();
    assertThat(stockMovementItemsWrapper.size(), is(3));
  }

  @Test
  public void shouldDeserializeStockCardWithLots() throws Exception {
    Product product = ProductBuilder.buildAdultProduct();
    product.setCode("08R01");
    when(mockProductRepository.getByCode("08R01")).thenReturn(product);

    String json = JsonFileReader.readJson(getClass(), "StockCardResponseWithLots.json");
    StockCard stockCard = stockCardAdapter.deserialize(new JsonParser().parse(json), null, null);
    assertNotNull(json);

    List<LotOnHand> lotsOnHandWrapper = stockCard.getLotOnHandListWrapper();
    assertThat(lotsOnHandWrapper.size(), is(2));
    assertThat(lotsOnHandWrapper.get(0).getLot().getLotNumber(), is("test3"));
    assertThat(lotsOnHandWrapper.get(0).getQuantityOnHand(), is(300L));
    assertThat(lotsOnHandWrapper.get(0).getLot().getExpirationDate(),
        is(DateUtil.parseString("2016-07-31", DateUtil.DB_DATE_FORMAT)));

    List<StockMovementItem> stockMovementItems = stockCard.getStockMovementItemsWrapper();
    assertEquals(1, stockMovementItems.size());
    assertEquals(700, stockMovementItems.get(0).getMovementQuantity());
    assertEquals(700, stockMovementItems.get(0).getStockOnHand());

    List<LotMovementItem> lotMovementItems = stockMovementItems.get(0)
        .getLotMovementItemListWrapper();
    assertEquals(2, lotMovementItems.size());
    assertThat(lotMovementItems.get(0).getMovementQuantity(), is(300L));
    assertThat(lotMovementItems.get(1).getMovementQuantity(), is(400L));

    assertEquals("08R01", lotMovementItems.get(0).getLot().getProduct().getCode());
    assertEquals("08R01", lotMovementItems.get(1).getLot().getProduct().getCode());
  }

  @Test
  public void shouldSetupExpireDate() {
    item1.setExpireDates("2015-02-15");
    item2.setExpireDates("2015-03-15");
    item3.setExpireDates("2015-04-15");
    stockCard.setStockMovementItemsWrapper(wrapper);
    stockCardAdapter.setupStockCardExpireDates(stockCard);

    assertThat(stockCard.getExpireDates(), is(item3.getExpireDates()));
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProductRepository.class).toInstance(mockProductRepository);
      bind(ProgramRepository.class).toInstance(mockProgramRepository);
    }
  }
}