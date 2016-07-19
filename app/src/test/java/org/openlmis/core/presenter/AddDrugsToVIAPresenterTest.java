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
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
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
    private StockRepository stockRepository;
    AddDrugsToVIAPresenter.AddDrugsToVIAView view;
    private RnrFormHelper rnrFormHelper;


    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        programRepository = mock(ProgramRepository.class);
        productProgramRepository = mock(ProductProgramRepository.class);
        rnrFormItemRepository = mock(RnrFormItemRepository.class);
        stockRepository = mock(StockRepository.class);
        rnrFormHelper = mock(RnrFormHelper.class);

        view = mock(AddDrugsToVIAPresenter.AddDrugsToVIAView.class);

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
        Observable observable = presenter.saveRnrItemsObservable(inventoryViewModels, new Date(), new Date());
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
        presenter.generateNewVIAItems(newArrayList(inventoryViewModel1, inventoryViewModel2), new Date(), new Date());
        verify(rnrFormItemRepository, never()).batchCreateOrUpdate(anyList());

    }

    @Test
    public void shouldAssignValuesToSelectedArchivedProductsWhenSaving() throws Exception {
        Product product1 = new ProductBuilder().setCode("P1").setPrimaryName("product 1").setIsArchived(true).setIsActive(true).build();
        InventoryViewModel inventoryViewModel1 = new InventoryViewModel(product1);
        inventoryViewModel1.setQuantity("100");

        StockCard stockCard = new StockCardBuilder().setStockOnHand(0L).setProduct(product1).build();
        StockMovementItem stockMovementItem1 = new StockMovementItemBuilder().withDocumentNo("123").build();
        StockMovementItem stockMovementItem2 = new StockMovementItemBuilder().build();
        StockMovementItem stockMovementItem3 = new StockMovementItemBuilder().build();
        Date periodBegin = DateUtil.parseString("2016-01-21", DateUtil.DB_DATE_FORMAT);
        Date periodEnd = DateUtil.parseString("2016-02-20", DateUtil.DB_DATE_FORMAT);

        when(view.validateInventory()).thenReturn(true);
        when(stockRepository.queryStockCardByProductId(product1.getId())).thenReturn(stockCard);
        when(stockRepository.queryStockItemsByPeriodDates(stockCard, periodBegin, periodEnd)).thenReturn(newArrayList(stockMovementItem1, stockMovementItem2, stockMovementItem3));

        ArrayList<InventoryViewModel> inventoryViewModels = newArrayList(inventoryViewModel1);
        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable observable = presenter.saveRnrItemsObservable(inventoryViewModels, periodBegin, periodEnd);
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        ArgumentCaptor<RnrFormItem> captor = ArgumentCaptor.forClass(RnrFormItem.class);
        ArgumentCaptor<List> captor2 = ArgumentCaptor.forClass(List.class);
        verify(rnrFormHelper).assignTotalValues(captor.capture(), captor2.capture());
        List<RnrFormItem> captorAllValues = captor.getAllValues();
        List<List> captor2AllValues = captor2.getAllValues();
        assertThat(captorAllValues.get(0).getRequestAmount(), is(100L));
        assertThat(((StockMovementItem) captor2AllValues.get(0).get(0)).getDocumentNumber(), is("123"));
    }

    @NonNull
    private InventoryViewModel buildInventoryViewModel(String productCode, String quantity) {
        Product product1 = new ProductBuilder().setCode(productCode).setPrimaryName("product 1").setIsActive(true).build();
        InventoryViewModel inventoryViewModel1 = new InventoryViewModel(product1);
        inventoryViewModel1.setQuantity(quantity);
        return inventoryViewModel1;
    }

}