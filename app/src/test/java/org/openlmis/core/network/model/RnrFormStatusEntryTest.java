package org.openlmis.core.network.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.model.RnRForm.Status;

public class RnrFormStatusEntryTest {

  private RnrFormStatusEntry rnrFormStatusEntry;

  @Before
  public void setUp() throws Exception {
    rnrFormStatusEntry = new RnrFormStatusEntry();
  }

  @Test
  public void isValidStatus_shouldReturnTrueWhenStatusIsInApproval() {
    // given
    rnrFormStatusEntry.setStatus(Status.IN_APPROVAL);
    // when
    boolean actualResult = rnrFormStatusEntry.isValidStatus();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void isValidStatus_shouldReturnTrueWhenStatusIsApproved() {
    // given
    rnrFormStatusEntry.setStatus(Status.APPROVED);
    // when
    boolean actualResult = rnrFormStatusEntry.isValidStatus();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void isValidStatus_shouldReturnTrueWhenStatusIsRejected() {
    // given
    rnrFormStatusEntry.setStatus(Status.REJECTED);
    // when
    boolean actualResult = rnrFormStatusEntry.isValidStatus();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void isValidStatus_shouldReturnFalseWhenStatusIsNotInApprovalOrApprovedOrRejected() {
    // given
    rnrFormStatusEntry.setStatus(Status.AUTHORIZED);
    // when
    boolean actualResult = rnrFormStatusEntry.isValidStatus();
    // then
    assertFalse(actualResult);
  }
}