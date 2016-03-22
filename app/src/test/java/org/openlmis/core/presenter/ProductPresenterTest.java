package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ProductPresenterTest {

    private ProductPresenter presenter;
    private ProductRepository productRepository;
    private ProgramRepository programRepository;

    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        programRepository = mock(ProgramRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(ProgramRepository.class).toInstance(programRepository);
            }
        });
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductPresenter.class);
    }

    @Test
    public void shouldLoadMMIAProducts() throws Exception {
        // when
        Program program = new Program();
        when(programRepository.queryByCode(Constants.MMIA_PROGRAM_CODE)).thenReturn(program);
        ArrayList<Product> list = new ArrayList<>();
        Product product = ProductBuilder.buildAdultProduct();
        list.add(product);
        when(productRepository.queryProducts(program.getId())).thenReturn(list);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        presenter.loadMMIAProducts().subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        verify(productRepository).queryProducts(programRepository.queryByCode(Constants.MMIA_PROGRAM_CODE).getId());

        Product actual = subscriber.getOnNextEvents().get(0).get(0).getProduct();
        assertThat(actual, is(product));
    }
}