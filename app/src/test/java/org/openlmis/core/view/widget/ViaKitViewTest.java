package org.openlmis.core.view.widget;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class ViaKitViewTest {

    private ViaKitView viaKitView;

    @Before
    public void setUp() throws Exception {
        viaKitView = new ViaKitView(LMISTestApp.getContext());
    }

    @Test
    public void shouldReturnTrueWhenDataIsValidate() throws Exception {
        viaKitView.etKitReceivedHF.setText("1");
        viaKitView.etKitReceivedCHW.setText("2");
        viaKitView.etKitOpenedHF.setText("3");
        viaKitView.etKitOpenedCHW.setText("4");
        boolean validate = viaKitView.validate();
        assertEquals(View.VISIBLE, viaKitView.getVisibility());
        assertTrue(validate);
    }

    @Test
    public void shouldReturnFalseWhenDataIsInValidate() throws Exception {
        viaKitView.etKitReceivedHF.setText("");
        viaKitView.etKitReceivedCHW.setText("2");
        viaKitView.etKitOpenedHF.setText("3");
        viaKitView.etKitOpenedCHW.setText("4");
        boolean validate = viaKitView.validate();
        assertFalse(validate);
    }
}