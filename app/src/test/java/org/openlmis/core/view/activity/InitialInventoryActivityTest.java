package org.openlmis.core.view.activity;

import android.content.Intent;
import android.view.View;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class InitialInventoryActivityTest {
    private InitialInventoryActivity initialInventoryActivity;
    private InitialInventoryPresenter mockedPresenter;

    private List<InventoryViewModel> data;

    @Before
    public void setUp() throws LMISException {
        mockedPresenter = mock(InitialInventoryPresenter.class);
        when(mockedPresenter.loadInventory()).thenReturn(Observable.<List<InventoryViewModel>>empty());

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(InitialInventoryPresenter.class).toInstance(mockedPresenter);
            }
        });

        initialInventoryActivity = Robolectric.buildActivity(InitialInventoryActivity.class).create().get();

        InventoryListAdapter mockedAdapter = mock(InventoryListAdapter.class);
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        data = newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
        when(mockedAdapter.getData()).thenReturn(data);

        initialInventoryActivity.mAdapter = mockedAdapter;
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGoToHomePageAfterInitInventoryOrDoPhysicalInventory(){
        initialInventoryActivity.goToParentPage();

        Intent startIntent = shadowOf(initialInventoryActivity).getNextStartedActivity();
        assertEquals(startIntent.getComponent().getClassName(), HomeActivity.class.getName());
    }

    @Test
    public void shouldGoToStockCardPageAfterAddedNewProduct(){
        Intent intentToStockCard = new Intent();
        intentToStockCard.putExtra(Constants.PARAM_IS_ADD_NEW_DRUG, true);

        initialInventoryActivity = Robolectric.buildActivity(InitialInventoryActivity.class).withIntent(intentToStockCard).create().get();

        initialInventoryActivity.goToParentPage();

        Intent startIntent = shadowOf(initialInventoryActivity).getNextStartedActivity();
        assertEquals(startIntent.getComponent().getClassName(), StockCardListActivity.class.getName());
        assertEquals(startIntent.getFlags(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Test
    public void shouldShowMessageAndNeverBackWhenPressBackInInitInventory() {
        initialInventoryActivity.onBackPressed();

        assertEquals(ShadowToast.getTextOfLatestToast(), initialInventoryActivity.getString(R.string.msg_save_before_exit));

        initialInventoryActivity.onBackPressed();

        assertFalse(initialInventoryActivity.isFinishing());
    }

    @Test
    public void shouldGetAddNewDrugActivity() {
        Intent intent = InitialInventoryActivity.getIntentToMe(RuntimeEnvironment.application, true);

        assertNotNull(intent);
        assertEquals(intent.getComponent().getClassName(), InitialInventoryActivity.class.getName());
        assertTrue(intent.getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false));
    }

    @Test
    public void shouldInitUIWhenInitialInventory() {
        assertThat(initialInventoryActivity.btnSave.getVisibility()).isEqualTo(View.GONE);
        assertTrue(initialInventoryActivity.loadingDialog.isShowing());

        verify(mockedPresenter).loadInventory();
    }

    @Test
    public void shouldDoInitialInventoryWhenBtnDoneClicked() {
        initialInventoryActivity.btnDone.performClick();

        verify(mockedPresenter).doInventory(data);
    }

}