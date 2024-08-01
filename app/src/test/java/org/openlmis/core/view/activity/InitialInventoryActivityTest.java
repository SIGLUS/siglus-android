package org.openlmis.core.view.activity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.InitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class InitialInventoryActivityTest {

  private InitialInventoryActivity initialInventoryActivity;
  private InitialInventoryPresenter mockedPresenter;

  private List<InventoryViewModel> data;
  private ActivityController<InitialInventoryActivity> activityController;

  @Before
  public void setUp() throws LMISException {
    mockedPresenter = mock(InitialInventoryPresenter.class);
    when(mockedPresenter.getInflatedInventoryOnMainThread()).thenReturn(Observable.empty());

    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(InitialInventoryPresenter.class).toInstance(mockedPresenter);
      }
    });

    activityController = Robolectric.buildActivity(InitialInventoryActivity.class);
    initialInventoryActivity = activityController.create().get();

    InitialInventoryAdapter mockedAdapter = mock(InitialInventoryAdapter.class);
    Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name")
        .setStrength("10mg").build();
    data = newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
    when(mockedAdapter.getData()).thenReturn(data);

    initialInventoryActivity.mAdapter = mockedAdapter;
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGoToHomePageAfterInitInventoryOrDoPhysicalInventory() {
    initialInventoryActivity.goToNextPage();

    Intent startIntent = shadowOf(initialInventoryActivity).getNextStartedActivity();
    assertEquals(startIntent.getComponent().getClassName(), HomeActivity.class.getName());
  }

  @Test
  public void shouldGoToStockCardPageAfterAddedNewProduct() {
    Intent intentToStockCard = new Intent();
    intentToStockCard.putExtra(Constants.PARAM_IS_ADD_NEW_DRUG, true);

    initialInventoryActivity = Robolectric
        .buildActivity(InitialInventoryActivity.class, intentToStockCard).create().get();

    initialInventoryActivity.goToNextPage();

    Intent startIntent = shadowOf(initialInventoryActivity).getNextStartedActivity();
    assertEquals(startIntent.getComponent().getClassName(), StockCardListActivity.class.getName());
    assertEquals(startIntent.getFlags(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
  }

  @Test
  public void shouldShowMessageAndNeverBackWhenPressBackInInitInventory() {
    RobolectricUtils.waitLooperIdle();

    initialInventoryActivity.onBackPressed();

    assertEquals(ShadowToast.getTextOfLatestToast(), initialInventoryActivity.getString(R.string.msg_save_before_exit));

    initialInventoryActivity.onBackPressed();

    assertFalse(initialInventoryActivity.isFinishing());
  }

  @Test
  public void shouldGetAddNewDrugActivity() {
    Intent intent = InitialInventoryActivity.getIntentToMe(ApplicationProvider.getApplicationContext(), true);

    assertNotNull(intent);
    assertEquals(intent.getComponent().getClassName(), InitialInventoryActivity.class.getName());
    assertTrue(intent.getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false));
  }

  @Test
  public void shouldInitUIWhenInitialInventory() {
    assertTrue(initialInventoryActivity.loadingDialog.isShowing());
    verify(mockedPresenter).getInflatedInventoryOnMainThread();
  }

  @Test
  public void shouldDoInitialInventoryWhenBtnDoneClicked() {
    RobolectricUtils.resetNextClickTime();

    when(initialInventoryActivity.mAdapter.validateAll()).thenReturn(-1);
    initialInventoryActivity.onNextMainPageAction = o -> { };

    when(mockedPresenter.initStockCardObservable()).thenReturn(Observable.empty());
    initialInventoryActivity.btnDone.performClick();
    verify(mockedPresenter).initStockCardObservable();
  }

  @Test
  public void shouldGoToMainPageWhenOnNextCalled() {
    initialInventoryActivity.onNextMainPageAction.call(null);

    assertNull(initialInventoryActivity.loadingDialog);
    assertFalse(SharedPreferenceMgr.getInstance().isNeedsInventory());
  }

  @Test
  public void shouldShowErrorWhenOnErrorCalled() {
    String errorMessage = "This is throwable error";
    initialInventoryActivity.errorAction.call(new Throwable(errorMessage));

    assertNull(initialInventoryActivity.loadingDialog);
  }
}