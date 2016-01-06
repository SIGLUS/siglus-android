package org.openlmis.core.view.widget;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class ViaKitViewTest {

    private ViaKitView viaKitView;

    @Before
    public void setUp() throws Exception {
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_show_kit_on_via_rnr_372, true);
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

    @Test
    public void shouldReturnTrueWhenToggleIsOFF() throws Exception {
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_show_kit_on_via_rnr_372, false);
        viaKitView = new ViaKitView(LMISTestApp.getContext());

        viaKitView.etKitReceivedHF.setText("");
        viaKitView.etKitReceivedCHW.setText("");
        viaKitView.etKitOpenedHF.setText("");
        viaKitView.etKitOpenedCHW.setText("");
        boolean validate = viaKitView.validate();
        assertTrue(validate);
        assertEquals(View.INVISIBLE, viaKitView.getVisibility());
    }

}