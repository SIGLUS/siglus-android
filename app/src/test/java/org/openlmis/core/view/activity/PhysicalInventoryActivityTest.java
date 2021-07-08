package org.openlmis.core.view.activity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.presenter.PhysicalInventoryPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.PhysicalInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryActivityTest extends LMISRepositoryUnitTest {

  private PhysicalInventoryActivity physicalInventoryActivity;
  private ActivityController<PhysicalInventoryActivity> activityController;
  private PhysicalInventoryPresenter mockedPresenter;
  private PhysicalInventoryAdapter mockedAdapter;
  private SearchView mockSearchView;

  @Before
  public void setUp() throws LMISException {
    mockedPresenter = mock(PhysicalInventoryPresenter.class);
    mockedAdapter = mock(PhysicalInventoryAdapter.class);
    mockSearchView = mock(SearchView.class);
    when(mockedPresenter.loadActivePrograms()).thenReturn(Observable.empty());
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(PhysicalInventoryPresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(PhysicalInventoryActivity.class);
    physicalInventoryActivity = activityController.create().start().resume().get();
    Product product = new ProductBuilder()
        .setCode("Product code")
        .setPrimaryName("Primary name")
        .setStrength("10mg")
        .build();
    StockCard stockCard = new StockCardBuilder().setProduct(product).setStockOnHand(10L).build();
    List<InventoryViewModel> data = newArrayList(new InventoryViewModel(stockCard), new InventoryViewModel(stockCard));
    when(mockedAdapter.getData()).thenReturn(data);

    physicalInventoryActivity.mAdapter = mockedAdapter;
    physicalInventoryActivity.searchView = mockSearchView;
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGoBackToParentPageWhenInventoryPageFinished() {
    // when
    physicalInventoryActivity.goToNextPage();

    // then
    assertTrue(physicalInventoryActivity.isFinishing());
  }

  @Test
  public void shouldShowSignDialogWhenCompleteClicked() {
    // given
    when(mockedAdapter.validateAll()).thenReturn(-1);

    // when
    physicalInventoryActivity.onCompleteClick();
    RobolectricUtils.waitLooperIdle();

    // then
    assertNotNull(physicalInventoryActivity.getSupportFragmentManager().findFragmentByTag("signature_dialog"));
  }

  @Test
  public void shouldDoInventoryWhenSignComplete(){
    // give
    when(mockedPresenter.doInventory(anyString())).thenReturn(Observable.empty());

    // when
    physicalInventoryActivity.signatureDialogDelegate.onSign("test");

    // then
    Mockito.verify(mockedPresenter,times(1)).doInventory("test");
  }

  @Test
  public void shouldCloseSearchWhenBackPressed(){
    // given
    when(mockSearchView.isIconified()).thenReturn(false);

    // when
    physicalInventoryActivity.onBackPressed();

    // then
    verify(mockSearchView,times(1)).onActionViewCollapsed();
  }

  @Test
  public void shouldShowDataChangeConfirmDialogWhenBackPressed() {
    // given
    when(mockedAdapter.isHasDataChanged()).thenReturn(true);
    when(mockSearchView.isIconified()).thenReturn(true);

    // when
    physicalInventoryActivity.onBackPressed();
    RobolectricUtils.waitLooperIdle();

    // then
    final Fragment backConfirmDialog = physicalInventoryActivity.getSupportFragmentManager()
        .findFragmentByTag("back_confirm_dialog");
    assertNotNull(backConfirmDialog);
    assertNotNull(((DialogFragment) backConfirmDialog).getDialog());
  }

  @Test
  public void shouldSaveDraftWhenSaveClicked(){
    // given
    when(mockedPresenter.saveDraftInventoryObservable()).thenReturn(Observable.empty());

    // when
    physicalInventoryActivity.onSaveClick();

    // then
    Mockito.verify(mockedPresenter,times(1)).saveDraftInventoryObservable();
  }

  @Test
  public void shouldShowErrorWhenOnErrorCalled() {
    String errorMessage = "This is throwable error";
    physicalInventoryActivity.errorAction.call(new Throwable(errorMessage));

    assertNull(physicalInventoryActivity.loadingDialog);
  }
}