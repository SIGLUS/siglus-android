package org.openlmis.core.view.activity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.view.activity.AddNonBasicProductsActivity.RESULT_CODE;
import static org.openlmis.core.view.activity.AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS;
import static org.openlmis.core.view.activity.BulkInitialInventoryActivity.REQUEST_CODE;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.BulkInitialInventoryPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Observable.OnSubscribe;

@RunWith(LMISTestRunner.class)
public class BulkInitialInventoryActivityTest {

  private BulkInitialInventoryActivity bulkInventoryActivity;
  private ActivityController<BulkInitialInventoryActivity> activityController;

  private BulkInitialInventoryAdapter mockedAdapter;
  private BulkInitialInventoryPresenter mockedPresenter;
  private SearchView mockSearchView;
  private Product product;

  @Before
  public void setUp() throws LMISException {
    mockedAdapter = mock(BulkInitialInventoryAdapter.class);
    mockedPresenter = mock(BulkInitialInventoryPresenter.class);
    mockSearchView = mock(SearchView.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    when(mockedPresenter.loadActivePrograms()).thenReturn(Observable.empty());
    activityController = Robolectric.buildActivity(BulkInitialInventoryActivity.class);
    bulkInventoryActivity = activityController.create().start().resume().get();
    product = new ProductBuilder()
        .setProductId(1L)
        .setCode("Product code")
        .setPrimaryName("Primary name")
        .setStrength("10mg").build();
    List<InventoryViewModel> data = newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
    when(mockedAdapter.getData()).thenReturn(data);
    bulkInventoryActivity.mAdapter = mockedAdapter;
    bulkInventoryActivity.searchView = mockSearchView;
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGoToHomePageAfterInitInventoryAndSetNeedInventoryToFalse() {
    // when
    bulkInventoryActivity.goToNextPage();

    // then
    Intent startIntent = shadowOf(bulkInventoryActivity).getNextStartedActivity();
    assertEquals(HomeActivity.class.getName(), startIntent.getComponent().getClassName());
    assertFalse(SharedPreferenceMgr.getInstance().isNeedsInventory());
  }

  @Test
  public void shouldGoToAddProductPageAfterBtnClick() {
    // given
    when(mockedPresenter.getAllAddedNonBasicProduct()).thenReturn(Collections.singletonList("test"));

    // when
    bulkInventoryActivity.goToAddNonBasicProductsLister().onClick(null);

    // then
    Intent startIntent = shadowOf(bulkInventoryActivity).getNextStartedActivity();
    assertEquals(AddNonBasicProductsActivity.class.getName(), startIntent.getComponent().getClassName());
    assertNotNull(startIntent.getSerializableExtra(SELECTED_NON_BASIC_PRODUCTS));
  }

  @Test
  public void shouldCorrectSetTotal() {
    // given
    final InventoryViewModel inventoryViewModel1 = new InventoryViewModel(product);
    inventoryViewModel1.setViewType(BulkInitialInventoryAdapter.ITEM_BASIC);
    final InventoryViewModel inventoryViewModel2 = new InventoryViewModel(product);
    inventoryViewModel2.setViewType(BulkInitialInventoryAdapter.ITEM_BASIC);
    List<InventoryViewModel> data = newArrayList(inventoryViewModel1, inventoryViewModel2);
    when(mockedPresenter.getInventoryViewModelList()).thenReturn(data);

    // when
    bulkInventoryActivity.setTotal();

    // then
    assertEquals(bulkInventoryActivity.getString(R.string.label_total, 2),
        bulkInventoryActivity.tvTotal.getText().toString());
  }

  @Test
  public void shouldSaveDraftWhenSaveClicked() {
    // given
    when(mockedPresenter.saveDraftInventoryObservable()).thenReturn(Observable.empty());

    // when
    bulkInventoryActivity.onSaveClick();

    // then
    Mockito.verify(mockedPresenter, times(1)).saveDraftInventoryObservable();
  }

  @Test
  public void shouldDoInventoryWhenCompleteClicked() {
    // give
    when(mockedPresenter.doInventory()).thenReturn(Observable.empty());
    when(mockedAdapter.validateAllForCompletedClick()).thenReturn(-1);

    // when
    bulkInventoryActivity.onCompleteClick();

    // then
    Mockito.verify(mockedPresenter, times(1)).doInventory();
  }

  @Test
  public void shouldCorrectSetBtnDoneStatus() {
    // given
    when(mockedPresenter.doInventory()).thenReturn(Observable.empty());
    when(mockedAdapter.validateAllForCompletedClick()).thenReturn(1);

    // when
    bulkInventoryActivity.onCompleteClick();

    // then
    assertTrue(bulkInventoryActivity.btnDone.isEnabled());
  }

  @Test
  public void shouldReloadAfterSaveDraft() {
    // given
    final OnSubscribe<Object> testOnSubscribe = subscriber -> subscriber.onNext(new Object());
    when(mockedPresenter.saveDraftInventoryObservable()).thenReturn(Observable.create(testOnSubscribe));
    when(mockedPresenter.loadInventory()).thenReturn(Observable.empty());

    // when
    bulkInventoryActivity.onSaveClick();

    // then
    Mockito.verify(mockedPresenter, times(1)).saveDraftInventoryObservable();
  }

  @Test
  public void shouldCloseSearchWhenBackPressed(){
    // given
    when(mockSearchView.isIconified()).thenReturn(false);

    // when
    bulkInventoryActivity.onBackPressed();

    // then
    verify(mockSearchView,times(1)).onActionViewCollapsed();
  }

  @Test
  public void shouldShowDataChangeConfirmDialogWhenBackPressed() {
    // given
    when(mockedAdapter.isHasDataChanged()).thenReturn(true);
    when(mockSearchView.isIconified()).thenReturn(true);

    // when
    bulkInventoryActivity.onBackPressed();
    RobolectricUtils.waitLooperIdle();

    // then
    final Fragment backConfirmDialog = bulkInventoryActivity.getSupportFragmentManager()
        .findFragmentByTag("back_confirm_dialog");
    assertNotNull(backConfirmDialog);
    assertNotNull(((DialogFragment) backConfirmDialog).getDialog());
  }

  @Test
  public void shouldCorrectSetOnActivityResult(){
    // given
    ArrayList<Product> newAddedProduct = new ArrayList<>();
    newAddedProduct.add(product);
    final Intent intent = new Intent();
    intent.putExtra(SELECTED_NON_BASIC_PRODUCTS, (Serializable) newAddedProduct);
    when(mockedPresenter.addNonBasicProductsToInventory(any())).thenReturn(Observable.empty());

    // when
    bulkInventoryActivity.onActivityResult(REQUEST_CODE,RESULT_CODE,intent);

    // then
    verify(mockedPresenter,times(1)).addNonBasicProductsToInventory(newAddedProduct);
  }

  @Test
  public void shouldUpdateUiAfterRemoveNonBasicProductListener(){
    // given
    final BulkInitialInventoryViewModel mockViewModel = mock(BulkInitialInventoryViewModel.class);

    // when
    bulkInventoryActivity.removeNonBasicProductListener().removeNoneBasicProduct(mockViewModel);

    // then
    verify(mockedPresenter,times(1)).removeNonBasicProductElement(mockViewModel);
    verify(mockedAdapter,times(1)).refresh();
    verify(mockedAdapter,times(1)).notifyDataSetChanged();
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(BulkInitialInventoryPresenter.class).toInstance(mockedPresenter);
    }

  }
}