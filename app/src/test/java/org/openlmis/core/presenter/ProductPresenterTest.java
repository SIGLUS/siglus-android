package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ProductPresenterTest {

    private ProductPresenter presenter;
    private ProductRepository productRepository;
    private ProgramRepository programRepository;
    private RegimenRepository regimenRepository;

    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        programRepository = mock(ProgramRepository.class);
        regimenRepository = mock(RegimenRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(ProgramRepository.class).toInstance(programRepository);
                bind(RegimenRepository.class).toInstance(regimenRepository);
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

    @Test
    public void shouldSaveRegime() throws Exception {
        when(regimenRepository.getByName(anyString())).thenReturn(null);

        TestSubscriber<Void> subscriber = new TestSubscriber<>();
        presenter.saveRegimes(getInventoryViewModels(), Regimen.RegimeType.Adults).subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        ArgumentCaptor<Regimen> regimenArgumentCaptor = ArgumentCaptor.forClass(Regimen.class);
        verify(regimenRepository).create(regimenArgumentCaptor.capture());

        Regimen regimen = regimenArgumentCaptor.getValue();
        assertThat(regimen.getName(), is("Product code+Product code"));
        assertThat(regimen.getType(), is(Regimen.RegimeType.Adults));
    }

    @Test
    public void shouldNotSaveRegimeWhenHasRegimeInDB() throws Exception {
        when(regimenRepository.getByName(anyString())).thenReturn(new Regimen());

        TestSubscriber<Void> subscriber = new TestSubscriber<>();
        presenter.saveRegimes(getInventoryViewModels(), Regimen.RegimeType.Adults).subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        verify(regimenRepository, never()).create(any(Regimen.class));
    }

    private ArrayList<InventoryViewModel> getInventoryViewModels() {
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        return newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
    }
}