package org.openlmis.core.utils;

import android.view.View;
import android.view.ViewGroup;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;

@RunWith(LMISTestRunner.class)
public class ViewUtilTest extends TestCase {

    @Test
    public void shouldBeSameHeightAfterSyncHeight() throws Exception {
        final View leftView = new View(LMISApp.getContext());
        final View rightView = new View(LMISApp.getContext());
        leftView.setLayoutParams(new ViewGroup.LayoutParams(1,100));
        rightView.setLayoutParams(new ViewGroup.LayoutParams(1,200));
        ViewUtil.syncViewHeight(leftView, rightView);

        assertEquals(leftView.getHeight(),rightView.getHeight());
    }
}