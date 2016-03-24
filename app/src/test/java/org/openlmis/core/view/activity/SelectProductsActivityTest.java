package org.openlmis.core.view.activity;

import android.app.Activity;
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
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

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
        when(presenter.loadRegimeProducts()).thenReturn(value);

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
    public void shouldShowToastWhenMoreThanLimitChecked() throws Exception {
        selectProductsActivity.viewModels = getInventoryViewModels();
        for (InventoryViewModel model : selectProductsActivity.viewModels) {
            model.setChecked(true);
        }
        selectProductsActivity.btnNext.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("the maximum number of regimes is 5"));
    }

    @Test
    public void shouldSaveRegimeWhenOneProductHasChecked() throws Exception {
        Observable<Regimen> value = Observable.create(new Observable.OnSubscribe<Regimen>() {
            @Override
            public void call(Subscriber<? super Regimen> subscriber) {

            }
        });
        when(presenter.saveRegimes(anyList(), any(Regimen.RegimeType.class))).thenReturn(value);

        selectProductsActivity.viewModels = getInventoryViewModels();
        selectProductsActivity.viewModels.get(0).setChecked(true);
        selectProductsActivity.btnNext.performClick();

        verify(presenter).saveRegimes(newArrayList(selectProductsActivity.viewModels.get(0)), Regimen.RegimeType.Adults);
    }

    @Test
    public void shouldReturnRegimeWhenOnNext() throws Exception {
        ShadowActivity shadowActivity = shadowOf(selectProductsActivity);
        Regimen regimen = new Regimen();
        String regimenName = "regimenName";
        regimen.setName(regimenName);
        selectProductsActivity.saveSubscriber.onNext(regimen);

        Intent resultIntent = shadowActivity.getResultIntent();
        assertThat(shadowActivity.getResultCode(), is(Activity.RESULT_OK));
        assertThat(((Regimen) resultIntent.getSerializableExtra(Constants.PARAM_CUSTOM_REGIMEN)).getName(), is(regimenName));
        assertTrue(shadowActivity.isFinishing());
    }

    private ArrayList<InventoryViewModel> getInventoryViewModels() {
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        return newArrayList(new InventoryViewModel(product), new InventoryViewModel(product), new InventoryViewModel(product), new InventoryViewModel(product), new InventoryViewModel(product), new InventoryViewModel(product));
    }
}
