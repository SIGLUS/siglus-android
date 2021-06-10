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
  public void shouldReturnTrueWhenGivenValidSignature() throws Exception {
    assertTrue(signatureDialog.checkSignature("sign"));
    assertTrue(signatureDialog.checkSignature(".."));
    assertTrue(signatureDialog.checkSignature("__"));
    assertTrue(signatureDialog.checkSignature("Sign"));
  }

  @Test
  public void shouldReturnFalseWhenGivenSignatureWithNumber() throws Exception {
    String signaturesFalse = "123";
    assertFalse(signatureDialog.checkSignature(signaturesFalse));
  }


  @Test
  public void shouldReturnFalseWhenGivenSignatureLessThanTwo() throws Exception {
    String signaturesFalse = "a";
    assertFalse(signatureDialog.checkSignature(signaturesFalse));
  }

  @Test
  public void shouldReturnFalseWhenGivenSignatureMoreThanFive() throws Exception {
    String signaturesFalse = "abcdef";
    assertFalse(signatureDialog.checkSignature(signaturesFalse));
  }

  @Test
  public void shouldReturnTrueWhenGivenSignatureEqualFive() throws Exception {
    String signaturesFalse = "abcde";
    assertTrue(signatureDialog.checkSignature(signaturesFalse));
  }
}