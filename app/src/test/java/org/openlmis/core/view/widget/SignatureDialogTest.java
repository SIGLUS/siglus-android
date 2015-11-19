package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void shouldReturnFalseWhenGivenSignatureMoreThanOneHundred() throws Exception {
        String signaturesFalse = "";
        for (int i = 0; i < 11; i++) {
            signaturesFalse += "abcdefghio";
        }
        assertFalse(signatureDialog.checkSignature(signaturesFalse));
    }

    @Test
    public void shouldReturnTrueWhenGivenSignatureEqualOneHundred() throws Exception {
        String signaturesFalse = "";
        for (int i = 0; i < 10; i++) {
            signaturesFalse += "abcdefghio";
        }
        assertTrue(signatureDialog.checkSignature(signaturesFalse));
    }
}