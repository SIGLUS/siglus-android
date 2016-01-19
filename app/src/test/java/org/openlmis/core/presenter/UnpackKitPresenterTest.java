package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.KitProductBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class UnpackKitPresenterTest {
    private UnpackKitPresenter presenter;
    private UnpackKitPresenter.UnpackKitView view;
    private ProductRepository productRepository;
    private StockRepository stockRepository;
    private Product product;
    private StockCardViewModel viewModel;

    @Before
    public void setup() throws Exception {
        view = mock(UnpackKitPresenter.UnpackKitView.class);
        productRepository = mock(ProductRepository.class);
        stockRepository = mock(StockRepository.class);

        List<String> expireDates = new ArrayList<>();
        expireDates.add("15/2/2026");
        expireDates.add("30/5/2026");

        product = new ProductBuilder().setIsKit(false).setCode("productCode1").setPrimaryName("name1").setProductId(200L).build();
        viewModel = new StockCardViewModelBuilder(product).setChecked(true).setKitExpectQuantity(300).setQuantity("200").setExpiryDates(expireDates).build();

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(StockRepository.class).toInstance(stockRepository);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(UnpackKitPresenter.class);
        presenter.attachView(view);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldLoadKitProductList() throws Exception {
        //given
        Product kit = ProductBuilder.create().setCode("KIT_Code").setIsKit(true).build();
        Product product1 = ProductBuilder.create().setPrimaryName("p1").setProductId(1L).setCode("P1_Code").setIsKit(false).build();
        Product product2 = ProductBuilder.create().setPrimaryName("p2").setProductId(2L).setCode("P2_Code").setIsKit(false).build();
        KitProduct kitProduct1 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P1_Code").setQuantity(100).build();
        KitProduct kitProduct2 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P2_Code").setQuantity(200).build();

        List<KitProduct> kitProducts = Arrays.asList(kitProduct1, kitProduct2);
        kit.setKitProductList(kitProducts);

        when(productRepository.queryKitProductByKitCode(kit.getCode())).thenReturn(kitProducts);
        when(productRepository.getByCode(product1.getCode())).thenReturn(product1);
        when(productRepository.getByCode(product2.getCode())).thenReturn(product2);

        TestSubscriber<List<StockCardViewModel>> subscriber = new TestSubscriber<>();
        presenter.kitProductsSubscriber = subscriber;

        // when
        presenter.loadKitProducts("KIT_Code");

        subscriber.awaitTerminalEvent();
        //then

        verify(productRepository).queryKitProductByKitCode(kit.getCode());
        subscriber.assertNoErrors();

        List<StockCardViewModel> resultProducts = subscriber.getOnNextEvents().get(0);
        assertThat(resultProducts.size()).isEqualTo(2);

        StockCardViewModel viewModel1 = resultProducts.get(0);
        StockCardViewModel viewModel2 = resultProducts.get(1);

        assertThat(viewModel1.getProduct().getCode()).isEqualTo(product1.getCode());
        assertThat(viewModel2.getProduct().getCode()).isEqualTo(product2.getCode());
        assertThat(viewModel1.getKitExpectQuantity()).isEqualTo(100);
        assertThat(viewModel2.getKitExpectQuantity()).isEqualTo(200);
        assertTrue(viewModel1.isChecked());
        assertTrue(viewModel2.isChecked());
    }

    @Test
    public void shouldSaveStockCardWhenStockCardNotExists() throws Exception {
        Product kit = new ProductBuilder().setIsKit(true).setProductId(888L)
                .setCode("SD1112").setPrimaryName("primary name").build();
        StockCard kitStockCard = new StockCardBuilder().setStockCardId(112)
                .setStockOnHand(1000)
                .setCreateDate(new Date())
                .setProduct(kit)
                .build();

        when(stockRepository.queryStockCardByProductId(200L)).thenReturn(null);
        when(productRepository.getByCode("SD1112")).thenReturn(kit);
        when(stockRepository.queryStockCardByProductId(888L)).thenReturn(kitStockCard);


        TestSubscriber<Void> testSubscriber = new TestSubscriber();
        presenter.unpackProductsSubscriber = testSubscriber;
        presenter.stockCardViewModels = Arrays.asList(viewModel);
        presenter.kitCode = "SD1112";

        presenter.saveUnpackProducts();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(stockRepository).initStockCard(any(StockCard.class));
    }

    @Test
    public void shouldOnlySaveStockMovementItemsWhenStockCardExists() throws Exception {
        StockCard productStockCard = new StockCardBuilder().setStockCardId(111)
                .setStockOnHand(100)
                .setCreateDate(new Date())
                .setProduct(product)
                .setExpireDates("20/1/2026,15/2/2026")
                .build();

        Product kit = new ProductBuilder().setIsKit(true).setProductId(888L)
                .setCode("SD1112").setPrimaryName("primary name").build();

        StockCard kitStockCard = new StockCardBuilder().setStockCardId(112)
                .setStockOnHand(1000)
                .setCreateDate(new Date())
                .setProduct(kit)
                .build();


        TestSubscriber<Void> testSubscriber = new TestSubscriber();
        presenter.unpackProductsSubscriber = testSubscriber;
        presenter.stockCardViewModels = Arrays.asList(viewModel);
        presenter.kitCode = "SD1112";

        when(stockRepository.queryStockCardByProductId(200L)).thenReturn(productStockCard);

        when(productRepository.getByCode("SD1112")).thenReturn(kit);
        when(stockRepository.queryStockCardByProductId(888L)).thenReturn(kitStockCard);

        presenter.saveUnpackProducts();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(stockRepository, never()).initStockCard(any(StockCard.class));
        verify(stockRepository, times(2)).addStockMovementAndUpdateStockCard(any(StockMovementItem.class));

        assertThat(productStockCard.getStockOnHand()).isEqualTo(300);
        assertThat(productStockCard.getExpireDates()).isEqualTo("20/1/2026,15/2/2026,30/5/2026");
    }

    @Test
    public void shouldCallSaveSuccessWhenUnpackProductsSucceed() throws Exception {
        presenter.unpackProductsSubscriber.onNext(null);

        verify(view).loaded();
        verify(view).saveSuccess();
    }
}