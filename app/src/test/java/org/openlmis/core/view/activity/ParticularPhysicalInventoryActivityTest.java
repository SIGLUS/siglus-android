package org.openlmis.core.view.activity;

import android.content.Intent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.robolectric.Robolectric;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class ParticularPhysicalInventoryActivityTest {
    private ParticularPhysicalInventoryActivity particularPhysicalInventoryActivity;

    @Before
    public void setUp() throws LMISException {
        particularPhysicalInventoryActivity = Robolectric.buildActivity(ParticularPhysicalInventoryActivity.class).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGoToHomePageWhenInventoryPageFinished() {
        particularPhysicalInventoryActivity.goToNextPage();

        Intent startIntent = shadowOf(particularPhysicalInventoryActivity).getNextStartedActivity();
        assertTrue(particularPhysicalInventoryActivity.isFinishing());
        assertEquals(startIntent.getComponent().getClassName(), HomeActivity.class.getName());
        assertTrue(SharedPreferenceMgr.getInstance().hasLotInfo());
    }
}