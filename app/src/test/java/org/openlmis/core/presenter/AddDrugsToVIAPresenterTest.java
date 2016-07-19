package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class AddDrugsToVIAPresenterTest {

    private AddDrugsToVIAPresenter presenter;
    private ProductRepository productRepository;
    private ProgramRepository programRepository;
    private ProductProgramRepository productProgramRepository;
    private RnrFormItemRepository rnrFormItemRepository;
    AddDrugsToVIAPresenter.AddDrugsToVIAView view;


    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        programRepository = mock(ProgramRepository.class);
        productProgramRepository = mock(ProductProgramRepository.class);
        rnrFormItemRepository = mock(RnrFormItemRepository.class);

        view = mock(AddDrugsToVIAPresenter.AddDrugsToVIAView.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(ProgramRepository.class).toInstance(programRepository);
                bind(ProductProgramRepository.class).toInstance(productProgramRepository);
                bind(RnrFormItemRepository.class).toInstance(rnrFormItemRepository);
            }
        });
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(AddDrugsToVIAPresenter.class);
        presenter.attachView(view);
    }


    @Test
    public void loadActiveProductsNotInVIAForm() throws Exception {
        Product product1 = new ProductBuilder().setCode("P1").setPrimaryName("ABC").build();
        Product product2 = new ProductBuilder().setCode("P2").setPrimaryName("DEF").build();
        when(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm()).thenReturn(newArrayList(product1, product2));

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        presenter.loadActiveProductsNotInVIAForm().subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        assertThat(subscriber.getOnNextEvents().get(0).size(), is(2));
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getProductName(), is("ABC"));
        assertThat(subscriber.getOnNextEvents().get(0).get(1).getProductName(), is("DEF"));
    }

    @Test
    public void shouldReturnRnrFormItemsListFromViewModels() throws Exception {
        InventoryViewModel inventoryViewModel1 = buildInventoryViewModel("P1", "12");
        InventoryViewModel inventoryViewModel2 = buildInventoryViewModel("P2", "34");

        when(view.validateInventory()).thenReturn(true);

        ArrayList<InventoryViewModel> inventoryViewModels = newArrayList(inventoryViewModel1, inventoryViewModel2);
        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable observable = presenter.saveRnrItemsObservable(inventoryViewModels);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(rnrFormItemRepository).batchCreateOrUpdate(captor.capture());
        List<List> captorAllValues = captor.getAllValues();
        assertThat(((RnrFormItem) captorAllValues.get(0).get(0)).getRequestAmount(), is(12L));
        assertThat(((RnrFormItem) captorAllValues.get(0).get(1)).getRequestAmount(), is(34L));
    }

    @Test
    public void shouldNotSaveOrUpdateIfNewlyAddedItemsNotValid() throws LMISException {
        InventoryViewModel inventoryViewModel1 = buildInventoryViewModel("P1", "12");
        InventoryViewModel inventoryViewModel2 = buildInventoryViewModel("P2", "34");

        when(view.validateInventory()).thenReturn(false);
        presenter.generateNewVIAItems(newArrayList(inventoryViewModel1, inventoryViewModel2));
        verify(rnrFormItemRepository, never()).batchCreateOrUpdate(anyList());

    }

    @NonNull
    private InventoryViewModel buildInventoryViewModel(String productCode, String quantity) {
        Product product1 = new ProductBuilder().setCode(productCode).setPrimaryName("product 1").setIsActive(true).build();
        InventoryViewModel inventoryViewModel1 = new InventoryViewModel(product1);
        inventoryViewModel1.setQuantity(quantity);
        return inventoryViewModel1;
    }

}