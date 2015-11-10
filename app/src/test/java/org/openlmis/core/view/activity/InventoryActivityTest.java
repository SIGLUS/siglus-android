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
import android.view.Menu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;


import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class InventoryActivityTest {

    private InventoryActivity inventoryActivity;

    @Before
    public void setUp() throws LMISException{

        inventoryActivity = Robolectric.buildActivity(InventoryActivityMock.class).create().get();
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
        intentToStockCard.putExtra(InventoryActivity.PARAM_IS_ADD_NEW_DRUG, true);
        inventoryActivity = Robolectric.buildActivity(InventoryActivityMock.class).withIntent(intentToStockCard).create().get();

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

    static class InventoryActivityMock extends InventoryActivity {
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            return false;
        }
    }
}
