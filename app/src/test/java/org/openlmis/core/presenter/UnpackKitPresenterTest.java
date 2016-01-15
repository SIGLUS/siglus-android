package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.KitProductBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class UnpackKitPresenterTest {
    private UnpackKitPresenter presenter;
    private UnpackKitPresenter.UnpackKitView view;
    private ProductRepository repository;

    @Before
    public void setup() throws Exception {
        view = mock(UnpackKitPresenter.UnpackKitView.class);
        repository = mock(ProductRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(repository);
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

    @Test
    public void shouldLoadKitProductList() throws Exception {
        //given
        Product kit = ProductBuilder.create().setCode("KIT_Code").setIsKit(true).build();
        Product product1 = ProductBuilder.create().setPrimaryName("p1").setProductId(1L).setCode("P1_Code").setIsKit(false).build();
        Product product2 = ProductBuilder.create().setPrimaryName("p2").setProductId(2L).setCode("P2_Code").setIsKit(false).build();
        KitProduct kitProduct1 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P1_Code").build();
        KitProduct kitProduct2 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P2_Code").build();

        List<KitProduct> kitProducts = Arrays.asList(kitProduct1, kitProduct2);
        kit.setKitProductList(kitProducts);

        when(repository.queryKitProductByKitCode(kit.getCode())).thenReturn(kitProducts);
        when(repository.getByCode(product1.getCode())).thenReturn(product1);
        when(repository.getByCode(product2.getCode())).thenReturn(product2);

        // when

        TestSubscriber<List<StockCardViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<StockCardViewModel>> observable = presenter.loadKitProducts("KIT_Code");
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        //then

        verify(repository).queryKitProductByKitCode(kit.getCode());
        subscriber.assertNoErrors();

        List<StockCardViewModel> resultProducts = subscriber.getOnNextEvents().get(0);
        assertThat(resultProducts.size()).isEqualTo(2);
        assertThat(resultProducts.get(0).getProduct().getCode()).isEqualTo(product1.getCode());
        assertThat(resultProducts.get(1).getProduct().getCode()).isEqualTo(product2.getCode());
    }
}