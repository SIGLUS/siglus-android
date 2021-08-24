package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class AddProductsToBulkEntriesPresenterTest {

  private ProductRepository productRepository;

  private AddProductsToBulkEntriesPresenter addProductsToBulkEntriesPresenter;

  @Before
  public void setup() throws Exception {
    productRepository = mock(ProductRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProductRepository.class).toInstance(productRepository);
      }
    });
    addProductsToBulkEntriesPresenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(AddProductsToBulkEntriesPresenter.class);
  }

  @Test
  public void shouldGetAllProductWithoutAddedProducts() throws LMISException {
    // given
    Product product1 = Product.builder().code("22A07").primaryName("TDR").build();
    Product product2 = Product.builder().code("22A08").primaryName("Prata").build();
    Product product3 = Product.builder().code("22A09").primaryName("potassio").build();
    Product product4 = Product.builder().code("22A01").primaryName("nitrao").build();
    Product product5 = Product.builder().code("22A02").primaryName("mudolo").build();
    List<Product> products = new ArrayList<>();
    products.add(product1);
    products.add(product2);
    products.add(product3);
    products.add(product4);
    products.add(product5);
    List<String> addedProducts = Arrays.asList(product1.getCode(), product2.getCode());
    when(productRepository.listAllProductsWithoutKit()).thenReturn(products);

    // when
    TestSubscriber<List<ProductsToBulkEntriesViewModel>> subscriber = new TestSubscriber<>();
    Observable<List<ProductsToBulkEntriesViewModel>> observable = addProductsToBulkEntriesPresenter
        .getProducts(addedProducts, false, null);
    observable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    // then
    subscriber.assertNoErrors();
    List<ProductsToBulkEntriesViewModel> productList = subscriber.getOnNextEvents().get(0);
    assertEquals("22A02", productList.get(0).getProduct().getCode());
  }

}