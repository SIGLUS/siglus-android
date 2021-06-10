package org.openlmis.core.view.activity;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class SelectRegimeProductsActivityTest {

  private SelectRegimeProductsActivity selectProductsActivity;
  ProductPresenter presenter;

  @Before
  public void setUp() throws Exception {
    presenter = mock(ProductPresenter.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application,
        binder -> binder.bind(ProductPresenter.class).toInstance(presenter));

    Observable<List<RegimeProductViewModel>> value = Observable.create(subscriber -> {
    });
    when(presenter.loadRegimeProducts(Regimen.RegimeType.Adults)).thenReturn(value);

    Intent intent = new Intent();
    intent.putExtra(SelectRegimeProductsActivity.PARAM_REGIME_TYPE, Regimen.RegimeType.Adults);
    selectProductsActivity = Robolectric.buildActivity(SelectRegimeProductsActivity.class, intent)
        .create().get();
  }

  @Test
  public void shouldShowToastWhenHasNotChecked() {
    LMISTestApp.getInstance().setCurrentTimeMillis(100000);
    SingleClickButtonListener.isViewClicked = false;

    selectProductsActivity.viewModels = getInventoryViewModels();
    selectProductsActivity.btnNext.performClick();

    assertThat(ShadowToast.getTextOfLatestToast(), is("Please select product"));
  }

  @Test
  public void shouldShowToastWhenMoreThanLimitChecked() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(100000);
    SingleClickButtonListener.isViewClicked = false;

    selectProductsActivity.viewModels = getInventoryViewModels();
    for (RegimeProductViewModel model : selectProductsActivity.viewModels) {
      model.setChecked(true);
    }
    selectProductsActivity.btnNext.performClick();

    assertThat(ShadowToast.getTextOfLatestToast(), is("You can only select 1 products for regime"));
  }

  @Test
  public void shouldSaveRegimeWhenOneProductHasChecked() {
    LMISTestApp.getInstance().setCurrentTimeMillis(100000);
    SingleClickButtonListener.isViewClicked = false;

    Observable<Regimen> value = Observable.create(subscriber -> {
    });
    when(presenter.saveRegimes(anyObject(), any(Regimen.RegimeType.class))).thenReturn(value);

    selectProductsActivity.viewModels = getInventoryViewModels();
    selectProductsActivity.viewModels.get(0).setChecked(true);
    selectProductsActivity.btnNext.performClick();

    verify(presenter)
        .saveRegimes(selectProductsActivity.viewModels.get(0), Regimen.RegimeType.Adults);
  }

  @Test
  public void shouldReturnRegimeWhenOnNext() {
    ShadowActivity shadowActivity = shadowOf(selectProductsActivity);
    Regimen regimen = new Regimen();
    String regimenName = "regimenName";
    regimen.setName(regimenName);
    selectProductsActivity.saveSubscriber.onNext(regimen);

    Intent resultIntent = shadowActivity.getResultIntent();
    assertThat(shadowActivity.getResultCode(), is(Activity.RESULT_OK));
    assertThat(
        ((Regimen) resultIntent.getSerializableExtra(Constants.PARAM_CUSTOM_REGIMEN)).getName(),
        is(regimenName));
    assertTrue(shadowActivity.isFinishing());
  }

  private ArrayList<RegimeProductViewModel> getInventoryViewModels() {
    return newArrayList(new RegimeProductViewModel("3TC 150mg"),
        new RegimeProductViewModel("3TC 150mg"),
        new RegimeProductViewModel("3TC 150mg"),
        new RegimeProductViewModel("3TC 150mg"),
        new RegimeProductViewModel("3TC 150mg"),
        new RegimeProductViewModel("3TC 150mg"));
  }
}
