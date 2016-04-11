package org.openlmis.core.view.activity;


import android.content.Intent;
import android.widget.LinearLayout;

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
import org.openlmis.core.view.holder.SelectEmergencyProductsViewHolder;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        activity.viewModels = getInventoryViewModels();
        activity.btnNext.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("Please check product"));
    }

    @Test
    public void shouldShowToastWhenMoreThanLimitChecked() throws Exception {
        activity.viewModels.addAll(getInventoryViewModels());
        for (InventoryViewModel model : activity.viewModels) {
            model.setChecked(true);
        }

        activity.mAdapter.notifyDataSetChanged();

        SelectEmergencyProductsViewHolder viewHolder = activity.mAdapter.onCreateViewHolder(new LinearLayout(activity), 0);
        activity.mAdapter.onBindViewHolder(viewHolder, 0);
        viewHolder.itemView.findViewById(R.id.touchArea_checkbox).performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is("You can only select 10 products for an emergency requisition"));
    }

    @Test
    public void shouldGoToNextPage() throws Exception {
        activity.viewModels.addAll(getInventoryViewModels());
        activity.viewModels.get(0).setChecked(true);
        StockCard stockCard = new StockCard();
        stockCard.setId(100);
        activity.viewModels.get(0).setStockCard(stockCard);

        activity.btnNext.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowOf(activity).getNextStartedActivity();

        MatcherAssert.assertThat(startedIntent.getComponent().getClassName(), equalTo(VIARequisitionActivity.class.getName()));
        ArrayList<StockCard> stockCards = (ArrayList<StockCard>) startedIntent.getSerializableExtra(VIARequisitionActivity.PARAM_SELECTED_EMERGENCY);

        MatcherAssert.assertThat(stockCards.get(0).getId(), Matchers.is(100L));
        assertTrue(shadowActivity.isFinishing());
    }

    private ArrayList<InventoryViewModel> getInventoryViewModels() {
        Product product = new ProductBuilder().setPrimaryName("Product name").setCode("011111").build();
        ArrayList<InventoryViewModel> inventoryViewModels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
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