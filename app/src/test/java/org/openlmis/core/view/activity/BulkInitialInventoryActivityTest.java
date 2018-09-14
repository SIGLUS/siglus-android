package org.openlmis.core.view.activity;

import android.content.Intent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.adapter.InventoryListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.Robolectric;

import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class BulkInitialInventoryActivityTest {

    private BulkInitialInventoryActivity bulkInventoryActivity;
    private List<InventoryViewModel> data;


    @Before
    public void setUp() throws LMISException {
        bulkInventoryActivity = Robolectric.buildActivity(BulkInitialInventoryActivity.class).create().get();

        InventoryListAdapter mockedAdapter = mock(InventoryListAdapter.class);
        Product product = new ProductBuilder().setCode("Product code").setPrimaryName("Primary name").setStrength("10mg").build();
        data = newArrayList(new InventoryViewModel(product), new InventoryViewModel(product));
        when(mockedAdapter.getData()).thenReturn(data);

        bulkInventoryActivity = Robolectric.buildActivity(BulkInitialInventoryActivity.class).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGoToHomePageAfterInitInventoryAndSetNeedInventoryToFalse(){
//        bulkInventoryActivity.goToNextPage();
//
//        Intent startIntent = shadowOf(bulkInventoryActivity).getNextStartedActivity();
//        assertEquals(startIntent.getComponent().getClassName(), HomeActivity.class.getName());
//        assertEquals(SharedPreferenceMgr.getInstance().isNeedsInventory(), false);
    }
}