package org.openlmis.core.view.widget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SignatureDialogTest {

  private SignatureDialog signatureDialog;

  @Before
  public void setUp() throws Exception {
    signatureDialog = new SignatureDialog();
  }

  @Test
  public void shouldReturnTrueWhenGivenValidSignature() {
    assertTrue(signatureDialog.checkSignature("sign"));
    assertTrue(signatureDialog.checkSignature(".."));
    assertTrue(signatureDialog.checkSignature("__"));
    assertTrue(signatureDialog.checkSignature("Sign"));
    assertTrue(signatureDialog.checkSignature("abcde"));
  }

  @Test
  public void shouldReturnFalseWhenGivenIllegalSignature() {
    assertFalse(signatureDialog.checkSignature("123"));
    assertFalse(signatureDialog.checkSignature("a"));
    assertFalse(signatureDialog.checkSignature("abcdef"));
  }
}