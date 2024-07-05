package org.openlmis.core.view.viewmodel;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Lot;
import org.openlmis.core.utils.DateUtil;

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

  @Test
  public void getLotNumbers_shouldReturnLotNumbers() {
    // given
    String lotNumber = "lotNumber";
    Lot mockedLot = mock(Lot.class);
    when(mockedLot.getLotNumber()).thenReturn(lotNumber);

    IssueVoucherReportLotViewModel mockedIssueVoucherReportLotViewModel =
        mock(IssueVoucherReportLotViewModel.class);
    when(mockedIssueVoucherReportLotViewModel.getLot()).thenReturn(mockedLot);
    issueVoucherReportProductViewModel.setLotViewModelList(
        newArrayList(mockedIssueVoucherReportLotViewModel)
    );
    // when
    List<String> actualLotNumbers = issueVoucherReportProductViewModel.getLotNumbers();
    // then
    assertEquals(1, actualLotNumbers.size());
    assertEquals(lotNumber, actualLotNumbers.get(0));
  }

  @Test
  public void addNewLot_shouldAddNewLotInList() {
    // given
    String lotNumber = "lotNumber";
    Date expirationDate = DateUtil.getCurrentDate();
    String rejectReasonCode = "rejectReasonCode";
    OrderStatus orderStatus = OrderStatus.SHIPPED;
    long shippedQuantity = 0L;

    issueVoucherReportProductViewModel.setLotViewModelList(newArrayList());
    // when
    issueVoucherReportProductViewModel.addNewLot(
        lotNumber, expirationDate, rejectReasonCode, orderStatus, shippedQuantity
    );
    // then
    List<IssueVoucherReportLotViewModel> lotViewModelList = issueVoucherReportProductViewModel.getLotViewModelList();
    assertEquals(1, lotViewModelList.size());
    IssueVoucherReportLotViewModel lotViewModel = lotViewModelList.get(0);
    Lot lot = lotViewModel.getLot();
    assertEquals(lotNumber, lot.getLotNumber());
    assertEquals(expirationDate, lot.getExpirationDate());
    assertEquals(rejectReasonCode, lotViewModel.getRejectedReason());
    assertEquals(orderStatus, lotViewModel.getOrderStatus());
    assertEquals(shippedQuantity, (long) lotViewModel.getShippedQuantity());
  }

  @Test
  public void isRemoteAndShipped_shouldReturnTrueWhenIsRemoteAndShipped() {
    // given
    issueVoucherReportProductViewModel.setLocal(false);
    issueVoucherReportProductViewModel.setOrderStatus(OrderStatus.SHIPPED);
    // when
    boolean actualResult = issueVoucherReportProductViewModel.isRemoteAndShipped();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void isRemoteAndShipped_shouldReturnFalseWhenIsLocal() {
    // given
    issueVoucherReportProductViewModel.setLocal(true);
    issueVoucherReportProductViewModel.setOrderStatus(OrderStatus.SHIPPED);
    // when
    boolean actualResult = issueVoucherReportProductViewModel.isRemoteAndShipped();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void isRemoteAndShipped_shouldReturnTrueWhenIsReceived() {
    // given
    issueVoucherReportProductViewModel.setLocal(false);
    issueVoucherReportProductViewModel.setOrderStatus(OrderStatus.RECEIVED);
    // when
    boolean actualResult = issueVoucherReportProductViewModel.isRemoteAndShipped();
    // then
    assertFalse(actualResult);
  }
}