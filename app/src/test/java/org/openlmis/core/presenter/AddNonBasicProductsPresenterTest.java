package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class AddNonBasicProductsPresenterTest {

    private AddNonBasicProductsPresenter presenter;
    private ProductRepository productRepository;

    private Product product1 = new Product();
    private Product product2 = new Product();
    private Product product3 = new Product();

    @Before
    public void setUp() throws Exception {
        product1 = new Product();
        product2 = new Product();
        product1.setCode("08A01");
        product2.setCode("08A02");
        product3.setCode("08A03");
        product1.setPrimaryName("Product 1");
        product2.setPrimaryName("Product 2");
        product3.setPrimaryName("Product 3");
        product1.setType("Comprimido");
        product2.setType("Injectavle");
        product3.setType("Injectavle");
        product1.setBasic(false);
        product2.setBasic(false);
        product3.setBasic(false);
        productRepository = mock(ProductRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(AddNonBasicProductsPresenter.class);
    }

    @Test
    public void shouldGenerateNonBasicProductsViewModelsWhenThereAreNotNonBasicProductsSelected() {
        List<Product> expectedNonBasicProducts = newArrayList(product1, product2);
        TestSubscriber<List<NonBasicProductsViewModel>> subscriber = new TestSubscriber<>();

        when(productRepository.listNonBasicProducts()).thenReturn(expectedNonBasicProducts);
        Observable<List<NonBasicProductsViewModel>> observable = presenter.getAllNonBasicProductsViewModels(new ArrayList<Product>());
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        List<NonBasicProductsViewModel> actualProductsViewModels = subscriber.getOnNextEvents().get(0);

        assertThat(expectedNonBasicProducts.size(), is(actualProductsViewModels.size()));
        for (int index = 0; index < expectedNonBasicProducts.size(); index++) {
            assertThat(expectedNonBasicProducts.get(index).getCode(), is(actualProductsViewModels.get(index).getProductCode()));
        }
    }

    @Test
    public void shouldGenerateNonBasicProductsViewModelsWhenThereAreNonBasicProductsSelected() {
        List<Product> expectedNonBasicProducts = newArrayList(product1, product2, product3);
        List<Product> expectedNonBasicProductsSelected = newArrayList(product1);
        TestSubscriber<List<NonBasicProductsViewModel>> subscriber = new TestSubscriber<>();

        when(productRepository.listNonBasicProducts()).thenReturn(expectedNonBasicProducts);
        Observable<List<NonBasicProductsViewModel>> observable = presenter.getAllNonBasicProductsViewModels(expectedNonBasicProductsSelected);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        List<NonBasicProductsViewModel> actualProductsViewModels = subscriber.getOnNextEvents().get(0);

        assertThat(expectedNonBasicProducts.size(), is(actualProductsViewModels.size()));
        for (int index = 0; index < expectedNonBasicProducts.size(); index++) {
            assertThat(expectedNonBasicProducts.get(index).getCode(), is(actualProductsViewModels.get(index).getProductCode()));
        }
        assertThat(actualProductsViewModels.get(0).isChecked(), is(true));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(productRepository);
        }
    }
}