package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.AddDrugsToViaInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class AddDrugsToVIAPresenterTest {

    private AddDrugsToVIAPresenter presenter;
    private ProductRepository productRepository;
    private ProgramRepository programRepository;
    private ProductProgramRepository productProgramRepository;
    private RnrFormItemRepository rnrFormItemRepository;
    private StockRepository stockRepository;
    private RnrFormHelper rnrFormHelper;


    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        programRepository = mock(ProgramRepository.class);
        productProgramRepository = mock(ProductProgramRepository.class);
        rnrFormItemRepository = mock(RnrFormItemRepository.class);
        stockRepository = mock(StockRepository.class);
        rnrFormHelper = mock(RnrFormHelper.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(ProgramRepository.class).toInstance(programRepository);
                bind(ProductProgramRepository.class).toInstance(productProgramRepository);
                bind(RnrFormItemRepository.class).toInstance(rnrFormItemRepository);
                bind(StockRepository.class).toInstance(stockRepository);
                bind(RnrFormHelper.class).toInstance(rnrFormHelper);
            }
        });
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(AddDrugsToVIAPresenter.class);
    }


    @Test
    public void loadActiveProductsNotInVIAForm() throws Exception {
        Product product1 = new ProductBuilder().setCode("P1").setPrimaryName("A1").build();
        Product product2 = new ProductBuilder().setCode("P2").setPrimaryName("A2").build();
        Product product3 = new ProductBuilder().setCode("P3").setPrimaryName("A3").build();
        Product product4 = new ProductBuilder().setCode("P4").setPrimaryName("A4").build();


        when(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm()).thenReturn(newArrayList(product1, product2, product3, product4));

        TestSubscriber<Void> subscriber = new TestSubscriber<>();
        presenter.loadActiveProductsNotInVIAForm(newArrayList("P3", "P4")).subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        assertThat(presenter.getInventoryViewModelList().size(), is(2));
        assertThat(presenter.getInventoryViewModelList().get(0).getProductName(), is("A1"));
        assertThat(presenter.getInventoryViewModelList().get(1).getProductName(), is("A2"));
    }

    @Test
    public void shouldConvertViewModelsToRnrFormList() throws Exception {
        InventoryViewModel inventoryViewModel1 = buildInventoryViewModel("P1", "12");
        inventoryViewModel1.setChecked(true);
        InventoryViewModel inventoryViewModel2 = buildInventoryViewModel("P2", "34");
        inventoryViewModel2.setChecked(true);

        when(productRepository.getByCode("P1")).thenReturn(new ProductBuilder().setCode("P1").setIsActive(true).setIsArchived(false).build());
        when(productRepository.getByCode("P2")).thenReturn(new ProductBuilder().setCode("P2").setIsActive(true).setIsArchived(true).build());

        presenter.getInventoryViewModelList().addAll(newArrayList(inventoryViewModel1, inventoryViewModel2));
        TestSubscriber<ArrayList<RnrFormItem>> subscriber = new TestSubscriber<>();
        Observable observable = presenter.convertViewModelsToRnrFormItems();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        assertThat(subscriber.getOnNextEvents().get(0).size(), is(2));
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getProduct().getCode(), is("P1"));
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getRequestAmount(), is(12L));
        assertThat(subscriber.getOnNextEvents().get(0).get(1).getProduct().getCode(), is("P2"));
        assertThat(subscriber.getOnNextEvents().get(0).get(1).getRequestAmount(), is(34L));
    }

    @Test
    public void shouldEqualBetweenViewModels() {
        InventoryViewModel inventoryViewModel1 = buildInventoryViewModel("P1", "34");
        inventoryViewModel1.setChecked(true);
        InventoryViewModel inventoryViewModel2 = buildInventoryViewModel("P2", "34");
        inventoryViewModel2.setChecked(true);
        assertEquals(inventoryViewModel1, inventoryViewModel2);
        assertTrue(inventoryViewModel1.validate());
    }

    @NonNull
    private InventoryViewModel buildInventoryViewModel(String productCode, String quantity) {
        Product product1 = new ProductBuilder().setCode(productCode).setPrimaryName("product 1").setIsActive(true).build();
        AddDrugsToViaInventoryViewModel inventoryViewModel1 = new AddDrugsToViaInventoryViewModel(product1);
        inventoryViewModel1.setQuantity(quantity);
        return inventoryViewModel1;
    }

}