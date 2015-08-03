package org.openlmis.core.view.activity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.robolectric.Robolectric.application;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class InventoryActivityTest {

    private InventoryActivity inventoryActivity;

    @Before
    public void setUp() {
        inventoryActivity = Robolectric.buildActivity(InventoryActivity.class).create().get();
    }

    @Test
    public void shouldCheckQuantityNotEmpty(){
        inventoryActivity.mAdapter.getInventoryList().get(0).setChecked(true);
        inventoryActivity.btnDone.performClick();
        assertThat(inventoryActivity.mAdapter.getInventoryList().get(0).isValid(), is(false));
    }
}
