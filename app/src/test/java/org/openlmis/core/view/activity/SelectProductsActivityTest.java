package org.openlmis.core.view.activity;

import android.content.Intent;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
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

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SelectProductsActivityTest {

    private SelectProductsActivity selectProductsActivity;
    ProductPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = mock(ProductPresenter.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ProductPresenter.class).toInstance(presenter);
            }
        });


        Observable<List<InventoryViewModel>> value = Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {

            }
        });
        when(presenter.loadMMIAProducts()).thenReturn(value);

        Intent intent = new Intent();
        intent.putExtra(SelectProductsActivity.PARAM_REGIME_TYPE, Regimen.RegimeType.Adults);
        selectProductsActivity = Robolectric.buildActivity(SelectProductsActivity.class).withIntent(intent).create().get();
    }


    @Test
    public void shouldShowToastWhenHasNotChecked() throws Exception {
        selectProductsActivity.viewModels = getInventoryViewModels();
        selectProductsActivity.btnNext.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("Please check product for regime"));
    }

    @Test
    public void shouldSaveRegimeWhenHasChecked() throws Exception {
        Observable<Void> value = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {

            }
        });
        when(presenter.saveRegimes(anyList(), any(Regimen.RegimeType.class))).thenReturn(value);

        selectProductsActivity.viewModels = getInventoryViewModels();
        selectProductsActivity.viewModels.get(0).setChecked(true);
        selectProductsActivity.btnNext.performClick();

        verify(presenter).saveRegimes(newArrayList(selectProductsActivity.viewModels.get(0)), Regimen.RegimeType.Adults);
    }

    private ArrayList<InventoryViewModel> getInventoryViewModels() {
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        return newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
    }
}