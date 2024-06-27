package org.openlmis.core.view.viewmodel;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.enumeration.OrderStatus;

public class IssueVoucherReportProductViewModelTest {

  private IssueVoucherReportProductViewModel issueVoucherReportProductViewModel;

  @Before
  public void setUp() throws Exception {
    issueVoucherReportProductViewModel = new IssueVoucherReportProductViewModel();
  }

  @Test
  public void shouldReturnTrue_whenValidateIsCalledAndAcceptedQuantityIsGreaterThanShipped() {
    // given
    IssueVoucherReportLotViewModel mockedIssueVoucherReportLotViewModel =
        mock(IssueVoucherReportLotViewModel.class);
    when(mockedIssueVoucherReportLotViewModel.getOrderStatus()).thenReturn(OrderStatus.SHIPPED);
    when(mockedIssueVoucherReportLotViewModel.getAcceptedQuantity()).thenReturn(10L);
    when(mockedIssueVoucherReportLotViewModel.getShippedQuantity()).thenReturn(9L);
    when(mockedIssueVoucherReportLotViewModel.compareAcceptedAndShippedQuantity()).thenReturn(1L);
    when(mockedIssueVoucherReportLotViewModel.getRejectedReason()).thenReturn("reason");

    issueVoucherReportProductViewModel.setLotViewModelList(
        newArrayList(mockedIssueVoucherReportLotViewModel));
    // when
    boolean actualResult = issueVoucherReportProductViewModel.validate();
    // then
    assertTrue(actualResult);
  }
}