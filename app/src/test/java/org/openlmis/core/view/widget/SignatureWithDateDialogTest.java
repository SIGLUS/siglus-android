package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

@RunWith(LMISTestRunner.class)
public class SignatureWithDateDialogTest {

  SignatureWithDateDialog dialog = new SignatureWithDateDialog();

  @Test
  public void shouldReturnMatchedLayoutIdWhenGetSignatureLayoutIdIsCalled() {
    // when
    int signatureLayoutId = dialog.getSignatureLayoutId();
    // then
    assertEquals(R.layout.dialog_signature_with_date, signatureLayoutId);
  }

  @Test
  public void shouldSetIsHideTitleFalseWhenHideTitleIsCalled() {
    // when
    dialog.hideTitle();
    // then
    assertTrue(dialog.isHideTitle);
  }

  @Test
  public void shouldCreateBundleWithDateParamWhenGetBundleToMeIsCalledWithDate() {
    // when
    String date = "2024-05-27";
    Bundle actualBundle = SignatureWithDateDialog.getBundleToMe(date);
    // then
    assertEquals(date, actualBundle.getString("Date"));
  }
}