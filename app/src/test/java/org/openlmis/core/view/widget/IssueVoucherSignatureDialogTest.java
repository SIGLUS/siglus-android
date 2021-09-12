package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.DumpFragmentActivity;
import org.robolectric.Robolectric;


@RunWith(LMISTestRunner.class)
public class IssueVoucherSignatureDialogTest {

  private IssueVoucherSignatureDialog signatureDialog;

  @Before
  public void setUp() throws Exception {
    signatureDialog = new IssueVoucherSignatureDialog();
  }

  @Test
  public void shouldReturnTrueWhenGivenValidSignature() {
    // then
    assertTrue(signatureDialog.checkSignature("sign"));
    assertTrue(signatureDialog.checkSignature("..."));
    assertTrue(signatureDialog.checkSignature("___"));
    assertTrue(signatureDialog.checkSignature("Sign"));
    assertTrue(signatureDialog.checkSignature("abcde"));
  }

  @Test
  public void shouldReturnFalseWhenGivenIllegalSignature() {
    // then
    assertFalse(signatureDialog.checkSignature("123"));
    assertFalse(signatureDialog.checkSignature("aa"));
    assertFalse(signatureDialog.checkSignature("abcdef"));
  }

  @Test
  public void shouldGetBundle() {
    // when
    Bundle bundle = IssueVoucherSignatureDialog.getBundleToMe("date", "program");

    //then
    assertEquals("date", bundle.getString("Date"));
  }

  @Test
  public void shouldShowCorrectUIWhenShow() {
    // given
    DumpFragmentActivity dummyActivity = Robolectric.setupActivity(DumpFragmentActivity.class);

    // when
    signatureDialog.setArguments(IssueVoucherSignatureDialog.getBundleToMe(DateUtil.formatDate(new Date()),
        "123"));
    signatureDialog.setDelegate(null);
    signatureDialog.show(dummyActivity.getSupportFragmentManager());

    // then
    assertEquals(null, signatureDialog.getDelegate());
  }

}
