package org.openlmis.core.view.activity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.view.holder.SelectEmergencyProductsViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

@RunWith(LMISTestRunner.class)
public class SelectEmergencyProductsActivityTest {

  private SelectEmergencyProductsActivity activity;

  private ProductPresenter productPresenter;
  private ActivityController<SelectEmergencyProductsActivity> activityController;

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

    activityController = Robolectric.buildActivity(SelectEmergencyProductsActivity.class);
    activity = activityController.create().start().resume().visible().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldLoadEmergencyProducts() throws Exception {
    verify(productPresenter).loadEmergencyProducts();
  }

  @Test
  public void shouldShowToastWhenHasNotChecked() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(100000);
    SingleClickButtonListener.isViewClicked = false;

    activity.mAdapter.refreshList(getInventoryViewModels());
    activity.btnNext.performClick();

    assertThat(ShadowToast.getTextOfLatestToast(), is("Please select product"));
  }

  @Test
  public void shouldShowToastWhenMoreThanLimitChecked() throws Exception {
    ArrayList<InventoryViewModel> inventoryViewModels = getInventoryViewModels();
    for (InventoryViewModel model : inventoryViewModels) {
      model.setChecked(true);
    }
    inventoryViewModels.add(new InventoryViewModel(
        new ProductBuilder().setPrimaryName("Product name").setCode("code").build()));

    activity.mAdapter.refreshList(inventoryViewModels);
    activity.mAdapter.notifyDataSetChanged();

    SelectEmergencyProductsViewHolder viewHolder = activity.mAdapter
        .onCreateViewHolder(new LinearLayout(activity), 0);
    activity.mAdapter.onBindViewHolder(viewHolder, 10);
    viewHolder.itemView.findViewById(R.id.touchArea_checkbox).performClick();

    assertThat(ShadowToast.getTextOfLatestToast(),
        is("You can only select 10 products for an emergency requisition"));
  }

  @Test
  public void shouldInflateCurrentDataAfterFilter() throws Exception {
    activity.mAdapter.refreshList(getInventoryViewModels());

    SelectEmergencyProductsViewHolder viewHolder = activity.mAdapter
        .onCreateViewHolder(new LinearLayout(activity), 0);
    activity.mAdapter.onBindViewHolder(viewHolder, 0);

    String actualValue = ((TextView) viewHolder.itemView.findViewById(R.id.tv_product_name))
        .getText().toString();
    assertThat(actualValue,
        is(activity.mAdapter.getFilteredList().get(0).getStyledName().toString()));
  }

  private ArrayList<InventoryViewModel> getInventoryViewModels() {
    ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Product product = new ProductBuilder().setPrimaryName("Product name")
          .setCode(String.valueOf(i)).build();
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