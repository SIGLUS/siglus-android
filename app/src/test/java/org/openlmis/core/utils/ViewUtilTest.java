package org.openlmis.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

@RunWith(LMISTestRunner.class)
public class ViewUtilTest extends TestCase {

  @Test
  public void shouldBeSameHeightAfterSyncHeight() {
    final View leftView = new View(LMISApp.getContext());
    final View rightView = new View(LMISApp.getContext());
    leftView.setLayoutParams(new ViewGroup.LayoutParams(1, 100));
    rightView.setLayoutParams(new ViewGroup.LayoutParams(1, 200));
    ViewUtil.syncViewHeight(leftView, rightView);

    assertEquals(leftView.getHeight(), rightView.getHeight());
  }

  @Test
  public void shouldSetErrorWhenEditTextIsEmpty() {
    EditText editText = new EditText(LMISApp.getContext());
    assertFalse(ViewUtil.checkEditTextEmpty(editText));
    assertThat(editText.getError().toString()).hasToString(LMISApp.getContext().getString(R.string.hint_error_input));
  }
}