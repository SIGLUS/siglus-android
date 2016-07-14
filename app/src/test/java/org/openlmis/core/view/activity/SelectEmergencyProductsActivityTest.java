package org.openlmis.core.view.activity;


import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.AbstractModule;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.holder.SelectEmergencyProductsViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

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

        activity = Robolectric.buildActivity(SelectEmergencyProductsActivity.class).create().start().resume().visible().get();
    }

    @Test
    public void shouldLoadEmergencyProducts() throws Exception {
        verify(productPresenter).loadEmergencyProducts();
    }


    @Test
    public void shouldShowToastWhenHasNotChecked() throws Exception {
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
        inventoryViewModels.add(new InventoryViewModel(new ProductBuilder().setPrimaryName("Product name").setCode(String.valueOf("code")).build()));

        activity.mAdapter.refreshList(inventoryViewModels);
        activity.mAdapter.notifyDataSetChanged();

        SelectEmergencyProductsViewHolder viewHolder = activity.mAdapter.onCreateViewHolder(new LinearLayout(activity), 0);
        activity.mAdapter.onBindViewHolder(viewHolder, 10);
        viewHolder.itemView.findViewById(R.id.touchArea_checkbox).performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("You can only select 10 products for an emergency requisition"));
    }

    @Test
    public void shouldInflateCurrentDataAfterFilter() throws Exception {
        activity.mAdapter.refreshList(getInventoryViewModels());

        activity.mAdapter.filter("8");
        SelectEmergencyProductsViewHolder viewHolder = activity.mAdapter.onCreateViewHolder(new LinearLayout(activity), 0);
        activity.mAdapter.onBindViewHolder(viewHolder, 0);

        String actualValue = ((TextView) viewHolder.itemView.findViewById(R.id.product_name)).getText().toString();
        assertThat(actualValue, is(activity.mAdapter.getFilteredList().get(0).getStyledName().toString()));
    }

    @Test
    public void shouldGoToNextPage() throws Exception {
        ArrayList<InventoryViewModel> inventoryViewModels = getInventoryViewModels();
        activity.mAdapter.refreshList(inventoryViewModels);
        inventoryViewModels.get(0).setChecked(true);
        StockCard stockCard = new StockCard();
        stockCard.setId(100);
        inventoryViewModels.get(0).setStockCard(stockCard);

        activity.btnNext.performClick();

        Intent startedIntent = shadowOf(activity).getNextStartedActivity();

        MatcherAssert.assertThat(startedIntent.getComponent().getClassName(), equalTo(VIARequisitionActivity.class.getName()));
        ArrayList<StockCard> stockCards = (ArrayList<StockCard>) startedIntent.getSerializableExtra(Constants.PARAM_SELECTED_EMERGENCY);

        MatcherAssert.assertThat(stockCards.get(0).getId(), Matchers.is(100L));
    }

    private ArrayList<InventoryViewModel> getInventoryViewModels() {
        ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Product product = new ProductBuilder().setPrimaryName("Product name").setCode(String.valueOf(i)).build();
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