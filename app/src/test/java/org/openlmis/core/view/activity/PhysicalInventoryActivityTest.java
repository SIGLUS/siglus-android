package org.openlmis.core.view.activity;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.PhysicalInventoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryActivityTest extends LMISRepositoryUnitTest {
    private PhysicalInventoryActivity physicalInventoryActivity;
    private PhysicalInventoryPresenter mockedPresenter;

    private List<InventoryViewModel> data;

    @Before
    public void setUp() throws LMISException {
        mockedPresenter = mock(PhysicalInventoryPresenter.class);
        when(mockedPresenter.loadInventory()).thenReturn(Observable.<List<InventoryViewModel>>empty());

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(PhysicalInventoryPresenter.class).toInstance(mockedPresenter);
            }
        });

        physicalInventoryActivity = Robolectric.buildActivity(PhysicalInventoryActivity.class).create().get();

        InventoryListAdapter mockedAdapter = mock(InventoryListAdapter.class);
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        data = newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
        when(mockedAdapter.getData()).thenReturn(data);

        physicalInventoryActivity.mAdapter = mockedAdapter;
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGoBackToParentPageWhenInventoryPageFinished(){
        Intent intentFromParentActivity = new Intent();
        intentFromParentActivity.putExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, true);

        Observable<List<InventoryViewModel>> value = Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {

            }
        });
        when(mockedPresenter.loadInventory()).thenReturn(value);

        physicalInventoryActivity = Robolectric.buildActivity(PhysicalInventoryActivity.class).withIntent(intentFromParentActivity).create().get();
        physicalInventoryActivity.goToParentPage();

        assertTrue(physicalInventoryActivity.isFinishing());
    }
}