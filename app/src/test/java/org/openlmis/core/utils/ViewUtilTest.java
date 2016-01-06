package org.openlmis.core.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void shouldSetErrorWhenEditTextIsEmpty() throws Exception {
        EditText editText = new EditText(LMISApp.getContext());
        boolean checkEditTextEmpty = ViewUtil.checkEditTextEmpty(editText);
        assertFalse(checkEditTextEmpty);
        assertThat(editText.getError().toString()).isEqualTo(LMISApp.getContext().getString(R.string.hint_error_input));
    }
}