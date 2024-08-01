package org.openlmis.core.model.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class ProductRepositoryTest extends LMISRepositoryUnitTest {

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  private ProductRepository productRepository;
  private StockRepository stockRepository;
  private String code = "code";

  @Before
  public void setUp() throws Exception {
    stockRepository = mock(StockRepository.class);
    sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockRepository.class).toInstance(stockRepository);
      }
    });

    productRepository = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(ProductRepository.class);
  }

  @Test
  public void shouldUpdateNotifyBannerListWhenSOHIsZeroAndProductIsDeActive() throws Exception {
    // given
    Product product = new Product();
    product.setPrimaryName("name");
    product.setActive(false);
    product.setCode(code);
    product.setArchived(false);

    Product existingProduct = ProductBuilder.create().setCode(code).setIsActive(true)
        .setIsArchived(true).build();
    productRepository.save(Collections.singletonList(existingProduct));

    StockCard stockCard = new StockCard();
    stockCard.setProduct(product);
    stockCard.setStockOnHand(0);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

    // when
    productRepository.updateDeactivateProductNotifyList(product);

    // then
    assertTrue(SharedPreferenceMgr.getInstance().isNeedShowProductsUpdateBanner());
  }

  @Test
  public void shouldNotUpdateNotifyBannerListWhenProductIsArchived() throws Exception {
    // given
    Product product = new Product();
    product.setPrimaryName("name");
    product.setActive(false);
    product.setCode(code);
    product.setArchived(true);

    Product existingProduct = ProductBuilder.create().setCode(code).setIsActive(true)
        .setIsArchived(true).build();
    productRepository.save(Collections.singletonList(existingProduct));

    StockCard stockCard = new StockCard();
    stockCard.setProduct(product);
    stockCard.setStockOnHand(0);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

    // when
    productRepository.updateDeactivateProductNotifyList(product);

    // then
    assertFalse(SharedPreferenceMgr.getInstance().isNeedShowProductsUpdateBanner());
  }

  @Test
  public void shouldRemoveNotifyBannerListWhenReactiveProduct() throws Exception {
    // given
    Product product = new Product();
    product.setPrimaryName("new name");
    product.setActive(true);
    product.setCode(code);

    Product existingProduct = ProductBuilder.create().setCode(code).setIsActive(false)
        .setPrimaryName("name").build();
    productRepository.save(Collections.singletonList(existingProduct));

    StockCard stockCard = new StockCard();
    stockCard.setStockOnHand(0);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

    // when
    productRepository.updateDeactivateProductNotifyList(product);

    // then
    assertFalse(SharedPreferenceMgr.getInstance().isNeedShowProductsUpdateBanner());
  }
}
