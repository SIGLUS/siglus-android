package org.openlmis.core.view.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.BulkInitialInventoryPresenterTest;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.Subscriber;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class BulkInitialInventoryActivityTest {

    private BulkInitialInventoryActivity bulkInventoryActivity;
    private List<InventoryViewModel> data;
    private BulkInitialInventoryAdapter mockedAdapter;
    private LongSparseArray<Product> noBasicProducts;
    private LongSparseArray<Product> basicProducts;
    private ProductRepository productRepositoryMock;
    private SingleClickButtonListener singleClickButtonListener;


    @Before
    public void setUp() throws LMISException {
        bulkInventoryActivity = Robolectric.buildActivity(BulkInitialInventoryActivity.class).create().get();

        mockedAdapter = mock(BulkInitialInventoryAdapter.class);
        productRepositoryMock = mock(ProductRepository.class);
        singleClickButtonListener = mock(SingleClickButtonListener.class);

        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        data = newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
        when(mockedAdapter.getData()).thenReturn(data);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        bulkInventoryActivity = Robolectric.buildActivity(BulkInitialInventoryActivity.class).create().get();

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        noBasicProducts = new LongSparseArray<>();
        basicProducts = new LongSparseArray<>();
        getNoBasicProductList();
        getBasicProductList();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGoToHomePageAfterInitInventoryAndSetNeedInventoryToFalse() {
        bulkInventoryActivity.goToNextPage();

        Intent startIntent = shadowOf(bulkInventoryActivity).getNextStartedActivity();
        assertEquals(startIntent.getComponent().getClassName(), HomeActivity.class.getName());
        assertEquals(SharedPreferenceMgr.getInstance().isNeedsInventory(), false);
    }

    @Test
    public void shouldHaveFiveBasicAndFiveNoBasic() throws LMISException {
        when(productRepositoryMock.listBasicProducts()).thenReturn(newArrayList(basicProducts.get(1L), basicProducts.get(2L), basicProducts.get(3L), basicProducts.get(4L), basicProducts.get(5L)));

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<InventoryViewModel>> observable = bulkInventoryActivity.presenter.loadInventory();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();


        bulkInventoryActivity.presenter.addNonBasicProductsToInventory(newArrayList(noBasicProducts.get(1), noBasicProducts.get(2), noBasicProducts.get(3), noBasicProducts.get(4), noBasicProducts.get(5)));
        List<InventoryViewModel> viewModels = bulkInventoryActivity.presenter.getInventoryViewModelList();
        assertEquals(viewModels.get(0).getViewType(), BulkInitialInventoryAdapter.ITEM_BASIC_HEADER);
        assertEquals(viewModels.get(1).getViewType(), BulkInitialInventoryAdapter.ITEM_BASIC);
        assertEquals(viewModels.get(6).getViewType(), BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER);
        assertEquals(viewModels.get(7).getViewType(), BulkInitialInventoryAdapter.ITEM_NO_BASIC);
        assertEquals(12, viewModels.size());

        bulkInventoryActivity.btnSave.performClick();
//        verify(bulkInventoryActivity).btnSave.getVisibility()
//        assertThat(bulkInventoryActivity, new StartedMatcher(BulkInitialInventoryActivity.class));
    }

    @NonNull
    private void getNoBasicProductList() {
        Product product1 = ProductBuilder.create().setProductId(1L).setIsBasic(false).setCode("ABC").setPrimaryName("Test Product").build();
        Product product2 = ProductBuilder.create().setProductId(2L).setIsBasic(false).setCode("productCode2").setPrimaryName("productName2").build();
        Product product3 = ProductBuilder.create().setProductId(3L).setIsBasic(false).setCode("productCode3").setPrimaryName("productName3").build();
        Product product4 = ProductBuilder.create().setProductId(4L).setIsBasic(false).setCode("productCode4").setPrimaryName("productName4").build();
        Product product5 = ProductBuilder.create().setProductId(5L).setIsBasic(false).setCode("productCode5").setPrimaryName("productName5").build();
        noBasicProducts.put(product1.getId(), product1);
        noBasicProducts.put(product2.getId(), product2);
        noBasicProducts.put(product3.getId(), product3);
        noBasicProducts.put(product4.getId(), product4);
        noBasicProducts.put(product5.getId(), product5);
    }

    @NonNull
    private void getBasicProductList() {
        Product product1 = ProductBuilder.create().setProductId(1L).setIsBasic(true).setCode("bABC").setPrimaryName("bTest Product").build();
        Product product2 = ProductBuilder.create().setProductId(2L).setIsBasic(true).setCode("bProductCode2").setPrimaryName("bProductName2").build();
        Product product3 = ProductBuilder.create().setProductId(3L).setIsBasic(true).setCode("bProductCode3").setPrimaryName("bProductName3").build();
        Product product4 = ProductBuilder.create().setProductId(4L).setIsBasic(true).setCode("bProductCode4").setPrimaryName("bProductName4").build();
        Product product5 = ProductBuilder.create().setProductId(5L).setIsBasic(true).setCode("bProductCode5").setPrimaryName("bProductName5").build();
        basicProducts.put(product1.getId(), product1);
        basicProducts.put(product2.getId(), product2);
        basicProducts.put(product3.getId(), product3);
        basicProducts.put(product4.getId(), product4);
        basicProducts.put(product5.getId(), product5);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(productRepositoryMock);
        }

    }
}