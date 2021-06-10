package org.openlmis.core.presenter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class AddNonBasicProductsPresenterTest {

  private AddNonBasicProductsPresenter presenter;
  private ProductRepository productRepository;

  private Product product1 = new Product();
  private Product product2 = new Product();
  private final Product product3 = new Product();
  private final Product product4 = new Product();

  private final String addedNonBasicProductCode = "08A04";

  @Before
  public void setUp() throws Exception {
    product1 = new Product();
    product2 = new Product();
    product1.setCode("08A01");
    product2.setCode("08A02");
    product3.setCode("08A03");
    product4.setCode("08A04");
    product1.setPrimaryName("Product 1");
    product2.setPrimaryName("Product 2");
    product3.setPrimaryName("Product 3");
    product4.setPrimaryName("Product 4");
    product1.setType("Comprimido");
    product2.setType("Injectavle");
    product3.setType("Injectavle");
    product4.setType("Comprimido");
    product1.setBasic(false);
    product2.setBasic(false);
    product3.setBasic(false);
    product4.setBasic(false);
    productRepository = mock(ProductRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(AddNonBasicProductsPresenter.class);
  }

  @Test
  public void shouldGenerateNonBasicProductsViewModelsWhenThereAreNotNonBasicProductsSelected() {
    List<Product> expectedNonBasicProducts = newArrayList(product1, product2);
    TestSubscriber<List<NonBasicProductsViewModel>> subscriber = new TestSubscriber<>();

    when(productRepository.listNonBasicProducts()).thenReturn(expectedNonBasicProducts);
    Observable<List<NonBasicProductsViewModel>> observable = presenter
        .getAllNonBasicProductsViewModels(new ArrayList<String>());
    observable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();
    List<NonBasicProductsViewModel> actualProductsViewModels = subscriber.getOnNextEvents().get(0);

    assertThat(expectedNonBasicProducts.size(), is(actualProductsViewModels.size()));
    for (int index = 0; index < expectedNonBasicProducts.size(); index++) {
      assertThat(expectedNonBasicProducts.get(index).getCode(),
          is(actualProductsViewModels.get(index).getProductCode()));
      assertThat(expectedNonBasicProducts.get(index).getType(),
          is(actualProductsViewModels.get(index).getProductType()));
      assertThat(expectedNonBasicProducts.get(index),
          is(actualProductsViewModels.get(index).getProduct()));
      assertFalse(actualProductsViewModels.get(index).isChecked());
      assertThat(actualProductsViewModels.get(index).getStyledProductName().toString(),
          is(getStyledProductName(expectedNonBasicProducts.get(index))));
    }
  }

  @Test
  public void shouldNotShowTheAddedNonBasicProductInTheList() {
    List<Product> allNonBasicProductFromDB = newArrayList(product1, product2, product3, product4);
    TestSubscriber<List<NonBasicProductsViewModel>> subscriber = new TestSubscriber<>();

    when(productRepository.listNonBasicProducts()).thenReturn(allNonBasicProductFromDB);
    Observable<List<NonBasicProductsViewModel>> observable = presenter
        .getAllNonBasicProductsViewModels(newArrayList(addedNonBasicProductCode));
    observable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();
    List<NonBasicProductsViewModel> actualProductsViewModel = subscriber.getOnNextEvents().get(0);

    assertThat(allNonBasicProductFromDB.size(), is(actualProductsViewModel.size() + 1));
    for (NonBasicProductsViewModel model : actualProductsViewModel) {
      assertNotEquals(model.getProductCode(), addedNonBasicProductCode);
    }

  }

  private String getStyledProductName(Product product) {
    return product.getPrimaryName() + " [" + product.getCode() + "]";
  }


  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProductRepository.class).toInstance(productRepository);
    }
  }
}