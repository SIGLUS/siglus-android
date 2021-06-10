package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;

import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

@RunWith(LMISTestRunner.class)
public class ViaRequisitionBodyViewTest {

  private ViaRequisitionBodyView view;

  @Before
  public void setUp() throws Exception {
    view = new ViaRequisitionBodyView(LMISTestApp.getContext());
  }

  @Test
  public void shouldSetEditable() throws Exception {
    view.setEditable(true);
    assertEquals(view.requisitionFormList.getDescendantFocusability(),
        ViewGroup.FOCUS_BLOCK_DESCENDANTS);

    view.setEditable(false);
    assertEquals(view.requisitionFormList.getDescendantFocusability(),
        ViewGroup.FOCUS_BEFORE_DESCENDANTS);
  }
}