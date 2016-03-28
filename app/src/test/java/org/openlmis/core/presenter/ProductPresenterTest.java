package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RegimeProduct;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
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

        TestSubscriber<List<RegimeProductViewModel>> subscriber = new TestSubscriber<>();
        presenter.loadRegimeProducts().subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        assertThat(subscriber.getOnNextEvents().get(0).size(), is(20));
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getShortCode(), is("3TC 150mg"));
        assertThat(subscriber.getOnNextEvents().get(0).get(19).getShortCode(), is("AZT 50mg/5ml sol oral"));
    }

    @Test
    public void shouldSaveRegime() throws Exception {
        when(regimenRepository.getByNameAndCategory(anyString(), any(Regimen.RegimeType.class))).thenReturn(null);

        TestSubscriber<Regimen> subscriber = new TestSubscriber<>();
        presenter.saveRegimes(getInventoryViewModels(), Regimen.RegimeType.Adults).subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        ArgumentCaptor<Regimen> regimenArgumentCaptor = ArgumentCaptor.forClass(Regimen.class);
        verify(regimenRepository).create(regimenArgumentCaptor.capture());

        Regimen regimen = regimenArgumentCaptor.getValue();
        assertThat(regimen.getName(), is("3TC 150mg+3TC 150mg"));
        assertThat(regimen.getType(), is(Regimen.RegimeType.Adults));
        assertThat(regimen.isCustom(), is(true));
    }

    @Test
    public void shouldNotSaveRegimeWhenHasRegimeInDB() throws Exception {
        when(regimenRepository.getByNameAndCategory(anyString(), any(Regimen.RegimeType.class))).thenReturn(new Regimen());

        TestSubscriber<Regimen> subscriber = new TestSubscriber<>();
        presenter.saveRegimes(getInventoryViewModels(), Regimen.RegimeType.Adults).subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        verify(regimenRepository, never()).create(any(Regimen.class));
    }

    private ArrayList<RegimeProductViewModel> getInventoryViewModels() {
        RegimeProduct regimeProduct = new RegimeProduct("3TC 150mg", "Lamivudina 150mg");
        return newArrayList(new RegimeProductViewModel(regimeProduct), new RegimeProductViewModel(regimeProduct));
    }
}