package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.R;

public class SignatureDialogTest {

  private SignatureDialog signatureDialog;

  @Before
  public void setUp() throws Exception {
    signatureDialog = new SignatureDialog();
  }

  @Test
  public void shouldReturnTrueWhenGivenValidSignature() {
    assertTrue(signatureDialog.checkSignature("sign"));
    assertTrue(signatureDialog.checkSignature("..."));
    assertTrue(signatureDialog.checkSignature("___"));
    assertTrue(signatureDialog.checkSignature("Sign"));
    assertTrue(signatureDialog.checkSignature("abcde"));
  }

  @Test
  public void shouldReturnFalseWhenGivenIllegalSignature() {
    assertFalse(signatureDialog.checkSignature("123"));
    assertFalse(signatureDialog.checkSignature("aa"));
    assertFalse(signatureDialog.checkSignature("abcdef"));
  }

  @Test
  public void shouldReturnMatchedLayoutIdWhenGetSignatureLayoutIdIsCalled() {
    assertEquals(R.layout.dialog_inventory_signature, signatureDialog.getSignatureLayoutId());
  }
}