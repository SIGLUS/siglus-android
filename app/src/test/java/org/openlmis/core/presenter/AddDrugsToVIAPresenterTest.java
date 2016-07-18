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
import rx.observers.TestSubscriber;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
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

        List<String> viaProgramCodes = newArrayList("PR1", "PR2");
        when(programRepository.queryProgramCodesByProgramCodeOrParentCode(Constants.VIA_PROGRAM_CODE)).thenReturn(newArrayList(viaProgramCodes));
        List<Long> productIdList = newArrayList(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        when(productProgramRepository.queryActiveProductIdsByProgramsWithKits(viaProgramCodes, false)).thenReturn(productIdList);
        List<Long> productsInVia = newArrayList(1L, 2L, 3L);
        when(rnrFormItemRepository.listAllProductIdsInCurrentVIADraft()).thenReturn(productsInVia);
        List<Long> productsNewlyAddedAsRnrItems = newArrayList(6L, 7L);
        when(rnrFormItemRepository.listAllProductIdsNewlyAddedAsRnrItems()).thenReturn(productsNewlyAddedAsRnrItems);

        when(productRepository.getById(4L)).thenReturn(new ProductBuilder().setCode("P1").setPrimaryName("ABC").build());
        when(productRepository.getById(5L)).thenReturn(new ProductBuilder().setCode("P2").setPrimaryName("DEF").build());
        when(productRepository.getById(6L)).thenReturn(new ProductBuilder().setCode("P3").setPrimaryName("111").build());
        when(productRepository.getById(7L)).thenReturn(new ProductBuilder().setCode("P4").setPrimaryName("222").build());

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
        List<RnrFormItem> newRnrItems = presenter.generateNewVIAItems(newArrayList(inventoryViewModel1, inventoryViewModel2));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(rnrFormItemRepository).batchCreateOrUpdate(captor.capture());
        List<List> captorAllValues = captor.getAllValues();
        assertThat(((RnrFormItem) captorAllValues.get(0).get(0)).getRequestAmount(), is(12L));
        assertThat(((RnrFormItem) captorAllValues.get(0).get(1)).getRequestAmount(), is(34L));

        assertThat(newRnrItems.size(), is(2));
        assertThat(newRnrItems.get(0).getRequestAmount(), is(12L));
        assertThat(newRnrItems.get(1).getRequestAmount(), is(34L));
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