package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
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

    @Before
    public void setUp() throws Exception {
        productRepository = mock(ProductRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(AddNonBasicProductsPresenter.class);
    }

    @Test
    public void shouldGenerateNonBasicProductsViewModels() {
        Product product1 = new Product();
        Product product2 = new Product();
        product1.setCode("08A01");
        product2.setCode("08A02");
        product1.setPrimaryName("Product 1");
        product2.setPrimaryName("Product 2");
        product1.setType("Comprimido");
        product2.setType("Injectavle");
        List<Product> expectedNonBasicProducts= newArrayList(product1,product2);
        TestSubscriber<List<NonBasicProductsViewModel>> subscriber = new TestSubscriber<>();

        when(productRepository.listNonBasicProducts()).thenReturn(expectedNonBasicProducts);
        Observable<List<NonBasicProductsViewModel>> observable = presenter.getAllNonBasicProductsViewModels();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        List<NonBasicProductsViewModel> actualProductsViewModels = subscriber.getOnNextEvents().get(0);

        assertThat(expectedNonBasicProducts.size(), is(actualProductsViewModels.size()));
        for (int index = 0; index < expectedNonBasicProducts.size(); index ++) {
            assertThat(expectedNonBasicProducts.get(index).getCode(), is(actualProductsViewModels.get(index).getProductCode()));
        }
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(productRepository);
        }
    }
}