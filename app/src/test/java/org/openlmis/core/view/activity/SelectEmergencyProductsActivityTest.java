package org.openlmis.core.view.activity;


import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SelectEmergencyProductsActivityTest {

    private SelectEmergencyProductsActivity activity;

    private ProductPresenter productPresenter;

    @Before
    public void setUp() throws Exception {
        productPresenter = mock(ProductPresenter.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductPresenter.class).toInstance(productPresenter);
            }
        });

        when(productPresenter.loadEmergencyProducts()).thenReturn(createDummyObservable());

        activity = Robolectric.setupActivity(SelectEmergencyProductsActivity.class);
    }

    @Test
    public void shouldLoadEmergencyProducts() throws Exception {
        verify(productPresenter).loadEmergencyProducts();
    }


    @Test
    public void shouldShowToastWhenHasNotChecked() throws Exception {
        activity.viewModels = getInventoryViewModels();
        activity.btnNext.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("Please check product"));
    }

    @Test
    public void shouldShowToastWhenMoreThanLimitChecked() throws Exception {
        activity.viewModels = getInventoryViewModels();
        for (InventoryViewModel model : activity.viewModels) {
            model.setChecked(true);
        }
        activity.btnNext.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("checked product limit is 20"));
    }

    private ArrayList<InventoryViewModel> getInventoryViewModels() {
        Product product = new ProductBuilder().setPrimaryName("Product name").setCode("011111").build();
        ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            inventoryViewModels.add(new InventoryViewModel(product));
        }
        return inventoryViewModels;
    }

    private Observable<List<InventoryViewModel>> createDummyObservable() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {

            }
        });
    }
}