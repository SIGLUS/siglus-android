package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

@RunWith(LMISTestRunner.class)
public class ViaKitViewTest {

    private ViaKitView viaKitView;

    @Before
    public void setUp() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_kit, true);
        viaKitView = new ViaKitView(LMISTestApp.getContext());
    }

}