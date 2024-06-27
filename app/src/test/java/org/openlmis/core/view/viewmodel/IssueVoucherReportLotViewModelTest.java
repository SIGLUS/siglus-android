package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class IssueVoucherReportLotViewModelTest {

  private IssueVoucherReportLotViewModel issueVoucherReportLotViewModel;

  @Before
  public void setUp() throws Exception {
    issueVoucherReportLotViewModel = new IssueVoucherReportLotViewModel();
  }

  @Test
  public void shouldReturnNull_whenCompareAcceptedAndShippedQuantityIsCalledAndAcceptedQuantityIsNull() {
    // given
    issueVoucherReportLotViewModel.setAcceptedQuantity(null);
    issueVoucherReportLotViewModel.setShippedQuantity(5L);
    // when
    Long actualResult = issueVoucherReportLotViewModel.compareAcceptedAndShippedQuantity();
    // then
    assertNull(actualResult);
  }

  @Test
  public void shouldReturnNull_whenCompareAcceptedAndShippedQuantityIsCalledAndShippedQuantityIsNull() {
    // given
    issueVoucherReportLotViewModel.setShippedQuantity(null);
    issueVoucherReportLotViewModel.setAcceptedQuantity(5L);
    // when
    Long actualResult = issueVoucherReportLotViewModel.compareAcceptedAndShippedQuantity();
    // then
    assertNull(actualResult);
  }

  @Test
  public void shouldReturnDiffValue_whenCompareAcceptedAndShippedQuantityIsCalledAndShippedAndAcceptedQuantityAreNotNull() {
    // given
    issueVoucherReportLotViewModel.setAcceptedQuantity(9L);
    issueVoucherReportLotViewModel.setShippedQuantity(10L);
    // when
    Long actualResult = issueVoucherReportLotViewModel.compareAcceptedAndShippedQuantity();
    // then
    assertEquals(-1L, actualResult.longValue());
  }
}