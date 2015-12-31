/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

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
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
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
public class InventoryActivityTest {

    private InventoryActivity inventoryActivity;
    private InventoryPresenter mockedPresenter;

    private List<StockCardViewModel> data;

    @Before
    public void setUp() throws LMISException{
        mockedPresenter = mock(InventoryPresenter.class);
        when(mockedPresenter.loadInventory()).thenReturn(Observable.<List<StockCardViewModel>>empty());

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(InventoryPresenter.class).toInstance(mockedPresenter);
            }
        });

        inventoryActivity = Robolectric.buildActivity(InventoryActivity.class).create().get();

        InventoryListAdapter mockedAdapter = mock(InventoryListAdapter.class);
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        data = newArrayList(new StockCardViewModel(product), new StockCardViewModel(product));
        when(mockedAdapter.getData()).thenReturn(data);

        inventoryActivity.mAdapter = mockedAdapter;
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGoToHomePageAfterInitInventoryOrDoPhysicalInventory(){
        inventoryActivity.goToMainPage();

        Intent startIntent = shadowOf(inventoryActivity).getNextStartedActivity();
        assertEquals(startIntent.getComponent().getClassName(), HomeActivity.class.getName());
    }

    @Test
    public void shouldGoToStockCardPageAfterAddedNewProduct(){
        Intent intentToStockCard = new Intent();
        intentToStockCard.putExtra(Constants.PARAM_IS_ADD_NEW_DRUG, true);

        inventoryActivity = Robolectric.buildActivity(InventoryActivity.class).withIntent(intentToStockCard).create().get();

        inventoryActivity.goToMainPage();

        Intent startIntent = shadowOf(inventoryActivity).getNextStartedActivity();
        assertEquals(startIntent.getComponent().getClassName(), StockCardListActivity.class.getName());
        assertEquals(startIntent.getFlags(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Test
    public void shouldShowMessageAndNeverBackWhenPressBackInInitInventory() {
        inventoryActivity.onBackPressed();

        assertEquals(ShadowToast.getTextOfLatestToast(), inventoryActivity.getString(R.string.msg_save_before_exit));

        inventoryActivity.onBackPressed();

        assertFalse(inventoryActivity.isFinishing());
    }

    @Test
    public void shouldGetAddNewDrugActivity() {
        Intent intent = InventoryActivity.getIntentToMe(RuntimeEnvironment.application, true);

        assertNotNull(intent);
        assertEquals(intent.getComponent().getClassName(), InventoryActivity.class.getName());
        assertTrue(intent.getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false));
    }

    @Test
    public void shouldInitUIWhenInitialInventory() {
        assertThat(inventoryActivity.btnSave.getVisibility()).isEqualTo(View.GONE);
        assertTrue(inventoryActivity.loadingDialog.isShowing());

        verify(mockedPresenter).loadInventory();
    }

    @Test
    public void shouldDoInitialInventoryWhenBtnDoneClicked() {
        inventoryActivity.btnDone.performClick();

        verify(mockedPresenter).doInitialInventory(data);
    }
}
